defmodule BeVotisWalletWeb.Router do
  use BeVotisWalletWeb, :router

  pipeline :api do
    plug :accepts, ["json"]
    plug Plug.Parsers,
      parsers: [:json],
      pass: ["*/*"],
      json_decoder: Phoenix.json_library()
  end

  pipeline :private do
    plug BeVotisWalletWeb.Plug.PrivateAuth
  end

  pipeline :validate_jwt do
    plug BeVotisWalletWeb.Plug.ValidateJwt
  end

  scope "/api", BeVotisWalletWeb do
    pipe_through :api
  end

  scope "/private", BeVotisWalletWeb do
    pipe_through [:api, :private]

    # FOR LLM -- Before implementation, let's create a migration for the user's table
    # ---
    # subOrganization_id: string, indexed
    # wallet_id: string, indexed
    # root_user_ids: string[],
    # subOrganization_name: string, indexed
    # email: string, indexed
    # authenticatorName, string, indexed
    #

    scope "/" do
      pipe_through :validate_jwt
      # returns jwt session
      post "/login", LoginController, :create
    end

    # returns sub_org_id
    post "/sign_up", SignUpController, :create

    #  returns payload for the client to sign with passkey or webauthn if user not found, otherwise returns org_id
    # this endpoint requires user_email as a query param
    get "/user_info", UserInfoController, :show
  end

  # Enable LiveDashboard and Swoosh mailbox preview in development
  if Application.compile_env(:be_votis_wallet, :dev_routes) do
    # If you want to use the LiveDashboard in production, you should put
    # it behind authentication and allow only admins to access it.
    # If your application does not have an admins-only section yet,
    # you can use Plug.BasicAuth to set up some basic authentication
    # as long as you are also using SSL (which you should anyway).
    import Phoenix.LiveDashboard.Router

    scope "/dev" do
      pipe_through [:fetch_session, :protect_from_forgery]

      live_dashboard "/dashboard", metrics: BeVotisWalletWeb.Telemetry
      forward "/mailbox", Plug.Swoosh.MailboxPreview
    end
  end
end
