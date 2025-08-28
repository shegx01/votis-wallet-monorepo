defmodule BeVotisWalletWeb.SignTransactionController do
  @moduledoc """
  Handle transaction signing with Turnkey

  This controller accepts pre-signed requests from clients to sign
  transactions using their Turnkey wallets. Supports both WebAuthn/Passkey
  and OAuth authentication methods.
  """
  use BeVotisWalletWeb, :controller

  alias BeVotisWalletWeb.Plugs.CheckUserExistence

  plug CheckUserExistence

  @spec create(Plug.Conn.t(), any()) :: Plug.Conn.t()
  defdelegate create(conn, params), to: BeVotisWalletWeb.SignTransactionController.Response
end
