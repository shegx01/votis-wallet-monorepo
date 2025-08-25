defmodule BeVotisWalletWeb.UserInfoController.Response do
  @moduledoc """
  Handle user info response

  """
  use BeVotisWalletWeb, :controller

  def show(conn, _params) do
    case conn.assigns[:user] do
      nil ->
        # User not found - return 404 with empty body
        send_resp(conn, 404, "")

      user ->
        # User found - return 200 with base64 encoded org_id
        encoded_org_id = Base.encode64(user.sub_org_id)

        conn
        |> put_status(200)
        |> json(%{org_id: encoded_org_id})
    end
  end
end
