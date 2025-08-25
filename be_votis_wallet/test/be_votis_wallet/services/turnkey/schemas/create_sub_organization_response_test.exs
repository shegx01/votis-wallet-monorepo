defmodule BeVotisWallet.Services.Turnkey.Schemas.CreateSubOrganizationResponseTest do
  use BeVotisWallet.DataCase

  alias BeVotisWallet.Services.Turnkey.Schemas.CreateSubOrganizationResponse

  @valid_turnkey_response %{
    "activity" => %{
      "id" => "01fc1cc2-7e61-4f8b-9c43-1c1b95c95d1b",
      "status" => "ACTIVITY_STATUS_COMPLETED",
      "type" => "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7",
      "organizationId" => "7c28571e-8776-4cb0-a135-0f4a5bef2c48",
      "timestampMs" => "1746736509954",
      "result" => %{
        "activity" => %{
          "id" => "01fc1cc2-7e61-4f8b-9c43-1c1b95c95d1b",
          "organizationId" => "7c28571e-8776-4cb0-a135-0f4a5bef2c48",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "type" => "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7",
          "result" => %{
            "createSubOrganizationResultV7" => %{
              "subOrganizationId" => "41424344-4546-4748-494a-4b4c4d4e4f50",
              "wallet" => %{
                "walletId" => "51525354-5556-5758-595a-616263646566",
                "addresses" => ["0x1234567890123456789012345678901234567890"]
              },
              "rootUserIds" => ["67686970-7172-7374-7576-777879808182"]
            }
          }
        }
      }
    }
  }

  @minimal_valid_response %{
    "activity" => %{
      "id" => "activity_123",
      "status" => "ACTIVITY_STATUS_COMPLETED",
      "timestampMs" => "1234567890",
      "result" => %{
        "activity" => %{
          "result" => %{
            "createSubOrganizationResultV7" => %{
              "subOrganizationId" => "sub_org_456"
            }
          }
        }
      }
    }
  }

  describe "new/1" do
    test "successfully validates a complete valid response" do
      assert {:ok, response_struct} = CreateSubOrganizationResponse.new(@valid_turnkey_response)
      
      assert response_struct.activity.id == "01fc1cc2-7e61-4f8b-9c43-1c1b95c95d1b"
      assert response_struct.activity.status == "ACTIVITY_STATUS_COMPLETED"
      assert response_struct.activity.type == "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7"
    end

    test "validates required activity fields" do
      invalid_response = %{"activity" => %{}}
      
      assert {:error, {:malformed_params, error_message, _}} = CreateSubOrganizationResponse.new(invalid_response)
      assert String.contains?(error_message, "id")
      assert String.contains?(error_message, "status")
    end

    test "validates activity status enum" do
      invalid_response = put_in(@minimal_valid_response, ["activity", "status"], "INVALID_STATUS")
      
      assert {:error, {:malformed_params, error_message, _}} = CreateSubOrganizationResponse.new(invalid_response)
      assert String.contains?(error_message, "is invalid")
    end

    test "validates activity type enum" do
      invalid_response = put_in(@valid_turnkey_response, ["activity", "type"], "INVALID_TYPE")
      
      assert {:error, {:malformed_params, error_message, _}} = CreateSubOrganizationResponse.new(invalid_response)
      assert String.contains?(error_message, "is invalid")
    end

    test "handles missing activity" do
      invalid_response = %{}
      
      assert {:error, {:malformed_params, _error_message, _}} = CreateSubOrganizationResponse.new(invalid_response)
    end
  end

  describe "parse_response/1" do
    test "successfully parses a complete valid response" do
      assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(@valid_turnkey_response)
      
      assert user_data.sub_org_id == "41424344-4546-4748-494a-4b4c4d4e4f50"
      assert user_data.activity_id == "01fc1cc2-7e61-4f8b-9c43-1c1b95c95d1b"
      assert user_data.root_user_ids == ["67686970-7172-7374-7576-777879808182"]
      assert user_data.wallet_id == "51525354-5556-5758-595a-616263646566"
      assert user_data.timestamp_ms == "1746736509954"
      assert user_data.status == "ACTIVITY_STATUS_COMPLETED"
    end

    test "successfully parses minimal valid response" do
      assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(@minimal_valid_response)
      
      assert user_data.sub_org_id == "sub_org_456"
      assert user_data.activity_id == "activity_123"
      assert user_data.root_user_ids == []
      assert is_nil(user_data.wallet_id)
      assert user_data.timestamp_ms == "1234567890"
      assert user_data.status == "ACTIVITY_STATUS_COMPLETED"
    end

    test "handles response with no wallet" do
      response_without_wallet = put_in(@valid_turnkey_response, 
        ["activity", "result", "activity", "result", "createSubOrganizationResultV7", "wallet"], 
        nil
      )
      
      assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(response_without_wallet)
      
      assert user_data.sub_org_id == "41424344-4546-4748-494a-4b4c4d4e4f50"
      assert user_data.activity_id == "01fc1cc2-7e61-4f8b-9c43-1c1b95c95d1b"
      assert is_nil(user_data.wallet_id)
      assert user_data.root_user_ids == ["67686970-7172-7374-7576-777879808182"]
    end

    test "handles response with empty root_user_ids" do
      response_without_root_users = put_in(@valid_turnkey_response, 
        ["activity", "result", "activity", "result", "createSubOrganizationResultV7", "rootUserIds"], 
        []
      )
      
      assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(response_without_root_users)
      
      assert user_data.root_user_ids == []
      assert user_data.sub_org_id == "41424344-4546-4748-494a-4b4c4d4e4f50"
    end

    test "handles response with missing root_user_ids" do
      response_without_root_users = 
        @valid_turnkey_response
        |> put_in(
          ["activity", "result", "activity", "result", "createSubOrganizationResultV7"], 
          Map.delete(@valid_turnkey_response["activity"]["result"]["activity"]["result"]["createSubOrganizationResultV7"], "rootUserIds")
        )
      
      assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(response_without_root_users)
      
      assert user_data.root_user_ids == []
      assert user_data.sub_org_id == "41424344-4546-4748-494a-4b4c4d4e4f50"
    end

    test "validates activity status values" do
      valid_statuses = [
        "ACTIVITY_STATUS_COMPLETED",
        "ACTIVITY_STATUS_PENDING",
        "ACTIVITY_STATUS_FAILED"
      ]

      for status <- valid_statuses do
        response = put_in(@minimal_valid_response, ["activity", "status"], status)
        assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(response)
        assert user_data.status == status
      end
    end

    test "rejects invalid activity status" do
      response = put_in(@minimal_valid_response, ["activity", "status"], "ACTIVITY_STATUS_INVALID")
      assert {:error, error_message} = CreateSubOrganizationResponse.parse_response(response)
      assert String.contains?(error_message, "is invalid")
    end

    test "returns error for missing activity id" do
      invalid_response = 
        @minimal_valid_response
        |> put_in(["activity"], Map.delete(@minimal_valid_response["activity"], "id"))
      
      assert {:error, error_message} = CreateSubOrganizationResponse.parse_response(invalid_response)
      assert String.contains?(error_message, "id")
    end

    test "returns error for missing activity status" do
      invalid_response = 
        @minimal_valid_response
        |> put_in(["activity"], Map.delete(@minimal_valid_response["activity"], "status"))
      
      assert {:error, error_message} = CreateSubOrganizationResponse.parse_response(invalid_response)
      assert String.contains?(error_message, "status")
    end

    test "returns error for missing sub organization id in V7 format" do
      invalid_response = 
        @minimal_valid_response
        |> put_in(
          ["activity", "result", "activity", "result", "createSubOrganizationResultV7"], 
          Map.delete(@minimal_valid_response["activity"]["result"]["activity"]["result"]["createSubOrganizationResultV7"], "subOrganizationId")
        )
      
      assert {:error, error_message} = CreateSubOrganizationResponse.parse_response(invalid_response)
      assert String.contains?(error_message, "sub_organization_id")
    end

    test "returns error for completely invalid response structure" do
      invalid_response = %{"invalid" => "structure"}
      
      assert {:error, _error_message} = CreateSubOrganizationResponse.parse_response(invalid_response)
    end

    test "handles fallback to older format when V7 is not available" do
      # Simulate older response format 
      older_response = %{
        "activity" => %{
          "id" => "activity_123",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "type" => "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION",
          "timestampMs" => "1234567890",
          "result" => %{
            "activity" => %{
              "result" => %{
                "createSubOrganizationResult" => %{
                  "organizationId" => "old_org_456"
                }
              }
            }
          }
        }
      }
      
      assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(older_response)
      
      assert user_data.sub_org_id == "old_org_456"
      assert user_data.activity_id == "activity_123"
      assert user_data.root_user_ids == []
      assert is_nil(user_data.wallet_id)
    end
  end

  describe "extract_user_data/1" do
    test "extracts all relevant user data from parsed response" do
      {:ok, response_struct} = CreateSubOrganizationResponse.new(@valid_turnkey_response)
      
      user_data = CreateSubOrganizationResponse.extract_user_data(response_struct)
      
      assert user_data.sub_org_id == "41424344-4546-4748-494a-4b4c4d4e4f50"
      assert user_data.activity_id == "01fc1cc2-7e61-4f8b-9c43-1c1b95c95d1b"
      assert user_data.root_user_ids == ["67686970-7172-7374-7576-777879808182"]
      assert user_data.wallet_id == "51525354-5556-5758-595a-616263646566"
      assert user_data.timestamp_ms == "1746736509954"
      assert user_data.status == "ACTIVITY_STATUS_COMPLETED"
    end

    test "handles missing wallet gracefully" do
      response_without_wallet = 
        @valid_turnkey_response
        |> put_in(
          ["activity", "result", "activity", "result", "createSubOrganizationResultV7", "wallet"], 
          nil
        )
      
      {:ok, response_struct} = CreateSubOrganizationResponse.new(response_without_wallet)
      
      user_data = CreateSubOrganizationResponse.extract_user_data(response_struct)
      
      assert is_nil(user_data.wallet_id)
      assert user_data.sub_org_id == "41424344-4546-4748-494a-4b4c4d4e4f50"
    end
  end

  describe "integration with actual Turnkey API response format" do
    test "parses the exact V7 response structure" do
      # Based on the actual Turnkey API documentation for V7
      v7_response = %{
        "activity" => %{
          "id" => "real-activity-id-uuid",
          "status" => "ACTIVITY_STATUS_COMPLETED",
          "type" => "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7",
          "organizationId" => "parent-org-id-uuid",
          "timestampMs" => "1746736509954",
          "result" => %{
            "activity" => %{
              "id" => "real-activity-id-uuid",
              "organizationId" => "parent-org-id-uuid",
              "status" => "ACTIVITY_STATUS_COMPLETED",
              "type" => "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7",
              "result" => %{
                "createSubOrganizationResultV7" => %{
                  "subOrganizationId" => "new-sub-org-uuid",
                  "wallet" => %{
                    "walletId" => "new-wallet-uuid",
                    "addresses" => ["0xabcdef1234567890abcdef1234567890abcdef12"]
                  },
                  "rootUserIds" => ["root-user-uuid-1", "root-user-uuid-2"]
                }
              }
            }
          }
        }
      }

      assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(v7_response)
      
      assert user_data.sub_org_id == "new-sub-org-uuid"
      assert user_data.activity_id == "real-activity-id-uuid"
      assert user_data.root_user_ids == ["root-user-uuid-1", "root-user-uuid-2"]
      assert user_data.wallet_id == "new-wallet-uuid"
      assert user_data.timestamp_ms == "1746736509954"
      assert user_data.status == "ACTIVITY_STATUS_COMPLETED"
    end

    test "handles response variations gracefully" do
      # Test with different combinations of optional fields
      test_cases = [
        # No wallet
        {
          put_in(@valid_turnkey_response, ["activity", "result", "activity", "result", "createSubOrganizationResultV7", "wallet"], nil),
          fn user_data -> assert is_nil(user_data.wallet_id) end
        },
        # No timestamp
        {
          Map.delete(@valid_turnkey_response["activity"], "timestampMs") |> then(&put_in(@valid_turnkey_response, ["activity"], &1)),
          fn user_data -> assert is_nil(user_data.timestamp_ms) end
        },
        # Different status
        {
          put_in(@valid_turnkey_response, ["activity", "status"], "ACTIVITY_STATUS_PENDING"),
          fn user_data -> assert user_data.status == "ACTIVITY_STATUS_PENDING" end
        }
      ]

      for {response, assertion} <- test_cases do
        assert {:ok, user_data} = CreateSubOrganizationResponse.parse_response(response)
        assertion.(user_data)
        # Should still have basic required fields
        assert is_binary(user_data.sub_org_id)
        assert is_binary(user_data.activity_id)
      end
    end
  end
end
