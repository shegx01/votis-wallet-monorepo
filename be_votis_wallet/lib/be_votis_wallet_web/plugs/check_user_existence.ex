defmodule BeVotisWalletWeb.Plugs.CheckUserExistence do
  @moduledoc """
  Check if user exists based on the email parameter.

  This plug reads the "email" parameter from either query params or body params,
  queries the database for a user with that email, and assigns the result to
  conn.assigns.user.

  ## Usage

  In a controller:
  ```elixir
  plug BeVotisWalletWeb.Plugs.CheckUserExistence
  ```

  ## Assigns

  - `:user` - The user struct if found, `nil` if not found
  """
  @behaviour Plug

  import Plug.Conn
  require Logger

  alias BeVotisWallet.Users.User

  def init(opts), do: opts

  def call(%Plug.Conn{} = conn, _opts) do
    email = get_email_from_params(conn)

    case email do
      nil ->
        Logger.warning("CheckUserExistence plug: No email parameter provided")
        assign(conn, :user, nil)

      email when is_binary(email) ->
        case User.get_by_email(email) do
          {:ok, user} ->
            Logger.debug("CheckUserExistence plug: User found", email: email, user_id: user.id)
            assign(conn, :user, user)

          {:error, :not_found} ->
            Logger.debug("CheckUserExistence plug: User not found", email: email)
            assign(conn, :user, nil)
        end
    end
  end

  # Private helper to extract email from various parameter sources
  defp get_email_from_params(conn) do
    # Try query params first, then body params, then general params
    # Handle unfetched params safely
    email = 
      get_param_safely(conn.query_params, "email") ||
      get_param_safely(conn.body_params, "email") ||
      get_param_safely(conn.params, "email")
    
    case email do
      nil -> nil
      "" -> nil
      email when is_binary(email) -> String.trim(email)
      _ -> nil
    end
  end
  
  # Safely get parameter from potentially unfetched params
  defp get_param_safely(%Plug.Conn.Unfetched{}, _key), do: nil
  defp get_param_safely(params, key) when is_map(params), do: params[key]
  defp get_param_safely(_, _), do: nil
end
