defmodule BeVotisWallet.Application do
  # See https://hexdocs.pm/elixir/Application.html
  # for more information on OTP Applications
  @moduledoc false

  use Application

  @impl true
  def start(_type, _args) do
    children = [
      BeVotisWalletWeb.Telemetry,
      BeVotisWallet.Repo,
      {DNSCluster, query: Application.get_env(:be_votis_wallet, :dns_cluster_query) || :ignore},
      {Phoenix.PubSub, name: BeVotisWallet.PubSub},
      # Start a worker by calling: BeVotisWallet.Worker.start_link(arg)
      # {BeVotisWallet.Worker, arg},
      # Start to serve requests, typically the last entry
      BeVotisWalletWeb.Endpoint
    ]

    # See https://hexdocs.pm/elixir/Supervisor.html
    # for other strategies and supported options
    opts = [strategy: :one_for_one, name: BeVotisWallet.Supervisor]
    Supervisor.start_link(children, opts)
  end

  # Tell Phoenix to update the endpoint configuration
  # whenever the application is updated.
  @impl true
  def config_change(changed, _new, removed) do
    BeVotisWalletWeb.Endpoint.config_change(changed, removed)
    :ok
  end
end
