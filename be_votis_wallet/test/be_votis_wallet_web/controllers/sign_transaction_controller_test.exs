defmodule BeVotisWalletWeb.SignTransactionControllerTest do
  use BeVotisWalletWeb.ConnCase, async: false

  import Mox

  alias BeVotisWallet.HTTPClient.Mock

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  # Set up the mock context
  setup do
    BeVotisWallet.Test.Mocks.setup_mocks()
    :ok
  end

  describe "POST /private/sign_transaction" do
    test "successfully signs transaction for existing user", %{conn: conn} do
      # Create a user in the database
      user = insert(:user, %{email: "signer@example.com", sub_org_id: "test_org_123"})

      # Mock successful transaction signing response
      signing_response = %{
        "activity" => %{
          "id" => "activity_sign_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "type" => "ACTIVITY_TYPE_SIGN_TRANSACTION_V2",
          "organizationId" => user.sub_org_id,
          "timestampMs" => "1746736509954",
          "result" => %{
            "signTransactionResult" => %{
              "signedTransaction" => "0x987654321fedcba...",
              "transactionHash" => "0xabcdef123456789..."
            }
          }
        }
      }

      # Set up Turnkey client mock
      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/sign_transaction")

        # Verify API key signature headers
        assert Enum.any?(headers, fn
                 {"X-Stamp", _} -> true
                 _ -> false
               end)

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, signing_response}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "stamped_transaction_request_body",
          "stamp" => "transaction_signature_12345"
        })

      response = json_response(conn, 200)

      assert %{
               "signTransactionResult" => %{
                 "signedTransaction" => "0x987654321fedcba...",
                 "transactionHash" => "0xabcdef123456789..."
               }
             } = response
    end

    test "handles non-standard Turnkey response structure gracefully", %{conn: conn} do
      user = insert(:user, %{email: "custom@example.com", sub_org_id: "test_org_custom"})

      # Mock response with unexpected structure
      custom_response = %{
        "activity" => %{
          "id" => "activity_custom",
          "result" => %{
            "customSignResult" => %{
              "signature" => "custom_signature_data"
            }
          }
        }
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, custom_response}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "custom_body",
          "stamp" => "custom_stamp"
        })

      response = json_response(conn, 200)

      # Should return the custom result structure
      assert %{
               "customSignResult" => %{
                 "signature" => "custom_signature_data"
               }
             } = response
    end

    test "handles missing result in response with fallback message", %{conn: conn} do
      user = insert(:user, %{email: "noresult@example.com", sub_org_id: "test_org_noresult"})

      # Mock response without result field
      no_result_response = %{
        "activity" => %{
          "id" => "activity_no_result",
          "status" => "ACTIVITY_STATUS_COMPLETED"
        }
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, no_result_response}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "body",
          "stamp" => "stamp"
        })

      response = json_response(conn, 200)
      assert %{"message" => "Transaction signed successfully"} = response
    end

    test "returns 400 for missing required parameters", %{conn: base_conn} do
      user = insert(:user, %{email: "test@example.com", sub_org_id: "test_org"})

      test_cases = [
        %{
          params: %{
            "email" => user.email,
            "stamp" => "stamp"
            # missing stamped_body
          },
          expected_field: "stamped_body"
        },
        %{
          params: %{
            "email" => user.email,
            "stamped_body" => "body"
            # missing stamp
          },
          expected_field: "stamp"
        },
        %{
          params: %{
            "email" => user.email,
            "stamped_body" => "",
            "stamp" => "stamp"
            # empty stamped_body
          },
          expected_field: "stamped_body"
        },
        %{
          params: %{
            "email" => user.email,
            "stamped_body" => "body",
            "stamp" => ""
            # empty stamp
          },
          expected_field: "stamp"
        }
      ]

      for %{params: params, expected_field: field} <- test_cases do
        conn = post(base_conn, ~p"/private/sign_transaction", params)

        response = json_response(conn, 400)
        assert %{"error" => "Missing required parameter: " <> ^field} = response
      end
    end

    test "returns 404 for non-existent user", %{conn: conn} do
      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => "nonexistent@example.com",
          "stamped_body" => "body",
          "stamp" => "stamp"
        })

      response = json_response(conn, 404)

      assert %{
               "error" => "User not found",
               "message" => "The specified user does not exist"
             } = response
    end

    test "returns 401 for Turnkey authentication failure", %{conn: conn} do
      user = insert(:user, %{email: "authfail@example.com", sub_org_id: "test_org_auth_fail"})

      # Mock Turnkey authentication failure
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 401, %{"message" => "Invalid signature"}}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "invalid_body",
          "stamp" => "invalid_stamp"
        })

      response = json_response(conn, 401)

      assert %{
               "error" => "Failed to sign transaction",
               "message" => "External service error"
             } = response
    end

    test "returns 400 for Turnkey bad request", %{conn: conn} do
      user = insert(:user, %{email: "badreq@example.com", sub_org_id: "test_org_bad_request"})

      # Mock Turnkey bad request
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 400, %{"message" => "Malformed transaction data"}}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "malformed_body",
          "stamp" => "stamp"
        })

      response = json_response(conn, 400)

      assert %{
               "error" => "Failed to sign transaction",
               "message" => "External service error"
             } = response
    end

    test "returns 422 for Turnkey unprocessable entity", %{conn: conn} do
      user = insert(:user, %{email: "unprocessable@example.com", sub_org_id: "test_org_422"})

      # Mock Turnkey unprocessable entity (invalid transaction format)
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 422, %{"message" => "Invalid transaction format"}}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "invalid_transaction_format",
          "stamp" => "stamp"
        })

      response = json_response(conn, 422)

      assert %{
               "error" => "Failed to sign transaction",
               "message" => "External service error"
             } = response
    end

    test "returns 500 for unknown Turnkey error codes", %{conn: conn} do
      user = insert(:user, %{email: "unknown@example.com", sub_org_id: "test_org_unknown"})

      # Mock unknown error code
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 999, %{"message" => "Unknown error"}}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "body",
          "stamp" => "stamp"
        })

      response = json_response(conn, 500)

      assert %{
               "error" => "Failed to sign transaction",
               "message" => "External service error"
             } = response
    end
  end

  describe "CheckUserExistence plug integration" do
    test "properly sets user in conn assigns for existing user", %{conn: conn} do
      user = insert(:user, %{email: "exists@example.com", sub_org_id: "test_org"})

      # Mock successful response
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok,
         %{
           "activity" => %{
             "id" => "test_activity",
             "result" => %{"signTransactionResult" => %{"signedTransaction" => "0x123"}}
           }
         }}
      end)

      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => user.email,
          "stamped_body" => "body",
          "stamp" => "stamp"
        })

      # Should succeed because user exists
      assert json_response(conn, 200)
    end

    test "handles non-existent user gracefully", %{conn: conn} do
      conn =
        post(conn, ~p"/private/sign_transaction", %{
          "email" => "doesnotexist@example.com",
          "stamped_body" => "body",
          "stamp" => "stamp"
        })

      # Should return 404 without calling Turnkey
      response = json_response(conn, 404)
      assert %{"error" => "User not found"} = response
    end
  end
end
