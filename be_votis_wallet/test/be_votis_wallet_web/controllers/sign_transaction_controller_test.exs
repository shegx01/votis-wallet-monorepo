defmodule BeVotisWalletWeb.SignTransactionControllerTest do
  use BeVotisWalletWeb.ConnCase, async: false

  import Mox

  alias BeVotisWallet.HTTPClient.Mock

  setup :verify_on_exit!

  setup_all do
    %{
      test_user_attrs: build_test_user_attrs(),
      turnkey_responses: build_turnkey_responses(),
      request_params: build_request_params()
    }
  end

  setup do
    BeVotisWallet.Test.Mocks.setup_mocks()
    :ok
  end

  describe "POST /private/sign_transaction" do
    test "successfully signs transaction for existing user", context do
      %{conn: conn, test_user_attrs: users, turnkey_responses: responses, request_params: params} = context
      user = insert(:user, users.signer)

      setup_detailed_turnkey_mock(responses.success_with_details)

      conn = post_sign_transaction(conn, user, params.valid)
      response = json_response(conn, 200)

      assert %{
               "signTransactionResult" => %{
                 "signedTransaction" => "0x987654321fedcba...",
                 "transactionHash" => "0xabcdef123456789..."
               }
             } = response
    end

    test "handles non-standard Turnkey response structure gracefully", context do
      %{conn: conn, test_user_attrs: users, turnkey_responses: responses, request_params: params} = context
      user = insert(:user, users.custom)

      setup_simple_turnkey_mock(responses.success_custom)

      conn = post_sign_transaction(conn, user, params.custom)
      response = json_response(conn, 200)

      assert %{
               "customSignResult" => %{
                 "signature" => "custom_signature_data"
               }
             } = response
    end

    test "handles missing result in response with fallback message", context do
      %{conn: conn, test_user_attrs: users, turnkey_responses: responses, request_params: params} = context
      user = insert(:user, users.no_result)

      setup_simple_turnkey_mock(responses.success_no_result)

      conn = post_sign_transaction(conn, user, params.basic)
      response = json_response(conn, 200)
      assert %{"message" => "Transaction signed successfully"} = response
    end

    test "returns 400 for missing required parameters", context do
      %{conn: conn, test_user_attrs: users} = context
      user = insert(:user, users.valid)
      
      test_cases = build_invalid_param_test_cases(user.email)

      for %{params: params, expected_field: field} <- test_cases do
        conn = post(conn, ~p"/private/sign_transaction", params)

        response = json_response(conn, 400)
        assert %{"error" => "Missing required parameter: " <> ^field} = response
      end
    end

    test "returns 404 for non-existent user", context do
      %{conn: conn, request_params: params} = context
      
      conn =
        post(conn, ~p"/private/sign_transaction", params.nonexistent_user)

      response = json_response(conn, 404)

      assert %{
               "error" => "User not found",
               "message" => "The specified user does not exist"
             } = response
    end

    test "returns 401 for Turnkey authentication failure", context do
      %{conn: conn, test_user_attrs: users, request_params: params} = context
      user = insert(:user, users.auth_fail)

      setup_failed_turnkey_mock(401, %{"message" => "Invalid signature"})

      conn = post_sign_transaction(conn, user, params.invalid_auth)
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

  # Test data builders
  defp build_test_user_attrs do
    %{
      valid: %{email: "test@example.com", sub_org_id: "test_org_123"},
      signer: %{email: "signer@example.com", sub_org_id: "test_org_123"},
      custom: %{email: "custom@example.com", sub_org_id: "test_org_custom"},
      no_result: %{email: "noresult@example.com", sub_org_id: "test_org_noresult"},
      auth_fail: %{email: "authfail@example.com", sub_org_id: "test_org_auth_fail"},
      bad_request: %{email: "badreq@example.com", sub_org_id: "test_org_bad_request"},
      unprocessable: %{email: "unprocessable@example.com", sub_org_id: "test_org_422"},
      unknown_error: %{email: "unknown@example.com", sub_org_id: "test_org_unknown"},
      exists: %{email: "exists@example.com", sub_org_id: "test_org"}
    }
  end

  defp build_request_params do
    %{
      valid: %{
        stamped_body: "stamped_transaction_request_body",
        stamp: "transaction_signature_12345"
      },
      custom: %{
        stamped_body: "custom_body",
        stamp: "custom_stamp"
      },
      basic: %{
        stamped_body: "body",
        stamp: "stamp"
      },
      invalid_auth: %{
        stamped_body: "invalid_body",
        stamp: "invalid_stamp"
      },
      malformed: %{
        stamped_body: "malformed_body",
        stamp: "stamp"
      },
      invalid_format: %{
        stamped_body: "invalid_transaction_format",
        stamp: "stamp"
      },
      nonexistent_user: %{
        email: "nonexistent@example.com",
        stamped_body: "body",
        stamp: "stamp"
      },
      missing_user: %{
        email: "doesnotexist@example.com",
        stamped_body: "body",
        stamp: "stamp"
      }
    }
  end

  defp build_turnkey_responses do
    %{
      success_with_details: %{
        "activity" => %{
          "id" => "activity_sign_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "type" => "ACTIVITY_TYPE_SIGN_TRANSACTION_V2",
          "organizationId" => "test_org_123",
          "timestampMs" => "1746736509954",
          "result" => %{
            "signTransactionResult" => %{
              "signedTransaction" => "0x987654321fedcba...",
              "transactionHash" => "0xabcdef123456789..."
            }
          }
        }
      },
      success_custom: %{
        "activity" => %{
          "id" => "activity_custom",
          "result" => %{
            "customSignResult" => %{
              "signature" => "custom_signature_data"
            }
          }
        }
      },
      success_no_result: %{
        "activity" => %{
          "id" => "activity_no_result",
          "status" => "ACTIVITY_STATUS_COMPLETED"
        }
      },
      success_basic: %{
        "activity" => %{
          "id" => "test_activity",
          "result" => %{"signTransactionResult" => %{"signedTransaction" => "0x123"}}
        }
      }
    }
  end

  defp setup_simple_turnkey_mock(response) do
    stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
      %{method: :post, url: "test", headers: [], body: ""}
    end)

    stub(Mock, :request, fn _payload -> {:ok, response} end)
  end

  defp setup_detailed_turnkey_mock(response) do
    stub(Mock, :build_payload, fn method, url, headers, body ->
      assert method == :post
      assert String.contains?(url, "/public/v1/submit/sign_transaction")

      assert Enum.any?(headers, fn
               {"X-Stamp", _} -> true
               _ -> false
             end)

      %{method: method, url: url, headers: headers, body: body}
    end)

    stub(Mock, :request, fn _payload -> {:ok, response} end)
  end

  defp setup_failed_turnkey_mock(status_code, error_message) do
    stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
      %{method: :post, url: "test", headers: [], body: ""}
    end)

    stub(Mock, :request, fn _payload -> {:error, status_code, error_message} end)
  end

  defp post_sign_transaction(conn, user, params) do
    post(conn, ~p"/private/sign_transaction", %{
      "email" => user.email,
      "stamped_body" => params.stamped_body,
      "stamp" => params.stamp
    })
  end

  defp build_invalid_param_test_cases(email) do
    [
      %{
        params: %{"email" => email, "stamp" => "stamp"},
        expected_field: "stamped_body"
      },
      %{
        params: %{"email" => email, "stamped_body" => "body"},
        expected_field: "stamp"
      },
      %{
        params: %{"email" => email, "stamped_body" => "", "stamp" => "stamp"},
        expected_field: "stamped_body"
      },
      %{
        params: %{"email" => email, "stamped_body" => "body", "stamp" => ""},
        expected_field: "stamp"
      }
    ]
  end
end
