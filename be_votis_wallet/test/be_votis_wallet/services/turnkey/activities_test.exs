defmodule BeVotisWallet.Services.Turnkey.ActivitiesTest do
  use ExUnit.Case, async: false

  import Mox

  alias BeVotisWallet.Services.Turnkey.Activities
  alias BeVotisWallet.HTTPClient.Mock

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  # Set up the mock context
  setup do
    BeVotisWallet.Test.Mocks.setup_mocks()
  end

  describe "create_sub_organization/2" do
    test "successfully creates a sub-organization" do
      org_name = "Test Organization"

      expected_response = %{
        "activity" => %{
          "id" => "activity_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createSubOrganizationResult" => %{
              "organizationId" => "org_456"
            }
          }
        }
      }

      # Set up mock stubs
      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")
        assert [{"Content-Type", "application/json"}, {"X-Turnkey-API-Key", _}] = headers

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION"
        assert decoded_body["parameters"]["subOrganizationName"] == org_name
        assert decoded_body["organizationId"]
        assert decoded_body["timestampMs"]

        # Return a mock request payload
        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      # Execute the function
      result = Activities.create_sub_organization(org_name)

      # Verify result
      assert {:ok, response} = result
      assert response == expected_response

      assert get_in(response, [
               "activity",
               "result",
               "createSubOrganizationResult",
               "organizationId"
             ]) == "org_456"
    end

    test "handles API error response" do
      org_name = "Test Organization"
      error_message = %{"message" => "Invalid organization name"}

      stub(Mock, :build_payload, fn _, _, _, _ ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 400, error_message}
      end)

      result = Activities.create_sub_organization(org_name)

      assert {:error, 400, ^error_message} = result
    end

    test "accepts additional options" do
      org_name = "Test Organization"
      opts = [root_quorum_threshold: 2, root_users: ["user1", "user2"]]

      stub(Mock, :build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["parameters"]["rootQuorumThreshold"] == 2
        assert decoded_body["parameters"]["rootUsers"] == ["user1", "user2"]

        %{method: :post, url: "test", headers: [], body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "test"}}}
      end)

      Activities.create_sub_organization(org_name, opts)
    end
  end

  describe "create_user/4" do
    test "successfully creates a user" do
      org_id = "org_123"
      user_name = "testuser"
      user_email = "test@example.com"

      expected_response = %{
        "activity" => %{
          "id" => "activity_456",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createUsersResult" => %{
              "userIds" => ["user_789"]
            }
          }
        }
      }

      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_USERS"
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["parameters"]["userName"] == user_name
        assert decoded_body["parameters"]["userEmail"] == user_email

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Activities.create_user(org_id, user_name, user_email)

      assert {:ok, response} = result
      assert response == expected_response
    end
  end

  describe "create_wallet/4" do
    test "successfully creates a wallet with accounts" do
      org_id = "org_123"
      user_id = "user_456"
      wallet_name = "Test Wallet"

      accounts = [
        %{
          "curve" => "CURVE_SECP256K1",
          "pathFormat" => "PATH_FORMAT_BIP32",
          "path" => "m/44'/60'/0'/0/0",
          "addressFormat" => "ADDRESS_FORMAT_ETHEREUM"
        }
      ]

      expected_response = %{
        "activity" => %{
          "id" => "activity_789",
          "result" => %{
            "createWalletResult" => %{
              "walletId" => "wallet_abc"
            }
          }
        }
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_WALLET"
        assert decoded_body["parameters"]["userId"] == user_id
        assert decoded_body["parameters"]["walletName"] == wallet_name
        assert decoded_body["parameters"]["accounts"] == accounts

        %{method: :post, url: "test", headers: [], body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Activities.create_wallet(org_id, user_id, wallet_name, accounts)

      assert {:ok, response} = result
      assert response == expected_response
    end
  end

  describe "sign_transaction/4" do
    test "successfully signs a transaction" do
      org_id = "org_123"
      sign_with = "private_key_456"
      unsigned_tx = "0x123456789abcdef"

      expected_response = %{
        "activity" => %{
          "result" => %{
            "signTransactionResult" => %{
              "signedTransaction" => "0x987654321fedcba"
            }
          }
        }
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_SIGN_TRANSACTION_V2"
        assert decoded_body["parameters"]["signWith"] == sign_with
        assert decoded_body["parameters"]["unsignedTransaction"] == unsigned_tx
        assert decoded_body["parameters"]["type"] == "TRANSACTION_TYPE_ETHEREUM"

        %{method: :post, url: "test", headers: [], body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Activities.sign_transaction(org_id, sign_with, unsigned_tx)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "accepts custom transaction type" do
      org_id = "org_123"
      sign_with = "private_key_456"
      unsigned_tx = "bitcoin_tx_data"
      opts = [transaction_type: "TRANSACTION_TYPE_BITCOIN"]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["parameters"]["type"] == "TRANSACTION_TYPE_BITCOIN"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "test"}}}
      end)

      Activities.sign_transaction(org_id, sign_with, unsigned_tx, opts)
    end
  end

  describe "configuration and error handling" do
    test "uses configured organization ID when none provided" do
      # This would test the private get_default_organization_id function
      # by calling a function that uses it

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        # Should use the configured org ID from test config
        assert is_binary(decoded_body["organizationId"])

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "test"}}}
      end)

      Activities.create_sub_organization("test")
    end
  end

  describe "create_read_only_session/3" do
    test "successfully creates a read-only session" do
      org_id = "org_123"
      user_id = "user_456"

      expected_response = %{
        "activity" => %{
          "id" => "activity_789",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createReadOnlySessionResult" => %{
              "sessionToken" => "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
              "userId" => user_id,
              "organizationId" => org_id
            }
          }
        }
      }

      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_ONLY_SESSION"
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["parameters"]["userId"] == user_id

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Activities.create_read_only_session(org_id, user_id)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "creates session without user_id when not provided" do
      org_id = "org_123"

      stub(Mock, :build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_ONLY_SESSION"
        assert decoded_body["organizationId"] == org_id
        # Should not include userId in parameters when nil
        refute Map.has_key?(decoded_body["parameters"], "userId")

        %{method: :post, url: "test", headers: [], body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "test"}}}
      end)

      Activities.create_read_only_session(org_id)
    end
  end

  describe "create_read_write_session/4" do
    test "successfully creates a read-write session" do
      org_id = "org_123"
      user_id = "user_456"
      # Mock 65-byte uncompressed key
      target_public_key = "04" <> String.duplicate("ab", 64)

      expected_response = %{
        "activity" => %{
          "id" => "activity_abc",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createReadWriteSessionResultV2" => %{
              "apiKeyId" => "api_key_789",
              "credentialBundle" => "encrypted_bundle_hex_data"
            }
          }
        }
      }

      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_WRITE_SESSION_V2"
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["parameters"]["targetPublicKey"] == target_public_key
        assert decoded_body["parameters"]["userId"] == user_id
        # Default value
        assert decoded_body["parameters"]["expirationSeconds"] == "900"

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Activities.create_read_write_session(org_id, target_public_key, user_id)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "accepts custom options" do
      org_id = "org_123"
      target_public_key = "04" <> String.duplicate("cd", 64)
      user_id = "user_456"

      opts = [
        api_key_name: "Custom Session Name",
        expiration_seconds: 1800,
        invalidate_existing: true
      ]

      stub(Mock, :build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_WRITE_SESSION_V2"
        assert decoded_body["parameters"]["targetPublicKey"] == target_public_key
        assert decoded_body["parameters"]["userId"] == user_id
        assert decoded_body["parameters"]["apiKeyName"] == "Custom Session Name"
        assert decoded_body["parameters"]["expirationSeconds"] == "1800"
        assert decoded_body["parameters"]["invalidateExisting"] == true

        %{method: :post, url: "test", headers: [], body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "test"}}}
      end)

      Activities.create_read_write_session(org_id, target_public_key, user_id, opts)
    end

    test "removes nil values from parameters" do
      org_id = "org_123"
      target_public_key = "04" <> String.duplicate("ef", 64)
      # user_id is nil (not provided)

      stub(Mock, :build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_WRITE_SESSION_V2"
        assert decoded_body["parameters"]["targetPublicKey"] == target_public_key
        # Should not include userId in parameters when nil
        refute Map.has_key?(decoded_body["parameters"], "userId")
        # Should not include apiKeyName when not provided
        refute Map.has_key?(decoded_body["parameters"], "apiKeyName")

        %{method: :post, url: "test", headers: [], body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "test"}}}
      end)

      Activities.create_read_write_session(org_id, target_public_key)
    end
  end

  describe "request signing" do
    test "signs request when API private key is configured" do
      # Generate a test keypair
      alias BeVotisWallet.Services.Turnkey.Crypto
      {_public_pem, private_pem} = Crypto.generate_api_keypair()

      # Temporarily set the API private key
      original_config = Application.get_env(:be_votis_wallet, :turnkey, [])
      new_config = Keyword.put(original_config, :api_private_key, private_pem)
      Application.put_env(:be_votis_wallet, :turnkey, new_config)

      try do
        stub(Mock, :build_payload, fn _method, _url, headers, body ->
          decoded_body = Jason.decode!(body)

          # Verify that the request does NOT contain a stamp in the body
          refute Map.has_key?(decoded_body, "stamp")

          # Verify that the X-Stamp header is present
          stamp_header = Enum.find(headers, fn {name, _value} -> name == "X-Stamp" end)
          assert stamp_header != nil
          {"X-Stamp", stamp_value} = stamp_header
          assert is_binary(stamp_value)

          # Verify the stamp is valid base64
          assert {:ok, _} = Base.decode64(stamp_value)

          %{method: :post, url: "test", headers: headers, body: body}
        end)

        stub(Mock, :request, fn _payload ->
          {:ok, %{"activity" => %{"id" => "test"}}}
        end)

        # Execute a function that will trigger request signing
        result = Activities.create_sub_organization("Test Org")
        assert {:ok, _} = result
      after
        # Restore original config
        Application.put_env(:be_votis_wallet, :turnkey, original_config)
      end
    end

    test "handles signing failure gracefully" do
      # Set an invalid private key to trigger signing failure
      original_config = Application.get_env(:be_votis_wallet, :turnkey, [])
      new_config = Keyword.put(original_config, :api_private_key, "invalid-key")
      Application.put_env(:be_votis_wallet, :turnkey, new_config)

      try do
        stub(Mock, :build_payload, fn _method, _url, _headers, body ->
          decoded_body = Jason.decode!(body)

          # Verify that the request does NOT contain a stamp due to signing failure
          refute Map.has_key?(decoded_body, "stamp")

          %{method: :post, url: "test", headers: [], body: body}
        end)

        stub(Mock, :request, fn _payload ->
          {:ok, %{"activity" => %{"id" => "test"}}}
        end)

        # Execute a function that will attempt request signing
        result = Activities.create_sub_organization("Test Org")
        # Should still succeed despite signing failure
        assert {:ok, _} = result
      after
        # Restore original config
        Application.put_env(:be_votis_wallet, :turnkey, original_config)
      end
    end

    test "uses X-Stamp-WebAuthn header for WebAuthn authentication" do
      # Simulate what a mobile client would do - create a client signature
      alias BeVotisWallet.Services.Turnkey.Crypto
      {_public_pem, private_pem} = Crypto.generate_api_keypair()

      # Create a mock client signature (in reality, this would come from WebAuthn/hardware device)
      {:ok, client_signature} = Crypto.create_request_stamp("mock request body", private_pem)

      stub(Mock, :build_payload, fn _method, _url, headers, body ->
        decoded_body = Jason.decode!(body)

        # Verify that the request does NOT contain a stamp in the body
        refute Map.has_key?(decoded_body, "stamp")

        # Verify that the X-Stamp-WebAuthn header is present (not X-Stamp)
        stamp_header = Enum.find(headers, fn {name, _value} -> name == "X-Stamp-WebAuthn" end)
        assert stamp_header != nil
        {"X-Stamp-WebAuthn", stamp_value} = stamp_header
        assert is_binary(stamp_value)

        # Verify it's the client signature we provided
        assert stamp_value == client_signature

        # Verify the stamp is valid base64
        assert {:ok, _} = Base.decode64(stamp_value)

        # Verify X-Stamp header is NOT present
        x_stamp_header = Enum.find(headers, fn {name, _value} -> name == "X-Stamp" end)
        assert x_stamp_header == nil

        %{method: :post, url: "test", headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "test"}}}
      end)

      # Execute with WebAuthn auth type and client signature (as mobile would do)
      result =
        Activities.create_sub_organization("Test Org",
          auth_type: :webauthn,
          client_signature: client_signature
        )

      assert {:ok, _} = result
    end
  end
end
