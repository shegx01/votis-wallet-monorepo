defmodule BeVotisWalletWeb.SignUpControllerTest do
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

  describe "POST /private/sign_up" do
    test "returns 200 with base64 encoded org_id for existing user", %{conn: conn} do
      user = insert(:user)

      conn =
        post(conn, ~p"/private/sign_up", %{
          email: user.email,
          stamped_body: "dummy_body",
          stamp: "dummy_stamp",
          sub_organization_name: "Test Org"
        })

      assert json_response(conn, 200) == %{
               "org_id" => Base.encode64(user.sub_org_id)
             }
    end

    test "successfully creates new user with valid Turnkey response", %{conn: conn} do
      # Mock successful Turnkey response
      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "timestampMs" => "1234567890",
          "result" => %{
            "activity" => %{
              "result" => %{
                "createSubOrganizationResultV7" => %{
                  "subOrganizationId" => "sub_org_456",
                  "rootUserIds" => ["root_user_789"],
                  "wallet" => %{
                    "walletId" => "wallet_abc"
                  }
                }
              }
            }
          }
        }
      }

      # Set up Turnkey client mock
      stub(Mock, :build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/create_sub_organization")

        # Verify client signature headers
        assert Enum.any?(headers, fn
                 {"X-Stamp-WebAuthn", _} -> true
                 _ -> false
               end)

        %{method: method, url: url, headers: headers, body: body}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, turnkey_response}
      end)

      email = "newuser@example.com"

      conn =
        post(conn, ~p"/private/sign_up", %{
          email: email,
          stamped_body: "valid_stamped_body",
          stamp: "valid_client_signature",
          sub_organization_name: "New Test Organization",
          authenticator_name: "test_authenticator"
        })

      response = json_response(conn, 201)
      assert %{"org_id" => org_id} = response
      assert is_binary(org_id)

      # Verify user was created in database
      {:ok, user} = BeVotisWallet.Users.User.get_by_email(email)
      assert user.email == email
      assert user.sub_org_id == "sub_org_456"
      assert user.sub_organization_name == "New Test Organization"
      assert user.authenticator_name == "test_authenticator"
      assert user.wallet_id == "wallet_abc"
      assert user.root_user_ids == ["root_user_789"]
    end

    test "returns 400 for missing required parameters", %{conn: base_conn} do
      test_cases = [
        %{
          params: %{
            # missing email
            stamped_body: "body",
            stamp: "stamp",
            sub_organization_name: "org"
          },
          expected_error: "Missing required parameter: email"
        },
        %{
          params: %{
            email: "test@example.com"
            # missing stamped_body, stamp, sub_organization_name
          },
          expected_error: "Missing required parameter: stamped_body"
        },
        %{
          params: %{
            email: "test@example.com",
            stamped_body: "body"
            # missing stamp, sub_organization_name
          },
          expected_error: "Missing required parameter: stamp"
        },
        %{
          params: %{
            email: "test@example.com",
            stamped_body: "body",
            stamp: "stamp"
            # missing sub_organization_name
          },
          expected_error: "Missing required parameter: sub_organization_name"
        }
      ]

      for %{params: params, expected_error: expected_error} <- test_cases do
        conn = post(base_conn, ~p"/private/sign_up", params)

        assert json_response(conn, 400) == %{
                 "error" => expected_error
               }
      end
    end

    test "returns 400 for empty required parameters", %{conn: conn} do
      conn =
        post(conn, ~p"/private/sign_up", %{
          email: "",
          stamped_body: "body",
          stamp: "stamp",
          sub_organization_name: "org"
        })

      assert json_response(conn, 400) == %{
               "error" => "Missing required parameter: email"
             }
    end

    test "handles optional authenticator_name parameter", %{conn: conn} do
      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "timestampMs" => "1234567890",
          "result" => %{
            "activity" => %{
              "result" => %{
                "createSubOrganizationResultV7" => %{
                  "subOrganizationId" => "sub_org_456",
                  "rootUserIds" => ["root_user_789"],
                  "wallet" => %{
                    "walletId" => "wallet_abc"
                  }
                }
              }
            }
          }
        }
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, turnkey_response}
      end)

      # Test without authenticator_name
      conn =
        post(conn, ~p"/private/sign_up", %{
          email: "test@example.com",
          stamped_body: "body",
          stamp: "stamp",
          sub_organization_name: "org"
        })

      assert json_response(conn, 201)

      # Verify user was created with nil authenticator_name
      {:ok, user} = BeVotisWallet.Users.User.get_by_email("test@example.com")
      assert user.authenticator_name == nil
    end

    test "returns 502 for Turnkey API errors", %{conn: conn} do
      # Mock Turnkey API error
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 400, %{"message" => "Invalid request"}}
      end)

      conn =
        post(conn, ~p"/private/sign_up", %{
          email: "test@example.com",
          stamped_body: "invalid_body",
          stamp: "invalid_stamp",
          sub_organization_name: "Test Org"
        })

      assert json_response(conn, 502) == %{
               "error" => "External service error"
             }
    end

    test "returns 502 for invalid Turnkey response", %{conn: conn} do
      # Mock invalid Turnkey response
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, %{"invalid" => "response"}}
      end)

      conn =
        post(conn, ~p"/private/sign_up", %{
          email: "test@example.com",
          stamped_body: "valid_body",
          stamp: "valid_stamp",
          sub_organization_name: "Test Org"
        })

      assert json_response(conn, 502) == %{
               "error" => "Invalid response from external service"
             }
    end

    test "returns 200 for existing users (no database error)", %{conn: conn} do
      # Create a user first - this should return the existing user's org_id, not cause an error
      existing_user = insert(:user, %{email: "test@example.com"})

      conn =
        post(conn, ~p"/private/sign_up", %{
          email: "test@example.com",
          stamped_body: "valid_body",
          stamp: "valid_stamp",
          sub_organization_name: "Test Org"
        })

      # Should return existing user's org_id, not attempt to create duplicate
      assert json_response(conn, 200) == %{
               "org_id" => Base.encode64(existing_user.sub_org_id)
             }
    end

    test "handles case insensitive email lookup for existing users", %{conn: conn} do
      user = insert(:user, %{email: "test@example.com"})

      conn =
        post(conn, ~p"/private/sign_up", %{
          email: "TEST@EXAMPLE.COM",
          stamped_body: "dummy_body",
          stamp: "dummy_stamp",
          sub_organization_name: "Test Org"
        })

      assert json_response(conn, 200) == %{
               "org_id" => Base.encode64(user.sub_org_id)
             }
    end

    test "normalizes email to lowercase for new users", %{conn: conn} do
      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "timestampMs" => "1234567890",
          "result" => %{
            "activity" => %{
              "result" => %{
                "createSubOrganizationResultV7" => %{
                  "subOrganizationId" => "sub_org_456",
                  "rootUserIds" => ["root_user_789"],
                  "wallet" => %{
                    "walletId" => "wallet_abc"
                  }
                }
              }
            }
          }
        }
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, turnkey_response}
      end)

      conn =
        post(conn, ~p"/private/sign_up", %{
          email: "TEST@EXAMPLE.COM",
          stamped_body: "valid_body",
          stamp: "valid_stamp",
          sub_organization_name: "Test Org"
        })

      assert json_response(conn, 201)

      # Verify email was stored in lowercase
      {:ok, user} = BeVotisWallet.Users.User.get_by_email("test@example.com")
      assert user.email == "test@example.com"
    end
  end
end
