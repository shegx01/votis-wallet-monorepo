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

  alias BeVotisWallet.Users.User
  alias BeVotisWalletWeb.Utils.{SigningUtils, TurnkeyResponse}

  @spec create(Plug.Conn.t(), map()) :: Plug.Conn.t()
  def create(conn, params) do
    context = SigningUtils.create_signing_context(
      "raw payloads signing",
      "Raw payloads signed successfully",
      "Failed to sign raw payloads"
    )

    case conn.assigns[:user] do
      %User{} = user ->
        handle_signing_operation(conn, params, user, context)

      nil ->
        SigningUtils.log_user_not_found(Map.get(params, "email"), "raw payloads signing")
        error_data = SigningUtils.prepare_user_not_found_error()

        conn
        |> put_status(:not_found)
        |> json(error_data)
    end
  end

  defp handle_signing_operation(conn, params, user, context) do
    case SigningUtils.execute_signing_operation(
           params,
           user,
           "ACTIVITY_TYPE_SIGN_RAW_PAYLOADS",
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
