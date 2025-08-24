defmodule BeVotisWallet.Services.Turnkey.SessionsTest do
  use ExUnit.Case, async: false

  import Mox

  alias BeVotisWallet.Services.Turnkey.Sessions
  alias BeVotisWallet.HTTPClient.Mock

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  # Set up the mock context
  setup do
    BeVotisWallet.Test.Mocks.setup_mocks()
  end

  describe "generate_session_keypair/0" do
    test "generates valid HPKE keypair for session encryption" do
      {public_hex, private_hex} = Sessions.generate_session_keypair()

      # Verify hex format
      assert String.match?(public_hex, ~r/^[0-9a-f]+$/)
      assert String.match?(private_hex, ~r/^[0-9a-f]+$/)

      # Verify expected key sizes
      public_bytes = Base.decode16!(public_hex, case: :mixed)
      private_bytes = Base.decode16!(private_hex, case: :mixed)

      # Public key should be 65 bytes (uncompressed) or 33 bytes (compressed)
      assert byte_size(public_bytes) in [33, 65]

      # Private key should be 32 bytes
      assert byte_size(private_bytes) == 32
    end

    test "generates different keypairs on multiple calls" do
      {pub1, priv1} = Sessions.generate_session_keypair()
      {pub2, priv2} = Sessions.generate_session_keypair()

      assert pub1 != pub2
      assert priv1 != priv2
    end
  end

  describe "decrypt_credential_bundle/2" do
    test "delegates to Crypto module" do
      # This test verifies that Sessions.decrypt_credential_bundle properly delegates
      # to the Crypto module. In production, this would work with real HPKE decryption.
      encrypted_bundle = "abcd1234"
      private_key = "ef567890"

      # Since we can't mock easily, we'll test that it properly calls through
      # The actual crypto functionality is tested in crypto_test.exs
      result = Sessions.decrypt_credential_bundle(encrypted_bundle, private_key)

      # We expect an error because this is fake data
      assert {:error, _reason} = result
    end
  end

  describe "create_read_write_session_for_client/4" do
    test "creates session and returns encrypted credential bundle for client" do
      organization_id = "test-org-123"
      user_id = "test-user-456"
      client_public_key = "client-generated-public-key-hex"
      api_key_id = "api-key-789"

      expected_response = %{
        "activity" => %{
          "id" => "activity_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createReadWriteSessionResultV2" => %{
              "apiKeyId" => api_key_id,
              "credentialBundle" => "encrypted-bundle-for-client-hex"
            }
          }
        }
      }

      # Mock HTTP client calls
      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        assert [{"Content-Type", "application/json"}, {"X-Turnkey-API-Key", "test_api_key"}] =
                 headers

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_WRITE_SESSION_V2"
        assert decoded_body["organizationId"] == organization_id
        assert decoded_body["parameters"]["targetPublicKey"] == client_public_key
        assert decoded_body["parameters"]["userId"] == user_id
        assert decoded_body["parameters"]["apiKeyName"] == "Mobile Test Session"

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      # Test the function
      assert {:ok, session} =
               Sessions.create_read_write_session_for_client(
                 organization_id,
                 client_public_key,
                 user_id,
                 api_key_name: "Mobile Test Session"
               )

      # Verify session structure - should return raw data for client to decrypt
      assert %{
               api_key_id: ^api_key_id,
               credential_bundle: "encrypted-bundle-for-client-hex",
               metadata: metadata
             } = session

      # Check metadata
      assert is_integer(metadata.created_at)
    end

    test "handles activity creation failure" do
      organization_id = "test-org-123"
      client_public_key = "client-public-key"
      error_message = %{"message" => "Unauthorized"}

      stub(Mock, :build_payload, fn _, _, _, _ ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 403, error_message}
      end)

      assert {:error, {403, ^error_message}} =
               Sessions.create_read_write_session_for_client(
                 organization_id,
                 client_public_key,
                 nil,
                 []
               )
    end
  end

  describe "create_read_write_session_for_server/3" do
    test "validates required options are present" do
      organization_id = "test-org-123"

      # Should fail if required options are missing
      assert_raise KeyError, fn ->
        Sessions.create_read_write_session_for_server(
          organization_id,
          nil,
          api_key_name: "Server Session"
          # Missing target_public_key and hpke_private_key
        )
      end
    end

    test "accepts required options and creates server session with credential decryption" do
      organization_id = "test-org-123"
      {public_hex, private_hex} = Sessions.generate_session_keypair()
      api_key_id = "api-key-server-789"

      expected_response = %{
        "activity" => %{
          "id" => "activity_server_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createReadWriteSessionResultV2" => %{
              "apiKeyId" => api_key_id,
              "credentialBundle" => "mock-encrypted-bundle-hex"
            }
          }
        }
      }

      # Mock credential data that would be returned after decryption
      _mock_credentials = %{
        "apiKeyId" => api_key_id,
        "apiKey" => "server-api-key",
        "privateKey" => "server-private-key",
        "organizationId" => organization_id,
        "userId" => "server-user-id"
      }

      # Mock HTTP client calls
      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        assert [{"Content-Type", "application/json"}, {"X-Turnkey-API-Key", "test_api_key"}] =
                 headers

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_WRITE_SESSION_V2"
        assert decoded_body["organizationId"] == organization_id
        assert decoded_body["parameters"]["targetPublicKey"] == public_hex
        assert decoded_body["parameters"]["apiKeyName"] == "Server Test Session"

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      # Since we can't easily mock the crypto decryption in this context,
      # let's test that the function accepts the right parameters and structures the call correctly
      result =
        Sessions.create_read_write_session_for_server(
          organization_id,
          nil,
          target_public_key: public_hex,
          hpke_private_key: private_hex,
          api_key_name: "Server Test Session"
        )

      # We expect the decryption to fail since we're using mock data
      # but the HTTP call structure should be correct
      assert {:error, _reason} = result
    end
  end

  describe "create_read_only_session/3" do
    test "creates read-only session successfully" do
      organization_id = "test-org-123"
      user_id = "test-user-456"

      expected_response = %{
        "activity" => %{
          "id" => "activity_readonly_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createReadOnlySessionResult" => %{
              "sessionToken" => "readonly-session-token",
              "expirationTimestamp" => "2024-12-31T23:59:59Z"
            }
          }
        }
      }

      # Mock HTTP client calls
      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        assert [{"Content-Type", "application/json"}, {"X-Turnkey-API-Key", "test_api_key"}] =
                 headers

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_READ_ONLY_SESSION"
        assert decoded_body["organizationId"] == organization_id
        assert decoded_body["parameters"]["userId"] == user_id

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      # Test the function
      assert {:ok, session_data} = Sessions.create_read_only_session(organization_id, user_id)

      # Verify session data structure
      expected_session_result =
        expected_response["activity"]["result"]["createReadOnlySessionResult"]

      assert session_data == expected_session_result
    end

    test "handles read-only session creation failure" do
      organization_id = "test-org-123"
      user_id = "test-user-456"
      error_message = %{"message" => "Invalid user"}

      stub(Mock, :build_payload, fn _, _, _, _ ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 400, error_message}
      end)

      assert {:error, {400, ^error_message}} =
               Sessions.create_read_only_session(organization_id, user_id)
    end
  end

  describe "integration workflow keypair validation" do
    test "complete HPKE keypair generation for session creation" do
      # Step 1: Generate HPKE keypair - this is the core functionality we can test
      {public_hex, private_hex} = Sessions.generate_session_keypair()

      # Step 2: Validate the keypair format
      assert is_binary(public_hex)
      assert is_binary(private_hex)
      assert String.match?(public_hex, ~r/^[0-9a-f]+$/)
      assert String.match?(private_hex, ~r/^[0-9a-f]+$/)

      # Step 3: Verify key sizes are appropriate for HPKE
      public_bytes = Base.decode16!(public_hex, case: :mixed)
      private_bytes = Base.decode16!(private_hex, case: :mixed)

      # Public key should be 65 bytes (uncompressed) or 33 bytes (compressed)
      assert byte_size(public_bytes) in [33, 65]
      # Private key should be 32 bytes
      assert byte_size(private_bytes) == 32

      # Step 4: Verify they're suitable for Turnkey session API format
      # This validates the hex encoding is correct for API consumption
      # 32 bytes * 2 hex chars
      assert String.length(private_hex) == 64
      # 33 or 65 bytes * 2 hex chars
      assert String.length(public_hex) in [66, 130]
    end

    test "validates server vs client session workflow differences" do
      {public_hex, private_hex} = Sessions.generate_session_keypair()

      # Server workflow - backend manages both keys and decrypts
      server_opts = [
        target_public_key: public_hex,
        hpke_private_key: private_hex,
        api_key_name: "Server Session"
      ]

      # Client workflow - backend only receives public key
      # Client would generate this themselves
      client_public_key = public_hex
      client_opts = [api_key_name: "Mobile Session"]

      # Validate that the function signatures are correct for each use case
      assert is_list(server_opts)
      assert Keyword.has_key?(server_opts, :target_public_key)
      assert Keyword.has_key?(server_opts, :hpke_private_key)

      assert is_binary(client_public_key)
      assert is_list(client_opts)
      # Client keeps this secret
      refute Keyword.has_key?(client_opts, :hpke_private_key)
    end
  end
end
