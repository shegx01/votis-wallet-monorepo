defmodule BeVotisWallet.DataCase do
  @moduledoc """
  This module defines the setup for tests requiring
  access to the application's data layer.

  You may define functions here to be used as helpers in
  your tests.

  Finally, if the test case interacts with the database,
  we enable the SQL sandbox, so changes done to the database
  are reverted at the end of every test. If you are using
  PostgreSQL, you can even run database tests asynchronously
  by setting `use BeVotisWallet.DataCase, async: true`, although
  this option is not recommended for other databases.
  """

  use ExUnit.CaseTemplate

  using do
    quote do
      alias BeVotisWallet.Repo

      import Ecto
      import Ecto.Changeset
      import Ecto.Query
      import BeVotisWallet.DataCase
    end
  end

  setup tags do
    BeVotisWallet.DataCase.setup_sandbox(tags)
    :ok
  end

  @doc """
  Sets up the sandbox based on the test tags.
  """
  def setup_sandbox(tags) do
    pid = Ecto.Adapters.SQL.Sandbox.start_owner!(BeVotisWallet.Repo, shared: not tags[:async])
    on_exit(fn -> Ecto.Adapters.SQL.Sandbox.stop_owner(pid) end)
  end

  @doc """
  A helper that transforms changeset errors into a map of messages.

      assert {:error, changeset} = Accounts.create_user(%{password: "short"})
      assert "password is too short" in errors_on(changeset).password
      assert %{password: ["password is too short"]} = errors_on(changeset)

  """
  def errors_on(changeset) do
    Ecto.Changeset.traverse_errors(changeset, fn {message, opts} ->
      Regex.replace(~r"%{(\w+)}", message, fn _, key ->
        opts |> Keyword.get(String.to_existing_atom(key), key) |> to_string()
      end)
    end)
  end

  @doc """
  Insert a user into the database for testing purposes.
  """
  def insert(:user, attrs \\ %{}) do
    default_attrs = %{
      email: "test#{System.unique_integer()}@example.com",
      sub_org_id: "org_#{System.unique_integer()}",
      sub_organization_name: "Test Organization #{System.unique_integer()}"
    }

    attrs =
      if is_list(attrs) do
        Map.merge(default_attrs, Enum.into(attrs, %{}))
      else
        Map.merge(default_attrs, attrs)
      end

    {:ok, user} = BeVotisWallet.Users.User.create_user(attrs)
    user
  end
end
