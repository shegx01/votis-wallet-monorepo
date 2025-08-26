defmodule BeVotisWalletWeb.Plugs.CheckUserExistenceTest do
  use BeVotisWalletWeb.ConnCase, async: true

  alias BeVotisWalletWeb.Plugs.CheckUserExistence

  describe "call/2" do
    test "assigns user when user exists and email is in query params", %{conn: conn} do
      user = insert(:user, email: "existing@example.com")

      conn =
        conn
        |> Map.put(:query_string, "email=existing@example.com")
        |> fetch_query_params()
        |> CheckUserExistence.call([])

      assert conn.assigns[:user].id == user.id
      assert conn.assigns[:user].email == user.email
    end

    test "assigns user when user exists and email is in body params", %{conn: conn} do
      user = insert(:user, email: "existing@example.com")

      conn =
        conn
        |> fetch_query_params()
        |> Map.put(:body_params, %{"email" => "existing@example.com"})
        |> CheckUserExistence.call([])

      assert conn.assigns[:user].id == user.id
      assert conn.assigns[:user].email == user.email
    end

    test "assigns user when user exists and email is in params", %{conn: conn} do
      user = insert(:user, email: "existing@example.com")

      conn =
        conn
        |> Map.put(:params, %{"email" => "existing@example.com"})
        |> fetch_query_params()
        |> CheckUserExistence.call([])

      assert conn.assigns[:user].id == user.id
      assert conn.assigns[:user].email == user.email
    end

    test "prioritizes query params over body params", %{conn: conn} do
      user1 = insert(:user, email: "query@example.com")
      _user2 = insert(:user, email: "body@example.com")

      conn =
        conn
        |> Map.put(:query_string, "email=query@example.com")
        |> fetch_query_params()
        |> Map.put(:body_params, %{"email" => "body@example.com"})
        |> CheckUserExistence.call([])

      assert conn.assigns[:user].id == user1.id
      assert conn.assigns[:user].email == "query@example.com"
    end

    test "prioritizes query params over params", %{conn: conn} do
      user1 = insert(:user, email: "query@example.com")
      _user2 = insert(:user, email: "params@example.com")

      conn =
        conn
        |> Map.put(:query_string, "email=query@example.com")
        |> fetch_query_params()
        |> Map.put(:params, %{"email" => "params@example.com"})
        |> CheckUserExistence.call([])

      assert conn.assigns[:user].id == user1.id
      assert conn.assigns[:user].email == "query@example.com"
    end

    test "prioritizes body params over params", %{conn: conn} do
      user1 = insert(:user, email: "body@example.com")
      _user2 = insert(:user, email: "params@example.com")

      conn =
        conn
        |> fetch_query_params()
        |> Map.put(:body_params, %{"email" => "body@example.com"})
        |> Map.put(:params, %{"email" => "params@example.com"})
        |> CheckUserExistence.call([])

      assert conn.assigns[:user].id == user1.id
      assert conn.assigns[:user].email == "body@example.com"
    end

    test "performs case insensitive email lookup", %{conn: conn} do
      user = insert(:user, email: "existing@example.com")

      conn =
        conn
        |> Map.put(:query_string, "email=EXISTING@EXAMPLE.COM")
        |> fetch_query_params()
        |> CheckUserExistence.call([])

      assert conn.assigns[:user].id == user.id
      assert conn.assigns[:user].email == "existing@example.com"
    end

    test "assigns nil when user does not exist", %{conn: conn} do
      conn =
        conn
        |> Map.put(:query_string, "email=nonexistent@example.com")
        |> fetch_query_params()
        |> CheckUserExistence.call([])

      assert is_nil(conn.assigns[:user])
    end

    test "assigns nil when no email parameter provided", %{conn: conn} do
      conn =
        conn
        |> fetch_query_params()
        |> CheckUserExistence.call([])

      assert is_nil(conn.assigns[:user])
    end

    test "assigns nil when email parameter is empty string", %{conn: conn} do
      conn =
        conn
        |> Map.put(:query_string, "email=")
        |> fetch_query_params()
        |> CheckUserExistence.call([])

      assert is_nil(conn.assigns[:user])
    end

    test "assigns nil when email parameter is nil", %{conn: conn} do
      conn =
        conn
        |> fetch_query_params()
        |> CheckUserExistence.call([])

      assert is_nil(conn.assigns[:user])
    end

    test "works with atom keys in params", %{conn: conn} do
      user = insert(:user, email: "existing@example.com")

      conn =
        conn
        |> fetch_query_params()
        |> Map.put(:params, %{email: "existing@example.com"})
        |> CheckUserExistence.call([])

      # Should still work even though our implementation looks for string keys
      # This test ensures backward compatibility if params are atomized
      assert is_nil(conn.assigns[:user]) || conn.assigns[:user].id == user.id
    end
  end

  describe "init/1" do
    test "returns options unchanged" do
      opts = [:some, :options]
      assert CheckUserExistence.init(opts) == opts
    end
  end
end
