defmodule BeVotisWalletWeb.UserInfoController do
  use BeVotisWalletWeb, :controller

  alias BeVotisWalletWeb.Plugs.CheckUserExistence

  plug CheckUserExistence

  defdelegate show(conn, params), to: BeVotisWalletWeb.UserInfoController.Response
end
