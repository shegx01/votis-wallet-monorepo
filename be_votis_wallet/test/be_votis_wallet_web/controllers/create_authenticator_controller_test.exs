defmodule BeVotisWalletWeb.CreateAuthenticatorControllerTest do
  use BeVotisWalletWeb.ConnCase, async: false

  import Mox

  alias BeVotisWallet.HTTPClient.Mock

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  # Set up the mock context
  setup do
    BeVotisWallet.Test.Mocks.setup_mocks()
  end

  setup %{conn: conn} do
    {:ok, conn: put_req_header(conn, "accept", "application/json")}
  end

  describe "POST /private/create_authenticators" do
    test "successfully creates authenticator for existing user", %{conn: conn} do
      # Create a test user using factory
      user =
        insert(:user, %{
          email: "test@example.com",
          sub_org_id: "org_123"
        })

      # Mock Turnkey response
      expected_response = %{
        "activity" => %{
          "id" => "activity_create_auth_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "result" => %{
            "createAuthenticatorsResult" => %{
              "authenticatorIds" => ["auth_123"]
            }
          }
        }
      }

      # Set up HTTP client mock
      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/create_authenticators")

        # Verify X-Stamp header for API key authentication
        assert Enum.any?(headers, fn
                 {"X-Stamp", _} -> true
                 _ -> false
               end)

        assert body == "test_stamped_body"
        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, expected_response}
      end)

      # Make the request
      params = %{
        "email" => user.email,
        "stamped_body" => "test_stamped_body",
        "stamp" => "test_stamp"
      }

      conn = post(conn, ~p"/private/create_authenticators", params)

      # Verify response
      assert json_response(conn, 200) == %{"message" => "Authenticator created"}
    end

    test "returns 404 when user does not exist", %{conn: conn} do
      params = %{
        "email" => "nonexistent@example.com",
        "stamped_body" => "test_stamped_body",
        "stamp" => "test_stamp"
      }

      conn = post(conn, ~p"/private/create_authenticators", params)

      assert json_response(conn, 404) == %{
               "error" => "User not found",
               "message" => "The specified user does not exist"
             }
    end

    test "returns 400 when stamped_body is missing", %{conn: conn} do
      # Create a test user using factory
      insert(:user, %{email: "test@example.com"})

      params = %{
        "email" => "test@example.com",
        "stamp" => "test_stamp"
        # Missing stamped_body
      }

      conn = post(conn, ~p"/private/create_authenticators", params)

      assert json_response(conn, 400) == %{"error" => "Missing required parameter: stamped_body"}
    end

    test "returns 400 when stamp is missing", %{conn: conn} do
      # Create a test user using factory
      insert(:user, %{email: "test@example.com"})

      params = %{
        "email" => "test@example.com",
        "stamped_body" => "test_stamped_body"
        # Missing stamp
      }

      conn = post(conn, ~p"/private/create_authenticators", params)

      assert json_response(conn, 400) == %{"error" => "Missing required parameter: stamp"}
    end

    test "returns 400 when stamped_body is empty", %{conn: conn} do
      # Create a test user using factory
      insert(:user, %{email: "test@example.com"})

      params = %{
        "email" => "test@example.com",
        "stamped_body" => "",
        "stamp" => "test_stamp"
      }

      conn = post(conn, ~p"/private/create_authenticators", params)

      assert json_response(conn, 400) == %{"error" => "Missing required parameter: stamped_body"}
    end

    test "handles Turnkey API errors correctly", %{conn: conn} do
      # Create a test user using factory
      insert(:user, %{email: "test@example.com"})

      # Mock Turnkey error response
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 400, %{"message" => "Invalid authenticator data"}}
      end)

      params = %{
        "email" => "test@example.com",
        "stamped_body" => "test_stamped_body",
        "stamp" => "test_stamp"
      }

      conn = post(conn, ~p"/private/create_authenticators", params)

      assert json_response(conn, 400) == %{
               "error" => "Failed to create authenticator",
               "message" => "External service error"
             }
    end

    test "maps various Turnkey status codes correctly", %{conn: conn} do
      # Create a test user using factory
      insert(:user, %{email: "test@example.com"})

      test_cases = [
        {401, :unauthorized},
        {403, :forbidden},
        {404, :not_found},
        {409, :conflict},
        {422, :unprocessable_entity},
        {429, :too_many_requests},
        {500, :internal_server_error},
        {502, :bad_gateway},
        {503, :service_unavailable},
        # Unknown status code
        {999, :internal_server_error}
      ]

      for {turnkey_status, expected_phoenix_status} <- test_cases do
        # Mock Turnkey error with specific status code
        stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
          %{method: :post, url: "test", headers: [], body: ""}
        end)

        stub(Mock, :request, fn _payload ->
          {:error, turnkey_status, %{"message" => "Error #{turnkey_status}"}}
        end)

        params = %{
          "email" => "test@example.com",
          "stamped_body" => "test_stamped_body",
          "stamp" => "test_stamp"
        }

        conn = post(conn, ~p"/private/create_authenticators", params)

        assert conn.status == Plug.Conn.Status.code(expected_phoenix_status)

        assert json_response(conn, expected_phoenix_status) == %{
                 "error" => "Failed to create authenticator",
                 "message" => "External service error"
               }
      end
    end

    test "CheckUserExistence plug assigns user correctly", %{conn: conn} do
      # Create a test user using factory
      user = insert(:user, %{email: "test@example.com"})

      # Mock successful response
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_123"}}}
      end)

      params = %{
        "email" => user.email,
        "stamped_body" => "test_stamped_body",
        "stamp" => "test_stamp"
      }

      conn = post(conn, ~p"/private/create_authenticators", params)

      assert json_response(conn, 200) == %{"message" => "Authenticator created"}
    end
  end
end
