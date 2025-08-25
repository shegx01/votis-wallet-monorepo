defmodule BeVotisWalletWeb.ConnCase do
  @moduledoc """
  This module defines the test case to be used by
  tests that require setting up a connection.

  Such tests rely on `Phoenix.ConnTest` and also
  import other functionality to make it easier
  to build common data structures and query the data layer.

  Finally, if the test case interacts with the database,
  we enable the SQL sandbox, so changes done to the database
  are reverted at the end of every test. If you are using
  PostgreSQL, you can even run database tests asynchronously
  by setting `use BeVotisWalletWeb.ConnCase, async: true`, although
  this option is not recommended for other databases.
  """

  use ExUnit.CaseTemplate

  using do
    quote do
      # The default endpoint for testing
      @endpoint BeVotisWalletWeb.Endpoint

      use BeVotisWalletWeb, :verified_routes

      # Import conveniences for testing with connections
      import Plug.Conn
      import Phoenix.ConnTest
      import BeVotisWalletWeb.ConnCase
      import BeVotisWallet.DataCase, only: [insert: 1, insert: 2]
    end
  end

  setup tags do
    BeVotisWallet.DataCase.setup_sandbox(tags)
    
    conn = Phoenix.ConnTest.build_conn()
    
    # Add basic auth for private endpoints
    auth_header = Plug.BasicAuth.encode_basic_auth("test_user", "test_pass")
    conn = Plug.Conn.put_req_header(conn, "authorization", auth_header)
    
    {:ok, conn: conn}
  end
end
