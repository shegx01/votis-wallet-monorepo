defmodule BeVotisWalletWeb.LoginController do
  use BeVotisWalletWeb, :controller

  @spec create(Plug.Conn.t(), %{
          optional(:__struct__) => none(),
          optional(atom() | binary()) => any()
        }) :: Plug.Conn.t()
  defdelegate create(conn, params), to: BeVotisWalletWeb.LoginController.Response
end
