defmodule BeVotisWalletWeb.SignRawPayloadController do
  @moduledoc """
  Handle raw payload signing with Turnkey

  This controller accepts pre-signed requests from clients to sign
  raw payloads using their Turnkey wallets. Supports both WebAuthn/Passkey
  and OAuth authentication methods.

  Uses ACTIVITY_TYPE_SIGN_RAW_PAYLOAD_V2 for single payload signing.
  """
  use BeVotisWalletWeb, :controller

  alias BeVotisWalletWeb.Plugs.CheckUserExistence

  plug CheckUserExistence

  @spec create(Plug.Conn.t(), any()) :: Plug.Conn.t()
  defdelegate create(conn, params), to: BeVotisWalletWeb.SignRawPayloadController.Response
end
