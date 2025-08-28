defmodule BeVotisWalletWeb.CreateAuthenticatorController do
  @moduledoc """
  Handle authenticator creation with Turnkey

  This controller accepts pre-signed requests from clients to create
  new authenticators (WebAuthn/Passkey) for existing users.
  """
  use BeVotisWalletWeb, :controller

  alias BeVotisWalletWeb.Plugs.CheckUserExistence

  plug CheckUserExistence

  @spec create(Plug.Conn.t(), any()) :: Plug.Conn.t()
  defdelegate create(conn, params), to: BeVotisWalletWeb.CreateAuthenticatorController.Response
end
