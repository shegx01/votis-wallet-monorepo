defmodule BeVotisWalletWeb.SignRawPayloadsController.Response do
  @moduledoc """
  Handle multiple raw payloads signing response

  Signs multiple raw payloads for existing users using pre-signed requests from clients.
  The stamped_body contains multiple raw payload data and signing specifications,
  and is forwarded to Turnkey's sign_raw_payloads endpoint using the appropriate
  authentication method (OAuth, WebAuthn, or Passkey).

  References:
  https://docs.turnkey.com/api-reference/activities/sign-raw-payloads
  """
  use BeVotisWalletWeb, :controller

  require Logger

  alias BeVotisWallet.Services.Turnkey.Activities
  alias BeVotisWallet.Users.User
  alias BeVotisWalletWeb.Utils.TurnkeyResponse

  @spec create(Plug.Conn.t(), map()) :: Plug.Conn.t()
  def create(conn, params) do
    case conn.assigns[:user] do
      %User{} = user ->
        # User exists, proceed with raw payloads signing
        handle_sign_raw_payloads(conn, params, user)

      nil ->
        # User doesn't exist, return 404
        Logger.warning("Sign raw payloads attempt for non-existent user",
          email: Map.get(params, "email")
        )

        conn
        |> put_status(:not_found)
        |> json(%{
          error: "User not found",
          message: "The specified user does not exist"
        })
    end
  end

  # Private function to handle raw payloads signing flow
  defp handle_sign_raw_payloads(conn, params, user) do
    with {:ok, stamped_body} <- extract_stamped_body(params),
         {:ok, stamp} <- extract_stamp(params),
         {:ok, turnkey_response} <- sign_turnkey_raw_payloads(stamped_body, stamp) do
      # Log successful raw payloads signing
      Logger.info("Successful raw payloads signing",
        email: user.email,
        user_id: user.id,
        sub_org_id: user.sub_org_id,
        activity_id: get_in(turnkey_response, ["activity", "id"])
      )

      # Return 200 with the signed payloads result
      case get_in(turnkey_response, ["activity", "result"]) do
        nil ->
          # Fallback if result structure is unexpected
          conn
          |> put_status(:ok)
          |> json(%{message: "Raw payloads signed successfully"})

        result ->
          # Return the actual signing result from Turnkey
          conn
          |> put_status(:ok)
          |> json(result)
      end
    else
      {:error, :missing_parameter, param} ->
        Logger.warning("Missing required parameter for raw payloads signing",
          parameter: param,
          user_id: user.id
        )

        conn
        |> put_status(:bad_request)
        |> json(%{error: "Missing required parameter: #{param}"})

      {:error, :turnkey_error, status_code, error_message} ->
        Logger.error("Turnkey API error during raw payloads signing",
          status_code: status_code,
          error: inspect(error_message),
          user_id: user.id
        )

        conn
        |> put_status(TurnkeyResponse.map_status_code(status_code))
        |> json(%{
          error: "Failed to sign raw payloads",
          message: "External service error"
        })
    end
  end

  # Parameter extraction helpers
  defp extract_stamped_body(params), do: get_required_param(params, "stamped_body")
  defp extract_stamp(params), do: get_required_param(params, "stamp")

  defp get_required_param(params, key) do
    case Map.get(params, key) do
      nil -> {:error, :missing_parameter, key}
      value when is_binary(value) and byte_size(value) > 0 -> {:ok, value}
      _ -> {:error, :missing_parameter, key}
    end
  end

  # Turnkey integration using execute_signed_request with API key authentication (X-Stamp)
  # This uses X-Stamp header for client-signed requests with API key from signup or login
  defp sign_turnkey_raw_payloads(stamped_body, stamp) do
    case Activities.execute_signed_request(
           stamped_body,
           stamp,
           "ACTIVITY_TYPE_SIGN_RAW_PAYLOADS",
           auth_type: :api_key
         ) do
      {:ok, response} -> {:ok, response}
      {:error, status_code, error_message} -> {:error, :turnkey_error, status_code, error_message}
    end
  end
end
