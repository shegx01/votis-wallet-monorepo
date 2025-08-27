defmodule BeVotisWalletWeb.LoginController.Response do
  use BeVotisWalletWeb, :controller

  require Logger

  alias BeVotisWallet.Services.Turnkey.Activities
  alias BeVotisWallet.Users.User
  alias BeVotisWalletWeb.LoginController.LoginParams

  @moduledoc """
  LoginController response handler

  Handles user login with pre-signed requests from clients.
  The stamped_body contains all authentication details (passkey or OAuth)
  and is forwarded to Turnkey's client_signed_request endpoint.

  References:
   Stamp ref: https://docs.turnkey.com/api-reference/activities/login-with-a-stamp --  FIDO2, passkey, etc
   OAuth ref: https://docs.turnkey.com/api-reference/activities/login-with-oauth -- google, facebook, etc
  """

  @spec create(Plug.Conn.t(), map()) :: Plug.Conn.t()
  def create(conn, params) do
    with {:ok, validated_params} <- validate_and_parse_params(params),
         {:ok, user} <- validate_user_exists(conn.assigns[:user]),
         {:ok, session_data} <- perform_authentication(validated_params) do
      # Extract JWT from session data
      jwt = extract_jwt_from_session(session_data)

      Logger.info("Successful login",
        auth_type: validated_params.auth_type,
        email: validated_params.email,
        user_id: user.id,
        activity_id: get_in(session_data, ["activity", "id"])
      )

      conn
      |> put_status(:ok)
      |> json(%{jwt: jwt})
    else
      {:error, :invalid_params, changeset} ->
        Logger.warning("Invalid login parameters",
          errors: inspect(format_changeset_errors(changeset))
        )

        conn
        |> put_status(:bad_request)
        |> json(%{error: "Invalid parameters", errors: format_changeset_errors(changeset)})

      {:error, :user_not_found} ->
        Logger.warning("User not found for login",
          email: Map.get(params, "email")
        )

        conn
        |> put_status(:not_found)
        |> json(%{
          error: "User not found",
          message: "The specified user does not exist"
        })

      {:error, :authentication_failed, status_code, error_message} ->
        Logger.error("Authentication failed",
          status_code: status_code,
          error: inspect(error_message)
        )

        conn
        |> put_status(map_turnkey_status_code(status_code))
        |> json(%{
          error: "Authentication failed",
          message: "Invalid credentials or authentication service error"
        })
    end
  end

  # Private helper functions

  defp validate_and_parse_params(params) do
    case LoginParams.changeset(%LoginParams{}, params) do
      %Ecto.Changeset{valid?: true} = changeset ->
        {:ok, Ecto.Changeset.apply_changes(changeset)}

      %Ecto.Changeset{valid?: false} = changeset ->
        {:error, :invalid_params, changeset}
    end
  end

  defp validate_user_exists(user) do
    case user do
      nil -> {:error, :user_not_found}
      %User{} = user -> {:ok, user}
    end
  end

  defp perform_authentication(%LoginParams{auth_type: "passkey"} = params) do
    # For passkey authentication, use execute_signed_request directly with STAMP_LOGIN activity
    case Activities.execute_signed_request(
           params.stamped_body,
           params.stamp,
           "ACTIVITY_TYPE_STAMP_LOGIN",
           auth_type: :passkey
         ) do
      {:ok, response} ->
        {:ok, response}

      {:error, status_code, error_message} ->
        {:error, :authentication_failed, status_code, error_message}
    end
  end

  defp perform_authentication(%LoginParams{auth_type: "oauth"} = params) do
    # For OAuth authentication, use execute_signed_request directly with OAUTH_LOGIN activity
    case Activities.execute_signed_request(
           params.stamped_body,
           params.stamp,
           "ACTIVITY_TYPE_OAUTH_LOGIN",
           auth_type: :webauthn
         ) do
      {:ok, response} ->
        {:ok, response}

      {:error, status_code, error_message} ->
        {:error, :authentication_failed, status_code, error_message}
    end
  end

  defp extract_jwt_from_session(session_data) do
    # Extract JWT/session token from the nested response structure
    case session_data do
      # OAuth login response - nested structure
      %{
        "activity" => %{
          "result" => %{
            "activity" => %{"result" => %{"oauthLoginResult" => %{"session" => session_token}}}
          }
        }
      } ->
        session_token

      # Stamp/Passkey login response - nested structure
      %{
        "activity" => %{
          "result" => %{
            "activity" => %{"result" => %{"stampLoginResult" => %{"session" => session_token}}}
          }
        }
      } ->
        session_token

      # Fallback: try to find session in the top-level result (in case structure varies)
      %{"activity" => %{"result" => %{"oauthLoginResult" => %{"session" => session_token}}}} ->
        session_token

      %{"activity" => %{"result" => %{"stampLoginResult" => %{"session" => session_token}}}} ->
        session_token

      # Generic fallback: search for session token in nested structure
      %{"activity" => %{"result" => result}} when is_map(result) ->
        find_session_in_nested_result(result)

      _ ->
        # Ultimate fallback: encode the entire response as JSON
        Logger.warning("Unexpected session response structure", response: inspect(session_data))
        Jason.encode!(session_data)
    end
  end

  defp find_session_in_nested_result(result) do
    cond do
      # Direct session fields
      Map.has_key?(result, "session") ->
        Map.get(result, "session")

      # Check nested activity structure
      is_map(result["activity"]) and is_map(result["activity"]["result"]) ->
        nested_result = result["activity"]["result"]

        cond do
          Map.has_key?(nested_result, "oauthLoginResult") and
              Map.has_key?(nested_result["oauthLoginResult"], "session") ->
            nested_result["oauthLoginResult"]["session"]

          Map.has_key?(nested_result, "stampLoginResult") and
              Map.has_key?(nested_result["stampLoginResult"], "session") ->
            nested_result["stampLoginResult"]["session"]

          true ->
            Jason.encode!(nested_result)
        end

      # Other possible session field names
      Map.has_key?(result, "sessionToken") ->
        Map.get(result, "sessionToken")

      Map.has_key?(result, "credentialBundle") ->
        Map.get(result, "credentialBundle")

      true ->
        Jason.encode!(result)
    end
  end

  defp format_changeset_errors(changeset) do
    Ecto.Changeset.traverse_errors(changeset, fn {msg, opts} ->
      Enum.reduce(opts, msg, fn {key, value}, acc ->
        String.replace(acc, "%{#{key}}", to_string(value))
      end)
    end)
  end

  defp map_turnkey_status_code(status_code) do
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
