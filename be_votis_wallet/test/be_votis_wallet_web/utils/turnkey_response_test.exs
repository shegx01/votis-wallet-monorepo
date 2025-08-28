defmodule BeVotisWalletWeb.Utils.TurnkeyResponseTest do
  use ExUnit.Case, async: true

  alias BeVotisWalletWeb.Utils.TurnkeyResponse

  describe "map_status_code/1" do
    test "maps common HTTP status codes correctly" do
      assert TurnkeyResponse.map_status_code(400) == :bad_request
      assert TurnkeyResponse.map_status_code(401) == :unauthorized
      assert TurnkeyResponse.map_status_code(403) == :forbidden
      assert TurnkeyResponse.map_status_code(404) == :not_found
      assert TurnkeyResponse.map_status_code(409) == :conflict
      assert TurnkeyResponse.map_status_code(422) == :unprocessable_entity
      assert TurnkeyResponse.map_status_code(429) == :too_many_requests
      assert TurnkeyResponse.map_status_code(500) == :internal_server_error
      assert TurnkeyResponse.map_status_code(502) == :bad_gateway
      assert TurnkeyResponse.map_status_code(503) == :service_unavailable
    end

    test "maps unknown status codes to internal_server_error" do
      assert TurnkeyResponse.map_status_code(999) == :internal_server_error
      assert TurnkeyResponse.map_status_code(0) == :internal_server_error
      assert TurnkeyResponse.map_status_code(100) == :internal_server_error
      assert TurnkeyResponse.map_status_code(301) == :internal_server_error
    end

    test "handles edge cases" do
      # Test negative numbers
      assert TurnkeyResponse.map_status_code(-1) == :internal_server_error

      # Test very large numbers
      assert TurnkeyResponse.map_status_code(9999) == :internal_server_error
    end
  end
end
