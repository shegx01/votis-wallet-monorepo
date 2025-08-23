import Config

# Configure your database
#
# The MIX_TEST_PARTITION environment variable can be used
# to provide built-in test partitioning in CI environment.
# Run `mix help test` for more information.
config :be_votis_wallet, BeVotisWallet.Repo,
  username: "postgres",
  password: "postgres",
  hostname: "localhost",
  database: "be_votis_wallet_test#{System.get_env("MIX_TEST_PARTITION")}",
  pool: Ecto.Adapters.SQL.Sandbox,
  pool_size: System.schedulers_online() * 2

# We don't run a server during test. If one is required,
# you can enable the server option below.
config :be_votis_wallet, BeVotisWalletWeb.Endpoint,
  http: [ip: {127, 0, 0, 1}, port: 4002],
  secret_key_base: "aUY3Rdu/mNOieELVAs+z1arlN9bMFP2bTWOhniQk3n3aQXeH7cW2hwFdAnG7hud7",
  server: false

# In test we don't send emails
config :be_votis_wallet, BeVotisWallet.Mailer, adapter: Swoosh.Adapters.Test

# Disable swoosh api client as it is only required for production adapters
config :swoosh, :api_client, false

# Print only warnings and errors during test
config :logger, level: :warning

# Initialize plugs at runtime for faster test compilation
config :phoenix, :plug_init_mode, :runtime
