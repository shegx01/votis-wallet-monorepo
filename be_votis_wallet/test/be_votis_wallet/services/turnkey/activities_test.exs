defmodule BeVotisWallet.Services.Turnkey.ActivitiesTest do
  use ExUnit.Case, async: true

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

      # Set up mock expectation
      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
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
      |> expect(:request, fn _payload ->
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

      Mock
      |> expect(:build_payload, fn _, _, _, _ ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)
      |> expect(:request, fn _payload ->
        {:error, 400, error_message}
      end)

      result = Activities.create_sub_organization(org_name)

      assert {:error, 400, ^error_message} = result
    end

    test "accepts additional options" do
      org_name = "Test Organization"
      opts = [root_quorum_threshold: 2, root_users: ["user1", "user2"]]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["parameters"]["rootQuorumThreshold"] == 2
        assert decoded_body["parameters"]["rootUsers"] == ["user1", "user2"]

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
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

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_USERS"
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["parameters"]["userName"] == user_name
        assert decoded_body["parameters"]["userEmail"] == user_email

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
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

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_WALLET"
        assert decoded_body["parameters"]["userId"] == user_id
        assert decoded_body["parameters"]["walletName"] == wallet_name
        assert decoded_body["parameters"]["accounts"] == accounts

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
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

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_SIGN_TRANSACTION_V2"
        assert decoded_body["parameters"]["signWith"] == sign_with
        assert decoded_body["parameters"]["unsignedTransaction"] == unsigned_tx
        assert decoded_body["parameters"]["type"] == "TRANSACTION_TYPE_ETHEREUM"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
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
end
