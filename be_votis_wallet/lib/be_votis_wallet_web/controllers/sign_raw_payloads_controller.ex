defmodule BeVotisWalletWeb.SignRawPayloadsController do
  @moduledoc """
  Handle multiple raw payloads signing with Turnkey

  This controller accepts pre-signed requests from clients to sign
  multiple raw payloads using their Turnkey wallets. Supports both
  WebAuthn/Passkey and OAuth authentication methods.

  Uses ACTIVITY_TYPE_SIGN_RAW_PAYLOADS for multiple payload signing.
  """
  use BeVotisWalletWeb, :controller

  alias BeVotisWalletWeb.Plugs.CheckUserExistence

  plug CheckUserExistence

  @spec create(Plug.Conn.t(), any()) :: Plug.Conn.t()
  defdelegate create(conn, params), to: BeVotisWalletWeb.SignRawPayloadsController.Response
end
