defmodule BeVotisWalletWeb.Plug.ValidateJwt do
  @moduledoc """
  NOTE: SKIP IMPLEMENTATION FO NOW UNTIL LOGIN IS IMPLEMENTED
  Validate Turnkey JWT
  Reference: https://docs.turnkey.com/api-reference/activities/create-read-write-session for references
  """
  @behaviour Plug

  alias Plug.Conn
  def init(opts), do: opts

  def call(%Conn{} = conn, _opts) do
    # Check if Turnkey JWT is present in the request header and also the claim is valid
    # if it has expirred but valid? refresh it
    # if it is invalid? return 401
    # if  it's valid? return the conn
    # TODO! implement this use reference for implementation
    conn
  end
end
