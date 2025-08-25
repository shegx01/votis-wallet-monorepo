defmodule BeVotisWalletWeb.LoginController do
  use BeVotisWalletWeb, :controller

  def create(conn, _params) do
    send_resp(conn, 200, "OK")
  end
end
