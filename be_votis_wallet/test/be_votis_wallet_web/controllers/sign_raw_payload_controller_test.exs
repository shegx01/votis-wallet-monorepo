defmodule BeVotisWalletWeb.SignRawPayloadControllerTest do
  use BeVotisWalletWeb.ConnCase, async: false

  import Mox

  alias BeVotisWallet.HTTPClient.Mock

  setup :verify_on_exit!

  setup_all do
    %{
      user_attrs: build_test_user_attrs(),
      turnkey_responses: build_turnkey_responses(),
      request_params: build_request_params()
    }
  end

  setup %{user_attrs: user_attrs} do
    BeVotisWallet.Test.Mocks.setup_mocks()
    
    # Create actual users in database based on the user_attrs
    users = %{
      rawsigner: insert(:user, user_attrs.rawsigner),
      custom: insert(:user, user_attrs.custom),
      no_result: insert(:user, user_attrs.no_result),
      valid: insert(:user, user_attrs.valid),
      auth_fail: insert(:user, user_attrs.auth_fail),
      bad_request: insert(:user, user_attrs.bad_request),
      unprocessable: insert(:user, user_attrs.unprocessable),
      unknown_error: insert(:user, user_attrs.unknown_error),
      exists: insert(:user, user_attrs.exists)
    }
    
    on_exit(fn ->
      # Clean up created users
      Enum.each(users, fn {_key, user} ->
        BeVotisWallet.Repo.delete(user)
      end)
    end)
    
    %{users: users}
  end

  describe "POST /private/sign_raw_payload" do
    test "successfully signs raw payload for existing user", context do
      %{conn: conn, users: users, turnkey_responses: responses, request_params: params} = context

      setup_detailed_turnkey_mock(responses.success_with_details)

      conn = post_sign_raw_payload(conn, users.rawsigner, params.valid)
      response = json_response(conn, 200)

      assert %{
               "signRawPayloadResult" => %{
                 "r" => "0x123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef01",
                 "s" => "0x23456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef012",
                 "v" => "0x1b"
               }
             } = response
    end

    test "handles non-standard Turnkey response structure gracefully", context do
      %{conn: conn, users: users, turnkey_responses: responses, request_params: params} = context

      setup_simple_turnkey_mock(responses.success_custom)

      conn = post_sign_raw_payload(conn, users.custom, params.custom)
      response = json_response(conn, 200)

      # Should return the custom result structure
      assert %{
               "customRawSignResult" => %{
                 "data" => "custom_signature_data"
               }
             } = response
    end

    test "handles missing result in response with fallback message", context do
      %{conn: conn, users: users, turnkey_responses: responses, request_params: params} = context

      setup_simple_turnkey_mock(responses.success_no_result)

      conn = post_sign_raw_payload(conn, users.no_result, params.basic)
      response = json_response(conn, 200)
      assert %{"message" => "Raw payload signed successfully"} = response
    end

    test "returns 400 for missing required parameters", context do
      %{conn: conn, users: users} = context
      
      test_cases = build_invalid_param_test_cases(users.valid.email)

      for %{params: params, expected_field: field} <- test_cases do
        conn = post(conn, ~p"/private/sign_raw_payload", params)

        response = json_response(conn, 400)
        assert %{"error" => "Missing required parameter: " <> ^field} = response
      end
    end

    test "returns 404 for non-existent user", context do
      %{conn: conn, request_params: params} = context
      
      conn =
        post(conn, ~p"/private/sign_raw_payload", params.nonexistent_user)

      response = json_response(conn, 404)

      assert %{
               "error" => "User not found",
               "message" => "The specified user does not exist"
             } = response
    end

    test "returns 401 for Turnkey authentication failure", context do
      %{conn: conn, users: users, request_params: params} = context

      setup_failed_turnkey_mock(401, %{"message" => "Invalid signature"})

      conn = post_sign_raw_payload(conn, users.auth_fail, params.invalid_auth)
      response = json_response(conn, 401)

      assert %{
               "error" => "Failed to sign raw payload",
               "message" => "External service error"
             } = response
    end

    test "returns 400 for Turnkey bad request", context do
      %{conn: conn, users: users, request_params: params} = context

      setup_failed_turnkey_mock(400, %{"message" => "Malformed payload data"})

      conn = post_sign_raw_payload(conn, users.bad_request, params.malformed)
      response = json_response(conn, 400)

      assert %{
               "error" => "Failed to sign raw payload",
               "message" => "External service error"
             } = response
    end

    test "returns 422 for Turnkey unprocessable entity", context do
      %{conn: conn, users: users, request_params: params} = context

      setup_failed_turnkey_mock(422, %{"message" => "Invalid payload format"})

      conn = post_sign_raw_payload(conn, users.unprocessable, params.invalid_format)
      response = json_response(conn, 422)

      assert %{
               "error" => "Failed to sign raw payload",
               "message" => "External service error"
             } = response
    end

    test "returns 500 for unknown Turnkey error codes", context do
      %{conn: conn, users: users, request_params: params} = context

      setup_failed_turnkey_mock(999, %{"message" => "Unknown error"})

      conn = post_sign_raw_payload(conn, users.unknown_error, params.basic)
      response = json_response(conn, 500)

      assert %{
               "error" => "Failed to sign raw payload",
               "message" => "External service error"
             } = response
    end
  end

  describe "CheckUserExistence plug integration" do
    test "properly sets user in conn assigns for existing user", context do
      %{conn: conn, users: users, turnkey_responses: responses} = context

      setup_simple_turnkey_mock(responses.success_basic)

      conn =
        post(conn, ~p"/private/sign_raw_payload", %{
          "email" => users.exists.email,
          "stamped_body" => "body",
          "stamp" => "stamp"
        })

      # Should succeed because user exists
      assert json_response(conn, 200)
    end

    test "handles non-existent user gracefully", %{conn: conn} do
      conn =
        post(conn, ~p"/private/sign_raw_payload", %{
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
      rawsigner: %{email: "rawsigner@example.com", sub_org_id: "test_org_123"},
      custom: %{email: "custom@example.com", sub_org_id: "test_org_custom"},
      no_result: %{email: "noresult@example.com", sub_org_id: "test_org_noresult"},
      valid: %{email: "test@example.com", sub_org_id: "test_org"},
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
        stamped_body: "stamped_raw_payload_request_body",
        stamp: "raw_payload_signature_12345"
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
        stamped_body: "invalid_payload_format",
        stamp: "stamp"
      },
      nonexistent_user: %{
        email: "nonexistent@example.com",
        stamped_body: "body",
        stamp: "stamp"
      }
    }
  end

  defp build_turnkey_responses do
    %{
      success_with_details: %{
        "activity" => %{
          "id" => "activity_raw_sign_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "type" => "ACTIVITY_TYPE_SIGN_RAW_PAYLOAD_V2",
          "organizationId" => "test_org_123",
          "timestampMs" => "1746736509954",
          "result" => %{
            "signRawPayloadResult" => %{
              "r" => "0x123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef01",
              "s" => "0x23456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef012",
              "v" => "0x1b"
            }
          }
        }
      },
      success_custom: %{
        "activity" => %{
          "id" => "activity_custom",
          "result" => %{
            "customRawSignResult" => %{
              "data" => "custom_signature_data"
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
          "result" => %{
            "signRawPayloadResult" => %{
              "r" => "0x123",
              "s" => "0x456",
              "v" => "0x1b"
            }
          }
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
      assert String.contains?(url, "/public/v1/submit/sign_raw_payload")

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

  defp post_sign_raw_payload(conn, user, params) do
    post(conn, ~p"/private/sign_raw_payload", %{
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
