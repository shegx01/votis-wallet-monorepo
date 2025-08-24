defmodule BeVotisWalletWeb.Plug.PrivateAuth do
  @behaviour Plug
  alias Plug.Conn
  def init(opts), do: opts

  @spec call(Plug.Conn.t(), any()) :: Plug.Conn.t()
  def call(%Conn{} = conn, _opts) do
    {username, password} = get_private_auth_credentials()
    Plug.BasicAuth.basic_auth(conn, username: username, password: password)
  end

  defp get_private_auth_credentials() do
    config = Application.get_env(:be_votis_wallet, :private_auth_credentials)
    {config[:username], config[:password]}
  end
end
