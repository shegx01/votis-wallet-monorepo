defmodule BeVotisWalletWeb.LoginController do
  use BeVotisWalletWeb, :controller

  defdelegate create(conn, params), to: BeVotisWalletWeb.LoginController.Response
end
