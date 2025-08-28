defmodule BeVotisWalletWeb.SignRawPayloadController.Response do
  @moduledoc """
  Handle raw payload signing response

  Signs raw payloads for existing users using pre-signed requests from clients.
  The stamped_body contains the raw payload data and signing specification,
  and is forwarded to Turnkey's sign_raw_payload endpoint using the appropriate
  authentication method (OAuth, WebAuthn, or Passkey).

  References:
  https://docs.turnkey.com/api-reference/activities/sign-raw-payload
  """
  use BeVotisWalletWeb, :controller

  alias BeVotisWallet.Users.User
  alias BeVotisWalletWeb.Utils.{SigningUtils, TurnkeyResponse}

  @spec create(Plug.Conn.t(), map()) :: Plug.Conn.t()
  def create(conn, params) do
    context = SigningUtils.create_signing_context(
      "raw payload signing",
      "Raw payload signed successfully",
      "Failed to sign raw payload"
    )

    case conn.assigns[:user] do
      %User{} = user ->
        # User exists, proceed with raw payload signing
        handle_signing_operation(conn, params, user, context)

      nil ->
        # User doesn't exist, return 404
        SigningUtils.log_user_not_found(Map.get(params, "email"), "raw payload signing")
        error_data = SigningUtils.prepare_user_not_found_error()

        conn
        |> put_status(:not_found)
        |> json(error_data)
    end
  end

  # Private function to handle raw payload signing using SigningUtils
  defp handle_signing_operation(conn, params, user, context) do
    case SigningUtils.execute_signing_operation(
           params,
           user,
           "ACTIVITY_TYPE_SIGN_RAW_PAYLOAD_V2",
           context
         ) do
      {:ok, response_data} ->
        conn
        |> put_status(:ok)
        |> json(response_data)

      {:error, :missing_parameter, param} ->
        error_data = SigningUtils.prepare_missing_param_error(param)

        conn
        |> put_status(:bad_request)
        |> json(error_data)

      {:error, :turnkey_error, status_code, _error_message} ->
        error_data = SigningUtils.prepare_turnkey_error(context.error_prefix)

        conn
        |> put_status(TurnkeyResponse.map_status_code(status_code))
        |> json(error_data)
    end
  end
end
