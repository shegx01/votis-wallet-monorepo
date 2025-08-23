defmodule BeVotisWallet.Services.Turnkey.QueriesTest do
  use ExUnit.Case, async: true

  import Mox

  alias BeVotisWallet.Services.Turnkey.Queries
  alias BeVotisWallet.HTTPClient.Mock

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  # Set up the mock context
  setup do
    BeVotisWallet.Test.Mocks.setup_mocks()
  end

  describe "get_organization/1" do
    test "successfully gets organization with default org ID" do
      expected_response = %{
        "organization" => %{
          "organizationId" => "org_123",
          "organizationName" => "Test Organization",
          "rootUsers" => []
        }
      }

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/query/get_organization")
        assert [{"Content-Type", "application/json"}, {"X-Turnkey-API-Key", _}] = headers

        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"]

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Queries.get_organization()

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "successfully gets organization with specific org ID" do
      org_id = "custom_org_456"

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"organization" => %{"organizationId" => org_id}}}
      end)

      result = Queries.get_organization(org_id)

      assert {:ok, _response} = result
    end
  end

  describe "get_user/2" do
    test "successfully gets user information" do
      org_id = "org_123"
      user_id = "user_456"

      expected_response = %{
        "user" => %{
          "userId" => user_id,
          "userName" => "testuser",
          "userEmail" => "test@example.com"
        }
      }

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/query/get_user")

        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["userId"] == user_id

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Queries.get_user(org_id, user_id)

      assert {:ok, response} = result
      assert response == expected_response
    end
  end

  describe "list_users/2" do
    test "successfully lists users without pagination" do
      org_id = "org_123"

      expected_response = %{
        "users" => [
          %{"userId" => "user_1", "userName" => "user1"},
          %{"userId" => "user_2", "userName" => "user2"}
        ]
      }

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        # Should not have pagination params when none provided
        assert Map.get(decoded_body, "limit") == nil
        assert Map.get(decoded_body, "paginationToken") == nil

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Queries.list_users(org_id)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "successfully lists users with pagination" do
      org_id = "org_123"
      opts = [limit: 10, pagination_token: "token_abc"]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["limit"] == 10
        assert decoded_body["paginationToken"] == "token_abc"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"users" => [], "paginationToken" => "next_token"}}
      end)

      result = Queries.list_users(org_id, opts)

      assert {:ok, _response} = result
    end
  end

  describe "get_wallet/2" do
    test "successfully gets wallet information" do
      org_id = "org_123"
      wallet_id = "wallet_456"

      expected_response = %{
        "wallet" => %{
          "walletId" => wallet_id,
          "walletName" => "Test Wallet",
          "accounts" => [
            %{
              "accountId" => "account_789",
              "address" => "0x1234567890abcdef",
              "curve" => "CURVE_SECP256K1"
            }
          ]
        }
      }

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/query/get_wallet")

        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["walletId"] == wallet_id

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Queries.get_wallet(org_id, wallet_id)

      assert {:ok, response} = result
      assert response == expected_response
    end
  end

  describe "list_wallets/2" do
    test "successfully lists wallets without filters" do
      org_id = "org_123"

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert Map.get(decoded_body, "userId") == nil

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"wallets" => []}}
      end)

      result = Queries.list_wallets(org_id)

      assert {:ok, _response} = result
    end

    test "successfully lists wallets with user filter" do
      org_id = "org_123"
      user_id = "user_456"
      opts = [user_id: user_id, limit: 5]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["userId"] == user_id
        assert decoded_body["limit"] == 5

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"wallets" => []}}
      end)

      result = Queries.list_wallets(org_id, opts)

      assert {:ok, _response} = result
    end
  end

  describe "get_activity/2" do
    test "successfully gets activity information" do
      org_id = "org_123"
      activity_id = "activity_456"

      expected_response = %{
        "activity" => %{
          "id" => activity_id,
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "type" => "ACTIVITY_TYPE_CREATE_WALLET",
          "result" => %{
            "createWalletResult" => %{
              "walletId" => "wallet_789"
            }
          }
        }
      }

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/query/get_activity")

        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["activityId"] == activity_id

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Queries.get_activity(org_id, activity_id)

      assert {:ok, response} = result
      assert response == expected_response
    end
  end

  describe "list_activities/2" do
    test "successfully lists activities without filters" do
      org_id = "org_123"

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert Map.get(decoded_body, "activityType") == nil

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activities" => []}}
      end)

      result = Queries.list_activities(org_id)

      assert {:ok, _response} = result
    end

    test "successfully lists activities with type filter" do
      org_id = "org_123"
      opts = [activity_type: "ACTIVITY_TYPE_CREATE_WALLET", limit: 20]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["activityType"] == "ACTIVITY_TYPE_CREATE_WALLET"
        assert decoded_body["limit"] == 20

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activities" => []}}
      end)

      result = Queries.list_activities(org_id, opts)

      assert {:ok, _response} = result
    end
  end

  describe "get_wallet_accounts/2" do
    test "successfully gets wallet accounts" do
      org_id = "org_123"
      wallet_id = "wallet_456"

      expected_response = %{
        "accounts" => [
          %{
            "accountId" => "account_1",
            "address" => "0x1111111111111111",
            "curve" => "CURVE_SECP256K1"
          },
          %{
            "accountId" => "account_2",
            "address" => "0x2222222222222222",
            "curve" => "CURVE_SECP256K1"
          }
        ]
      }

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/query/get_wallet_accounts")

        decoded_body = Jason.decode!(body)
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["walletId"] == wallet_id

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = Queries.get_wallet_accounts(org_id, wallet_id)

      assert {:ok, response} = result
      assert response == expected_response
    end
  end

  describe "error handling" do
    test "handles HTTP error responses" do
      org_id = "org_123"
      error_message = %{"message" => "Organization not found"}

      Mock
      |> expect(:build_payload, fn _, _, _, _ ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)
      |> expect(:request, fn _payload ->
        {:error, 404, error_message}
      end)

      result = Queries.get_organization(org_id)

      assert {:error, 404, ^error_message} = result
    end

    test "handles network errors" do
      org_id = "org_123"

      Mock
      |> expect(:build_payload, fn _, _, _, _ ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)
      |> expect(:request, fn _payload ->
        {:error, 0, :timeout}
      end)

      result = Queries.get_organization(org_id)

      assert {:error, 0, :timeout} = result
    end
  end
end
