defmodule BeVotisWalletWeb.Utils.SigningUtils do
  @moduledoc """
  Pure utility functions for handling signing operations.

  This module provides business logic for signing operations without
  any dependencies on Plug.Conn, making it easily testable and reusable:
  - Parameter extraction and validation
  - Turnkey API integration with execute_signed_request
  - Response data preparation
  - Operation context management

  Controllers use these functions and handle their own HTTP concerns.
  """

  require Logger

  alias BeVotisWallet.Services.Turnkey.Activities
  alias BeVotisWallet.Users.User

  @type activity_type :: String.t()
  @type signing_context :: %{
          operation_name: String.t(),
          success_message: String.t(),
          error_prefix: String.t()
        }
  @type signing_result ::
          {:ok, map()}
          | {:error, :missing_parameter, String.t()}
          | {:error, :turnkey_error, integer(), term()}

  @doc """
  Execute a complete signing operation with the given parameters.

  This function handles:
  1. Parameter extraction and validation
  2. Turnkey API request execution
  3. Success/error result preparation
  4. Appropriate logging

  ## Parameters
  - `params` - Request parameters map
  - `user` - The authenticated user
  - `activity_type` - The Turnkey activity type
  - `context` - Signing context with operation-specific messaging

  ## Returns
  - `{:ok, response_data}` - Success with response data to return
  - `{:error, :missing_parameter, param_name}` - Missing parameter error
  - `{:error, :turnkey_error, status_code, error_message}` - Turnkey API error
  """
  @spec execute_signing_operation(map(), %User{}, activity_type(), signing_context()) ::
          signing_result()
  def execute_signing_operation(params, %User{} = user, activity_type, context) do
    with {:ok, {stamped_body, stamp}} <- extract_signing_params(params),
         {:ok, turnkey_response} <- execute_turnkey_signing(stamped_body, stamp, activity_type) do
      # Log successful signing operation
      Logger.info("Successful #{context.operation_name}",
        email: user.email,
        user_id: user.id,
        sub_org_id: user.sub_org_id,
        activity_id: get_in(turnkey_response, ["activity", "id"]),
        activity_type: activity_type
      )

      # Prepare success response data
      response_data = prepare_success_response(turnkey_response, context.success_message)
      {:ok, response_data}
    else
      {:error, :missing_parameter, param} = error ->
        Logger.warning("Missing required parameter for #{context.operation_name}",
          parameter: param,
          user_id: user.id
        )

        error

      {:error, :turnkey_error, status_code, error_message} = error ->
        Logger.error("Turnkey API error during #{context.operation_name}",
          status_code: status_code,
          error: inspect(error_message),
          user_id: user.id,
          activity_type: activity_type
        )

        error
    end
  end

  @doc """
  Log a user not found attempt for a signing operation.

  ## Parameters
  - `email` - The email that was attempted (may be nil)
  - `operation_name` - Name of the operation for logging
  """
  @spec log_user_not_found(String.t() | nil, String.t()) :: :ok
  def log_user_not_found(email, operation_name) do
    Logger.warning("#{String.capitalize(operation_name)} attempt for non-existent user",
      email: email
    )
  end

  @doc """
  Extract and validate required signing parameters from request.

  ## Parameters
  - `params` - Request parameters map

  ## Returns
  - `{:ok, {stamped_body, stamp}}` on success
  - `{:error, :missing_parameter, param_name}` on missing/invalid parameter
  """
  @spec extract_signing_params(map()) ::
          {:ok, {String.t(), String.t()}} | {:error, :missing_parameter, String.t()}
  def extract_signing_params(params) do
    with {:ok, stamped_body} <- extract_stamped_body(params),
         {:ok, stamp} <- extract_stamp(params) do
      {:ok, {stamped_body, stamp}}
    end
  end

  @doc """
  Create a signing context for operation-specific messaging.

  ## Parameters
  - `operation_name` - Name of the operation (e.g., "transaction signing")
  - `success_message` - Message for successful fallback response
  - `error_prefix` - Prefix for error messages

  ## Returns
  - `signing_context()` map with operation-specific strings
  """
  @spec create_signing_context(String.t(), String.t(), String.t()) :: signing_context()
  def create_signing_context(operation_name, success_message, error_prefix) do
    %{
      operation_name: operation_name,
      success_message: success_message,
      error_prefix: error_prefix
    }
  end

  @doc """
  Prepare error response data for missing parameter errors.

  ## Parameters
  - `param_name` - Name of the missing parameter

  ## Returns
  - Map with error response data
  """
  @spec prepare_missing_param_error(String.t()) :: map()
  def prepare_missing_param_error(param_name) do
    %{error: "Missing required parameter: #{param_name}"}
  end

  @doc """
  Prepare error response data for Turnkey API errors.

  ## Parameters
  - `error_prefix` - Prefix for the error message

  ## Returns
  - Map with error response data
  """
  @spec prepare_turnkey_error(String.t()) :: map()
  def prepare_turnkey_error(error_prefix) do
    %{
      error: error_prefix,
      message: "External service error"
    }
  end

  @doc """
  Prepare user not found error response data.

  ## Returns
  - Map with error response data
  """
  @spec prepare_user_not_found_error() :: map()
  def prepare_user_not_found_error do
    %{
      error: "User not found",
      message: "The specified user does not exist"
    }
  end

  # Private functions

  defp extract_stamped_body(params), do: get_required_param(params, "stamped_body")
  defp extract_stamp(params), do: get_required_param(params, "stamp")

  defp get_required_param(params, key) do
    case Map.get(params, key) do
      nil -> {:error, :missing_parameter, key}
      value when is_binary(value) and byte_size(value) > 0 -> {:ok, value}
      _ -> {:error, :missing_parameter, key}
    end
  end

  defp execute_turnkey_signing(stamped_body, stamp, activity_type) do
    case Activities.execute_signed_request(
           stamped_body,
           stamp,
           activity_type,
           auth_type: :api_key
         ) do
      {:ok, response} -> {:ok, response}
      {:error, status_code, error_message} -> {:error, :turnkey_error, status_code, error_message}
    end
  end

  defp prepare_success_response(turnkey_response, fallback_message) do
    case get_in(turnkey_response, ["activity", "result"]) do
      nil ->
        # Fallback if result structure is unexpected
        %{message: fallback_message}

      result ->
        # Return the actual signing result from Turnkey
        result
    end
  end
end
