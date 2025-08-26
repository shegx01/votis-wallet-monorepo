defmodule BeVotisWalletWeb.SignUpController.Response do
  @moduledoc """
  Handle user sign up response
  Users can signup with passkey/oauth
  References
  https://docs.turnkey.com/api-reference/activities/create-sub-organization
  """
  use BeVotisWalletWeb, :controller

  require Logger

  alias BeVotisWallet.Services.Turnkey.Activities
  alias BeVotisWallet.Services.Turnkey.Schemas.CreateSubOrganizationResponse
  alias BeVotisWallet.Users.User

  @spec create(Plug.Conn.t(), map()) :: Plug.Conn.t()
  def create(conn, params) do
    case conn.assigns[:user] do
      %User{} = user ->
        # User already exists, return their encoded org_id
        Logger.info("Sign up attempt for existing user", email: user.email, user_id: user.id)

        encoded_org_id = Base.encode64(user.sub_org_id)

        conn
        |> put_status(200)
        |> json(%{org_id: encoded_org_id})

      nil ->
        # User doesn't exist, create new sub-organization with Turnkey
        handle_new_user_signup(conn, params)
    end
  end

  # Private function to handle new user signup flow
  defp handle_new_user_signup(conn, params) do
    with {:ok, email} <- extract_email(params),
         {:ok, stamped_body} <- extract_stamped_body(params),
         {:ok, stamp} <- extract_stamp(params),
         {:ok, sub_org_name} <- extract_sub_org_name(params),
         {:ok, authenticator_name} <- extract_authenticator_name(params),
         {:ok, turnkey_response} <- create_turnkey_sub_organization(stamped_body, stamp),
         {:ok, turnkey_data} <- parse_turnkey_response(turnkey_response),
         {:ok, user} <- create_user_record(email, sub_org_name, authenticator_name, turnkey_data) do
      # Log successful signup
      Logger.info("Successful user signup",
        email: email,
        user_id: user.id,
        sub_org_id: user.sub_org_id,
        activity_id: turnkey_data.activity_id
      )

      # Return 201 with encoded org_id
      encoded_org_id = Base.encode64(user.sub_org_id)

      conn
      |> put_status(201)
      |> json(%{org_id: encoded_org_id})
    else
      {:error, :missing_parameter, param} ->
        Logger.warning("Missing required parameter for signup", parameter: param)

        conn
        |> put_status(400)
        |> json(%{error: "Missing required parameter: #{param}"})

      {:error, :turnkey_error, status_code, error_message} ->
        Logger.error("Turnkey API error during signup",
          status_code: status_code,
          error: inspect(error_message)
        )

        conn
        |> put_status(502)
        |> json(%{error: "External service error"})

      {:error, :turnkey_parse_error, error_message} ->
        Logger.error("Failed to parse Turnkey response",
          error: inspect(error_message)
        )

        conn
        |> put_status(502)
        |> json(%{error: "Invalid response from external service"})

      {:error, :database_error, changeset} ->
        Logger.error("Database error during user creation",
          errors: inspect(changeset.errors)
        )

        conn
        |> put_status(500)
        |> json(%{error: "Internal server error"})
    end
  end

  # Parameter extraction helpers
  defp extract_email(params), do: get_required_param(params, "email")
  defp extract_stamped_body(params), do: get_required_param(params, "stamped_body")
  defp extract_stamp(params), do: get_required_param(params, "stamp")
  defp extract_sub_org_name(params), do: get_required_param(params, "sub_organization_name")
  defp extract_authenticator_name(params), do: get_optional_param(params, "authenticator_name")

  defp get_required_param(params, key) do
    case Map.get(params, key) do
      nil -> {:error, :missing_parameter, key}
      value when is_binary(value) and byte_size(value) > 0 -> {:ok, value}
      _ -> {:error, :missing_parameter, key}
    end
  end

  defp get_optional_param(params, key) do
    case Map.get(params, key) do
      nil -> {:ok, nil}
      value when is_binary(value) -> {:ok, value}
      _ -> {:ok, nil}
    end
  end

  # Turnkey integration using create_sub_organization with WebAuthn auth
  defp create_turnkey_sub_organization(_stamped_body, stamp) do
    # For signup flow, we create a sub-organization with WebAuthn authentication
    # The stamped_body and stamp are used as client signature for WebAuthn
    sub_org_name = "User Sub Organization"

    case Activities.create_sub_organization(sub_org_name,
           auth_type: :webauthn,
           client_signature: stamp
         ) do
      {:ok, response} -> {:ok, response}
      {:error, status_code, error_message} -> {:error, :turnkey_error, status_code, error_message}
    end
  end

  # Parse Turnkey response using embedded schema
  defp parse_turnkey_response(turnkey_response) do
    case CreateSubOrganizationResponse.parse_response(turnkey_response) do
      {:ok, user_data} -> {:ok, user_data}
      {:error, error_message} -> {:error, :turnkey_parse_error, error_message}
    end
  end

  # Database operations - include all data from Turnkey API response
  defp create_user_record(email, sub_org_name, authenticator_name, turnkey_data) do
    attrs = %{
      email: String.downcase(email),
      sub_org_id: turnkey_data.sub_org_id,
      sub_organization_name: sub_org_name,
      authenticator_name: authenticator_name,
      wallet_id: turnkey_data.wallet_id,
      root_user_ids: turnkey_data.root_user_ids
    }

    case User.create_user(attrs) do
      {:ok, user} -> {:ok, user}
      {:error, changeset} -> {:error, :database_error, changeset}
    end
  end
end
