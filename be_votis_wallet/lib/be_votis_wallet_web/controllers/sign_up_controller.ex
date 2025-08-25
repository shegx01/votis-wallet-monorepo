defmodule BeVotisWalletWeb.SignUpController do
  @moduledoc """
  Handle user sign up with Turnkey
  """
  use BeVotisWalletWeb, :controller

  alias BeVotisWalletWeb.Plugs.CheckUserExistence

  plug CheckUserExistence

  @spec create(Plug.Conn.t(), any()) :: Plug.Conn.t()
  defdelegate create(conn, params), to: BeVotisWalletWeb.SignUpController.Response
end
