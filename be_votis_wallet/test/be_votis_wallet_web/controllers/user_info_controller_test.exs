defmodule BeVotisWalletWeb.UserInfoControllerTest do
  use BeVotisWalletWeb.ConnCase, async: true

  describe "GET /private/user_info" do
    test "returns 200 with base64 encoded org_id when user exists", %{conn: conn} do
      user = insert(:user)
      
      conn = get(conn, ~p"/private/user_info?email=#{user.email}")
      
      assert json_response(conn, 200) == %{
        "org_id" => Base.encode64(user.sub_org_id)
      }
    end

    test "returns 404 when user does not exist", %{conn: conn} do
      conn = get(conn, ~p"/private/user_info?email=nonexistent@example.com")
      
      assert response(conn, 404) == ""
    end

    test "returns 404 when email parameter is missing", %{conn: conn} do
      conn = get(conn, ~p"/private/user_info")
      
      assert response(conn, 404) == ""
    end

    test "returns 404 when email parameter is empty", %{conn: conn} do
      conn = get(conn, ~p"/private/user_info?email=")
      
      assert response(conn, 404) == ""
    end

    test "case insensitive email lookup", %{conn: conn} do
      user = insert(:user, %{email: "test@example.com"})
      
      conn = get(conn, ~p"/private/user_info?email=TEST@EXAMPLE.COM")
      
      assert json_response(conn, 200) == %{
        "org_id" => Base.encode64(user.sub_org_id)
      }
    end

    test "handles whitespace in email parameter", %{conn: conn} do
      user = insert(:user, %{email: "test@example.com"})
      
      conn = get(conn, ~p"/private/user_info?email= test@example.com ")
      
      assert json_response(conn, 200) == %{
        "org_id" => Base.encode64(user.sub_org_id)
      }
    end
  end
end
