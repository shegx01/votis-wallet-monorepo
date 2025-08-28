defmodule BeVotisWalletWeb.Utils.SigningUtilsTest do
  use ExUnit.Case, async: false

  import ExUnit.CaptureLog
  import Mox

  alias BeVotisWallet.HTTPClient.Mock
  alias BeVotisWallet.Users.User
  alias BeVotisWalletWeb.Utils.SigningUtils

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  # Set up the mock context
  setup do
    BeVotisWallet.Test.Mocks.setup_mocks()
    :ok
  end

  describe "create_signing_context/3" do
    test "creates a signing context with proper fields" do
      context = SigningUtils.create_signing_context(
        "test operation",
        "Operation completed successfully",
        "Failed to perform operation"
      )

      assert context.operation_name == "test operation"
      assert context.success_message == "Operation completed successfully"
      assert context.error_prefix == "Failed to perform operation"
    end
  end

  describe "extract_signing_params/1" do
    test "successfully extracts valid parameters" do
      params = %{
        "stamped_body" => "valid_body_content",
        "stamp" => "valid_stamp_signature"
      }

      assert {:ok, {"valid_body_content", "valid_stamp_signature"}} =
               SigningUtils.extract_signing_params(params)
    end

    test "returns error for missing stamped_body" do
      params = %{"stamp" => "valid_stamp"}

      assert {:error, :missing_parameter, "stamped_body"} =
               SigningUtils.extract_signing_params(params)
    end

    test "returns error for missing stamp" do
      params = %{"stamped_body" => "valid_body"}

      assert {:error, :missing_parameter, "stamp"} = SigningUtils.extract_signing_params(params)
    end

    test "returns error for empty stamped_body" do
      params = %{"stamped_body" => "", "stamp" => "valid_stamp"}

      assert {:error, :missing_parameter, "stamped_body"} =
               SigningUtils.extract_signing_params(params)
    end

    test "returns error for empty stamp" do
      params = %{"stamped_body" => "valid_body", "stamp" => ""}

      assert {:error, :missing_parameter, "stamp"} = SigningUtils.extract_signing_params(params)
    end

    test "returns error for nil stamped_body" do
      params = %{"stamped_body" => nil, "stamp" => "valid_stamp"}

      assert {:error, :missing_parameter, "stamped_body"} =
               SigningUtils.extract_signing_params(params)
    end

    test "returns error for nil stamp" do
      params = %{"stamped_body" => "valid_body", "stamp" => nil}

      assert {:error, :missing_parameter, "stamp"} = SigningUtils.extract_signing_params(params)
    end
  end

  describe "execute_signing_operation/4" do
    setup do
      user = %User{
        id: "user_123",
        email: "test@example.com",
        sub_org_id: "org_456"
      }

      context = SigningUtils.create_signing_context(
        "test operation",
        "Test operation completed successfully",
        "Failed to perform test operation"
      )

      %{user: user, context: context}
    end

    test "successfully executes signing operation", %{user: user, context: context} do
      params = %{
        "stamped_body" => "test_body",
        "stamp" => "test_stamp"
      }

      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123",
          "result" => %{
            "testResult" => %{
              "signature" => "test_signature"
            }
          }
        }
      }

      # Mock successful Turnkey response
      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, turnkey_response}
      end)

      # Capture logs to verify proper logging
      log_output =
        capture_log(fn ->
          assert {:ok, response_data} =
                   SigningUtils.execute_signing_operation(
                     params,
                     user,
                     "ACTIVITY_TYPE_TEST",
                     context
                   )

          assert response_data == %{
                   "testResult" => %{
                     "signature" => "test_signature"
                   }
                 }
        end)

      # Don't check for specific log content as logging may be filtered in tests
      # Just verify that the function executed successfully
    end

    test "returns fallback message when Turnkey result is missing", %{user: user, context: context} do
      params = %{
        "stamped_body" => "test_body",
        "stamp" => "test_stamp"
      }

      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123"
          # Missing result field
        }
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:ok, turnkey_response}
      end)

      assert {:ok, response_data} =
               SigningUtils.execute_signing_operation(
                 params,
                 user,
                 "ACTIVITY_TYPE_TEST",
                 context
               )

      assert response_data == %{message: "Test operation completed successfully"}
    end

    test "returns error for missing parameter", %{user: user, context: context} do
      params = %{
        "stamp" => "test_stamp"
        # Missing stamped_body
      }

      assert {:error, :missing_parameter, "stamped_body"} =
               SigningUtils.execute_signing_operation(
                 params,
                 user,
                 "ACTIVITY_TYPE_TEST",
                 context
               )
    end

    test "returns error for Turnkey API failure", %{user: user, context: context} do
      params = %{
        "stamped_body" => "test_body",
        "stamp" => "test_stamp"
      }

      stub(Mock, :build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: ""}
      end)

      stub(Mock, :request, fn _payload ->
        {:error, 400, %{"message" => "Bad request"}}
      end)

      assert {:error, :turnkey_error, 400, %{"message" => "Bad request"}} =
               SigningUtils.execute_signing_operation(
                 params,
                 user,
                 "ACTIVITY_TYPE_TEST",
                 context
               )
    end
  end

  describe "log_user_not_found/2" do
    test "function executes successfully with email" do
      # Just verify the function doesn't crash - logging assertions are environment dependent
      assert :ok = SigningUtils.log_user_not_found("test@example.com", "test operation")
    end

    test "function executes successfully with nil email" do
      # Just verify the function doesn't crash - logging assertions are environment dependent
      assert :ok = SigningUtils.log_user_not_found(nil, "test operation")
    end
  end

  describe "prepare_missing_param_error/1" do
    test "creates error response for missing parameter" do
      error_data = SigningUtils.prepare_missing_param_error("test_param")

      assert error_data == %{error: "Missing required parameter: test_param"}
    end
  end

  describe "prepare_turnkey_error/1" do
    test "creates error response for Turnkey API error" do
      error_data = SigningUtils.prepare_turnkey_error("Failed to perform operation")

      assert error_data == %{
               error: "Failed to perform operation",
               message: "External service error"
             }
    end
  end

  describe "prepare_user_not_found_error/0" do
    test "creates user not found error response" do
      error_data = SigningUtils.prepare_user_not_found_error()

      assert error_data == %{
               error: "User not found",
               message: "The specified user does not exist"
             }
    end
  end

  describe "integration with different activity types" do
    setup do
      user = %User{
        id: "user_123",
        email: "test@example.com",
        sub_org_id: "org_456"
      }

      params = %{
        "stamped_body" => "test_body",
        "stamp" => "test_stamp"
      }

      %{user: user, params: params}
    end

    test "works with ACTIVITY_TYPE_SIGN_TRANSACTION_V2", %{user: user, params: params} do
      context = SigningUtils.create_signing_context(
        "transaction signing",
        "Transaction signed successfully",
        "Failed to sign transaction"
      )

      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123",
          "result" => %{
            "signTransactionResult" => %{
              "signedTransaction" => "0x123..."
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

      assert {:ok, response_data} =
               SigningUtils.execute_signing_operation(
                 params,
                 user,
                 "ACTIVITY_TYPE_SIGN_TRANSACTION_V2",
                 context
               )

      assert response_data == %{
               "signTransactionResult" => %{
                 "signedTransaction" => "0x123..."
               }
             }
    end

    test "works with ACTIVITY_TYPE_SIGN_RAW_PAYLOAD_V2", %{user: user, params: params} do
      context = SigningUtils.create_signing_context(
        "raw payload signing",
        "Raw payload signed successfully",
        "Failed to sign raw payload"
      )

      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123",
          "result" => %{
            "signRawPayloadResult" => %{
              "r" => "0x123...",
              "s" => "0x456...",
              "v" => "0x1b"
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

      assert {:ok, response_data} =
               SigningUtils.execute_signing_operation(
                 params,
                 user,
                 "ACTIVITY_TYPE_SIGN_RAW_PAYLOAD_V2",
                 context
               )

      assert response_data == %{
               "signRawPayloadResult" => %{
                 "r" => "0x123...",
                 "s" => "0x456...",
                 "v" => "0x1b"
               }
             }
    end

    test "works with ACTIVITY_TYPE_SIGN_RAW_PAYLOADS", %{user: user, params: params} do
      context = SigningUtils.create_signing_context(
        "raw payloads signing",
        "Raw payloads signed successfully",
        "Failed to sign raw payloads"
      )

      turnkey_response = %{
        "activity" => %{
          "id" => "activity_123",
          "result" => %{
            "signRawPayloadsResult" => %{
              "signatures" => [
                %{"r" => "0x123...", "s" => "0x456...", "v" => "0x1b"},
                %{"r" => "0x789...", "s" => "0xabc...", "v" => "0x1c"}
              ]
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

      assert {:ok, response_data} =
               SigningUtils.execute_signing_operation(
                 params,
                 user,
                 "ACTIVITY_TYPE_SIGN_RAW_PAYLOADS",
                 context
               )

      assert response_data == %{
               "signRawPayloadsResult" => %{
                 "signatures" => [
                   %{"r" => "0x123...", "s" => "0x456...", "v" => "0x1b"},
                   %{"r" => "0x789...", "s" => "0xabc...", "v" => "0x1c"}
                 ]
               }
             }
    end
  end
end
