# This file is responsible for configuring your application
# and its dependencies with the aid of the Config module.
#
# This configuration file is loaded before any dependency and
# is restricted to this project.

# General application configuration
import Config

config :be_votis_wallet,
  ecto_repos: [BeVotisWallet.Repo],
  generators: [timestamp_type: :utc_datetime]

# Configures the endpoint
config :be_votis_wallet, BeVotisWalletWeb.Endpoint,
  url: [host: "localhost"],
  adapter: Bandit.PhoenixAdapter,
  render_errors: [
    formats: [json: BeVotisWalletWeb.ErrorJSON],
    layout: false
  ],
  pubsub_server: BeVotisWallet.PubSub,
  live_view: [signing_salt: "dKICKf9F"]

# Configures the mailer
#
# By default it uses the "Local" adapter which stores the emails
# locally. You can see the emails in your browser, at "/dev/mailbox".
#
# For production it's recommended to configure a different adapter
# at the `config/runtime.exs`.
config :be_votis_wallet, BeVotisWallet.Mailer, adapter: Swoosh.Adapters.Local

# Configures Elixir's Logger
config :logger, :default_formatter,
  format: "$time $metadata[$level] $message\n",
  metadata: [:request_id]

# Use Jason for JSON parsing in Phoenix
config :phoenix, :json_library, Jason

# HTTP Client configuration
config :be_votis_wallet, :http_client, BeVotisWallet.HTTPClient.FinchClient

# Turnkey service configuration is now in config/runtime.exs
# This allows for proper environment variable handling at runtime

# Custom blockchain configurations
# Add support for additional chains beyond the 78+ built-in ones
# Each chain should follow the BeVotisWallet.ChainConfig.Chain struct format
# Custom chains take precedence over built-in chains with the same key
# Example:
# config :be_votis_wallet, :custom_chains, %{
#   my_custom_chain: %BeVotisWallet.ChainConfig.Chain{
#     chain_id: nil,
#     name: "My Custom Chain",
#     symbol: "MCC",
#     symbol_aliases: [],
#     curve: "CURVE_SECP256K1",
#     address_format: "ADDRESS_FORMAT_ETHEREUM",
#     path: "m/44'/9999'/0'/0/0",
#     path_format: "PATH_FORMAT_BIP32",
#     slip44_coin_type: 9999,
#     is_evm_compatible: true
#   }
# }
config :be_votis_wallet, :custom_chains, %{}

# Import environment specific config. This must remain at the bottom
# of this file so it overrides the configuration defined above.
import_config "#{config_env()}.exs"
