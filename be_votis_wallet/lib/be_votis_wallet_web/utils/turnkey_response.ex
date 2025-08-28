defmodule BeVotisWalletWeb.Utils.TurnkeyResponse do
  @moduledoc """
  Utility functions for handling Turnkey API responses in controllers.

  This module provides common functions for mapping Turnkey status codes 
  to Phoenix HTTP status atoms and other response handling utilities.
  """

  @doc """
  Maps Turnkey HTTP status codes to Phoenix HTTP status atoms.

  This function standardizes how we handle different Turnkey API error responses
  across all controllers that interact with Turnkey services.

  ## Examples

      iex> BeVotisWalletWeb.Utils.TurnkeyResponse.map_status_code(400)
      :bad_request
      
      iex> BeVotisWalletWeb.Utils.TurnkeyResponse.map_status_code(404)
      :not_found
      
      iex> BeVotisWalletWeb.Utils.TurnkeyResponse.map_status_code(999)
      :internal_server_error
  """
  def map_status_code(status_code) do
    case status_code do
      400 -> :bad_request
      401 -> :unauthorized
      403 -> :forbidden
      404 -> :not_found
      409 -> :conflict
      422 -> :unprocessable_entity
      429 -> :too_many_requests
      500 -> :internal_server_error
      502 -> :bad_gateway
      503 -> :service_unavailable
      _ -> :internal_server_error
    end
  end
end
