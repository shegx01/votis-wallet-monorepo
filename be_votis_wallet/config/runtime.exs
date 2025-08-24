import Config

# config/runtime.exs is executed for all environments, including
# during releases. It is executed after compilation and before the
# system starts, so it is typically used to load production configuration
# and secrets from environment variables or elsewhere. Do not define
# any compile-time configuration in here, as it won't be applied.
# The block below contains prod specific runtime configuration.

# ## Using releases
#
# If you use `mix release`, you need to explicitly enable the server
# by passing the PHX_SERVER=true when you start it:
#
#     PHX_SERVER=true bin/be_votis_wallet start
#
# Alternatively, you can use `mix phx.gen.release` to generate a `bin/server`
# script that automatically sets the env var above.
if System.get_env("PHX_SERVER") do
  config :be_votis_wallet, BeVotisWalletWeb.Endpoint, server: true
end

if config_env() == :prod do
  database_url =
    System.get_env("DATABASE_URL") ||
      raise """
      environment variable DATABASE_URL is missing.
      For example: ecto://USER:PASS@HOST/DATABASE
      """

  maybe_ipv6 = if System.get_env("ECTO_IPV6") in ~w(true 1), do: [:inet6], else: []

  config :be_votis_wallet, BeVotisWallet.Repo,
    # ssl: true,
    url: database_url,
    pool_size: String.to_integer(System.get_env("POOL_SIZE") || "10"),
    # For machines with several cores, consider starting multiple pools of `pool_size`
    # pool_count: 4,
    socket_options: maybe_ipv6

  # The secret key base is used to sign/encrypt cookies and other secrets.
  # A default value is used in config/dev.exs and config/test.exs but you
  # want to use a different value for prod and you most likely don't want
  # to check this value into version control, so we use an environment
  # variable instead.
  secret_key_base =
    System.get_env("SECRET_KEY_BASE") ||
      raise """
      environment variable SECRET_KEY_BASE is missing.
      You can generate one by calling: mix phx.gen.secret
      """

  host = System.get_env("PHX_HOST") || "example.com"
  port = String.to_integer(System.get_env("PORT") || "4000")

  config :be_votis_wallet, :dns_cluster_query, System.get_env("DNS_CLUSTER_QUERY")

  config :be_votis_wallet, BeVotisWalletWeb.Endpoint,
    url: [host: host, port: 443, scheme: "https"],
    http: [
      # Enable IPv6 and bind on all interfaces.
      # Set it to  {0, 0, 0, 0, 0, 0, 0, 1} for local network only access.
      # See the documentation on https://hexdocs.pm/bandit/Bandit.html#t:options/0
      # for details about using IPv6 vs IPv4 and loopback vs public addresses.
      ip: {0, 0, 0, 0, 0, 0, 0, 0},
      port: port
    ],
    secret_key_base: secret_key_base

  # ## SSL Support
  #
  # To get SSL working, you will need to add the `https` key
  # to your endpoint configuration:
  #
  #     config :be_votis_wallet, BeVotisWalletWeb.Endpoint,
  #       https: [
  #         ...,
  #         port: 443,
  #         cipher_suite: :strong,
  #         keyfile: System.get_env("SOME_APP_SSL_KEY_PATH"),
  #         certfile: System.get_env("SOME_APP_SSL_CERT_PATH")
  #       ]
  #
  # The `cipher_suite` is set to `:strong` to support only the
  # latest and more secure SSL ciphers. This means old browsers
  # and clients may not be supported. You can set it to
  # `:compatible` for wider support.
  #
  # `:keyfile` and `:certfile` expect an absolute path to the key
  # and cert in disk or a relative path inside priv, for example
  # "priv/ssl/server.key". For all supported SSL configuration
  # options, see https://hexdocs.pm/plug/Plug.SSL.html#configure/1
  #
  # We also recommend setting `force_ssl` in your config/prod.exs,
  # ensuring no data is ever sent via http, always redirecting to https:
  #
  #     config :be_votis_wallet, BeVotisWalletWeb.Endpoint,
  #       force_ssl: [hsts: true]
  #
  # Check `Plug.SSL` for all available options in `force_ssl`.

  # ## Configuring the mailer
  #
  # In production you need to configure the mailer to use a different adapter.
  # Here is an example configuration for Mailgun:
  #
  #     config :be_votis_wallet, BeVotisWallet.Mailer,
  #       adapter: Swoosh.Adapters.Mailgun,
  #       api_key: System.get_env("MAILGUN_API_KEY"),
  #       domain: System.get_env("MAILGUN_DOMAIN")
  #
  # Most non-SMTP adapters require an API client. Swoosh supports Req, Hackney,
  # and Finch out-of-the-box. This configuration is typically done at
  # compile-time in your config/prod.exs:
  #
  #     config :swoosh, :api_client, Swoosh.ApiClient.Req
  #
  # See https://hexdocs.pm/swoosh/Swoosh.html#module-installation for details.
end

# Turnkey service runtime configuration
# These environment variables are required for production
if config_env() == :prod do
  turnkey_api_key =
    System.get_env("TURNKEY_API_KEY") ||
      raise """
      environment variable TURNKEY_API_KEY is missing.
      This is required for Turnkey API authentication.
      """

  turnkey_api_secret =
    System.get_env("TURNKEY_API_SECRET") ||
      raise """
      environment variable TURNKEY_API_SECRET is missing.
      This is required for Turnkey API authentication.
      """

  turnkey_org_id =
    System.get_env("TURNKEY_ORG_ID") ||
      raise """
      environment variable TURNKEY_ORG_ID is missing.
      This is your Turnkey organization identifier.
      """

  turnkey_private_key_pem =
    System.get_env("TURNKEY_PRIVATE_KEY_PEM") ||
      case System.get_env("TURNKEY_PRIVATE_KEY_PATH") do
        nil -> 
          raise """
          environment variable TURNKEY_PRIVATE_KEY_PEM or TURNKEY_PRIVATE_KEY_PATH is missing.
          This is required for Turnkey API request signing.
          Either provide the PEM-encoded private key directly via TURNKEY_PRIVATE_KEY_PEM,
          or provide a file path to the private key via TURNKEY_PRIVATE_KEY_PATH.
          """
        path when is_binary(path) ->
          case File.read(path) do
            {:ok, pem} -> pem
            {:error, reason} -> 
              raise """
              Failed to read Turnkey private key from path: #{path}
              Error: #{inspect(reason)}
              Ensure the file exists and is readable.
              """
          end
      end

  config :be_votis_wallet, :turnkey,
    base_url: System.get_env("TURNKEY_BASE_URL") || "https://api.turnkey.com",
    api_key: turnkey_api_key,
    api_secret: turnkey_api_secret,
    organization_id: turnkey_org_id,
    api_private_key: turnkey_private_key_pem
end

# For development environment, allow environment variable override
if config_env() == :dev do
  # Only override if environment variables are actually set
  # This allows development with environment variables without breaking defaults
  dev_config = %{}

  dev_config =
    if base_url = System.get_env("TURNKEY_BASE_URL") do
      Map.put(dev_config, :base_url, base_url)
    else
      Map.put(dev_config, :base_url, "https://api.turnkey.com")
    end

  dev_config =
    if api_key = System.get_env("TURNKEY_API_KEY") do
      Map.put(dev_config, :api_key, api_key)
    else
      dev_config
    end

  dev_config =
    if api_secret = System.get_env("TURNKEY_API_SECRET") do
      Map.put(dev_config, :api_secret, api_secret)
    else
      dev_config
    end

  dev_config =
    if org_id = System.get_env("TURNKEY_ORG_ID") do
      Map.put(dev_config, :organization_id, org_id)
    else
      dev_config
    end

  # Add private key configuration for development (optional)
  dev_config =
    case {System.get_env("TURNKEY_PRIVATE_KEY_PEM"), System.get_env("TURNKEY_PRIVATE_KEY_PATH")} do
      {pem, _} when is_binary(pem) ->
        Map.put(dev_config, :api_private_key, pem)
      {nil, path} when is_binary(path) ->
        case File.read(path) do
          {:ok, pem} -> Map.put(dev_config, :api_private_key, pem)
          {:error, _} -> dev_config
        end
      {nil, nil} ->
        dev_config
    end

  if not Enum.empty?(dev_config) do
    config :be_votis_wallet, :turnkey, dev_config
  end
end
