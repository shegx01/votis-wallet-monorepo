defmodule BeVotisWallet.Services.Turnkey.Activities do
  @moduledoc """
  Turnkey Activities API service for state-changing operations.

  This module handles all Turnkey activity operations that modify state,
  such as creating organizations, users, wallets, and signing transactions.

  Activities require more customization for authentication, signing,
  and request formatting compared to simple query operations.
  """

  require Logger

  alias BeVotisWallet.Services.Turnkey.Crypto

  @type activity_type :: String.t()
  @type activity_params :: map()
  @type organization_id :: String.t()
  @type user_id :: String.t()
  @type activity_result :: {:ok, map()} | {:error, integer(), term()}
  @path "/public/v1/submit"

  # HTTP client module is injected via configuration
  defp http_client do
    Application.get_env(:be_votis_wallet, :http_client)
  end

  defp turnkey_config do
    Application.get_env(:be_votis_wallet, :turnkey, [])
  end

  @doc """
  Create a sub-organization under the main Turnkey organization.

  ## Parameters
  - `name` - Human-readable name for the sub-organization
  - `opts` - Additional options (e.g., `:root_users`, `:root_quorum_threshold`, `:auth_type`)

  ## Returns
  - `{:ok, response}` - Success with organization data including `organizationId`
  - `{:error, status_code, error_message}` - Failure response

  ## Example
      {:ok, %{"activity" => %{"result" => %{"createSubOrganizationResult" => %{"organizationId" => org_id}}}}}
  """
  def create_sub_organization(name, opts \\ []) do
    activity_params =
      %{
        "subOrganizationName" => name,
        "rootUsers" => Keyword.get(opts, :root_users, []),
        "rootQuorumThreshold" => Keyword.get(opts, :root_quorum_threshold, 1)
      }

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7",
      params: activity_params,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided (required for WebAuthn)
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Create a user within a Turnkey organization.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `user_name` - Unique username within the organization
  - `user_email` - User email address
  - `opts` - Additional options:
    - `:api_keys` - List of API keys for the user
    - `:authenticators` - List of authenticators for the user
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with user data including `userId`
  - `{:error, status_code, error_message}` - Failure response
  """
  def create_user(organization_id, user_name, user_email, opts \\ []) do
    activity_params = %{
      "userName" => user_name,
      "userEmail" => user_email,
      "apiKeys" => Keyword.get(opts, :api_keys, []),
      "authenticators" => Keyword.get(opts, :authenticators, [])
    }

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_USERS_V7",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Create a wallet for a user with specified accounts.

  ## Parameters
  - `organization_id` - Organization UUID
  - `user_id` - User UUID who will own the wallet
  - `wallet_name` - Name for the wallet
  - `accounts` - List of account specifications with curve and path info

  ## Returns
  - `{:ok, response}` - Success with wallet data including `walletId`
  - `{:error, status_code, error_message}` - Failure response

  ## Example accounts parameter
      [%{
        "curve" => "CURVE_SECP256K1",
        "pathFormat" => "PATH_FORMAT_BIP32",
        "path" => "m/44'/60'/0'/0/0",
        "addressFormat" => "ADDRESS_FORMAT_ETHEREUM"
      }]
  """
  def create_wallet(organization_id, user_id, wallet_name, accounts, opts \\ []) do
    activity_params = %{
      "userId" => user_id,
      "walletName" => wallet_name,
      "accounts" => accounts
    }

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_WALLET",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Create an account within an existing wallet.

  ## Parameters
  - `organization_id` - Organization UUID
  - `wallet_id` - Target wallet UUID
  - `account_spec` - Account specification with curve, path, and address format
  - `opts` - Additional options:
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with account data
  - `{:error, status_code, error_message}` - Failure response
  """
  def create_wallet_account(organization_id, wallet_id, account_spec, opts \\ []) do
    activity_params = %{
      "walletId" => wallet_id,
      "accounts" => [account_spec]
    }

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_WALLET_ACCOUNTS",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Sign a transaction using a Turnkey wallet account.

  ## Parameters
  - `organization_id` - Organization UUID
  - `sign_with` - Signing specification (private key ID or path)
  - `unsigned_transaction` - Transaction data to sign
  - `opts` - Additional signing options:
    - `:transaction_type` - Type of transaction (default: "TRANSACTION_TYPE_ETHEREUM")
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with signed transaction
  - `{:error, status_code, error_message}` - Failure response
  """
  def sign_transaction(organization_id, sign_with, unsigned_transaction, opts \\ []) do
    activity_params = %{
      "signWith" => sign_with,
      "unsignedTransaction" => unsigned_transaction,
      "type" => Keyword.get(opts, :transaction_type, "TRANSACTION_TYPE_ETHEREUM")
    }

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_SIGN_TRANSACTION_V2",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Create a read-only session for a user.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `user_id` - Optional user UUID (if not provided, uses the authenticated user)
  - `opts` - Additional options:
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with session data including JWT token
  - `{:error, status_code, error_message}` - Failure response

  ## Example response
      {:ok, %{"activity" => %{"result" => %{"createReadOnlySessionResult" => %{
        "sessionToken" => "eyJ...",
        "userId" => "user-id",
        "organizationId" => "org-id"
      }}}}}
  """
  def create_read_only_session(organization_id, user_id \\ nil, opts \\ []) do
    activity_params = %{
      "userId" => user_id
    }

    # Remove nil values
    activity_params = Enum.reject(activity_params, fn {_, v} -> is_nil(v) end) |> Enum.into(%{})

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_READ_ONLY_SESSION",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Create a read-write session for a user.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `target_public_key` - Client-side public key (hex) for HPKE encryption of credentials
  - `user_id` - Optional user UUID (if not provided, uses the authenticated user)
  - `opts` - Additional options:
    - `:api_key_name` - Optional human-readable name for the API key
    - `:expiration_seconds` - Expiration window in seconds (default: 900 = 15 minutes)
    - `:invalidate_existing` - Whether to invalidate other read-write session keys
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with session data including encrypted credential bundle
  - `{:error, status_code, error_message}` - Failure response

  ## Example response
      {:ok, %{"activity" => %{"result" => %{"createReadWriteSessionResultV2" => %{
        "apiKeyId" => "api-key-id",
        "credentialBundle" => "encrypted-bundle-hex"
      }}}}}
  """
  def create_read_write_session(organization_id, target_public_key, user_id \\ nil, opts \\ []) do
    activity_params = %{
      "targetPublicKey" => target_public_key,
      "userId" => user_id,
      "apiKeyName" => Keyword.get(opts, :api_key_name),
      "expirationSeconds" => to_string(Keyword.get(opts, :expiration_seconds, 900)),
      "invalidateExisting" => Keyword.get(opts, :invalidate_existing, false)
    }

    # Remove nil values
    activity_params = Enum.reject(activity_params, fn {_, v} -> is_nil(v) end) |> Enum.into(%{})

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_READ_WRITE_SESSION_V2",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Create policies for an organization.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `policies` - List of policy specifications:
    - `policyName` - Human-readable name for the policy
    - `effect` - Policy effect (e.g., "EFFECT_ALLOW")
    - `condition` - Policy condition expression
    - `consensus` - Consensus requirements
    - `notes` - Optional policy description/notes
  - `opts` - Additional options:
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with policy creation data
  - `{:error, status_code, error_message}` - Failure response

  ## Example
      {:ok, %{"activity" => %{"result" => %{"createPoliciesResult" => %{...}}}}}
  """
  def create_policies(organization_id, policies, opts \\ []) do
    activity_params = %{
      "policies" => policies
    }

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_POLICIES",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Create OAuth providers for a user.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `user_id` - User UUID to add OAuth providers to
  - `oauth_providers` - List of OAuth provider specifications:
    - `providerName` - Name of the OAuth provider
    - `oidcToken` - OIDC token for the provider
  - `opts` - Additional options:
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with OAuth provider creation data
  - `{:error, status_code, error_message}` - Failure response

  ## Example
      {:ok, %{"activity" => %{"result" => %{"createOauthProvidersResult" => %{...}}}}}
  """
  def create_oauth_providers(organization_id, user_id, oauth_providers, opts \\ []) do
    activity_params = %{
      "userId" => user_id,
      "oauthProviders" => oauth_providers
    }

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_CREATE_OAUTH_PROVIDERS",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Perform OAuth login and create a session.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `oidc_token` - Base64 encoded OIDC token
  - `public_key` - Client-side public key for session encryption
  - `opts` - Additional options:
    - `:expiration_seconds` - Expiration window in seconds (default: 900 = 15 minutes)
    - `:invalidate_existing` - Whether to invalidate other session keys
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for WebAuthn/Passkey)

  ## Returns
  - `{:ok, response}` - Success with session data
  - `{:error, status_code, error_message}` - Failure response

  ## Example
      {:ok, %{"activity" => %{"result" => %{"oauthLoginResult" => %{"session" => "token-data"}}}}}
  """
  def oauth_login(organization_id, oidc_token, public_key, opts \\ []) do
    activity_params = %{
      "oidcToken" => oidc_token,
      "publicKey" => public_key,
      "expirationSeconds" => to_string(Keyword.get(opts, :expiration_seconds, 900)),
      "invalidateExisting" => Keyword.get(opts, :invalidate_existing, false)
    }

    # Remove nil values
    activity_params = Enum.reject(activity_params, fn {_, v} -> is_nil(v) end) |> Enum.into(%{})

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_OAUTH_LOGIN",
      params: activity_params,
      organization_id: organization_id,
      auth_type: Keyword.get(opts, :auth_type, :api_key)
    ]

    # Add optional client_signature if provided
    execute_activity_opts =
      case Keyword.get(opts, :client_signature) do
        nil -> execute_activity_opts
        signature -> Keyword.put(execute_activity_opts, :client_signature, signature)
      end

    execute_activity(execute_activity_opts)
  end

  @doc """
  Perform stamp login (via passkey/WebAuthn) and create a session.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `public_key` - Client-side public key for session encryption
  - `opts` - Additional options:
    - `:expiration_seconds` - Expiration window in seconds (default: 900 = 15 minutes)
    - `:invalidate_existing` - Whether to invalidate other session keys
    - `:auth_type` - Authentication type (`:webauthn`, `:passkey`)
    - `:client_signature` - Pre-computed signature from client (required for this function)

  ## Returns
  - `{:ok, response}` - Success with session data
  - `{:error, status_code, error_message}` - Failure response

  ## Example
      {:ok, %{"activity" => %{"result" => %{"stampLoginResult" => %{"session" => "token-data"}}}}}
  """
  def stamp_login(organization_id, public_key, opts \\ []) do
    client_signature = Keyword.get(opts, :client_signature)

    unless client_signature do
      Logger.error("Client signature is required for stamp login")
      raise ArgumentError, "client_signature is required for stamp_login"
    end

    activity_params = %{
      "publicKey" => public_key,
      "expirationSeconds" => to_string(Keyword.get(opts, :expiration_seconds, 900)),
      "invalidateExisting" => Keyword.get(opts, :invalidate_existing, false)
    }

    # Remove nil values
    activity_params = Enum.reject(activity_params, fn {_, v} -> is_nil(v) end) |> Enum.into(%{})

    auth_type = Keyword.get(opts, :auth_type, :passkey)

    auth_type =
      unless auth_type in [:webauthn, :passkey] do
        Logger.warning(
          "Auth type #{inspect(auth_type)} not appropriate for stamp_login, using :passkey"
        )

        :passkey
      else
        auth_type
      end

    execute_activity_opts = [
      activity_type: "ACTIVITY_TYPE_STAMP_LOGIN",
      params: activity_params,
      organization_id: organization_id,
      auth_type: auth_type,
      client_signature: client_signature
    ]

    execute_activity(execute_activity_opts)
  end

  # Private helper functions

  @doc """
  Execute a signed request to Turnkey using the activity type to determine the endpoint.

  This is the unified implementation for handling pre-signed requests from clients
  (mobile apps, web clients) that have already built the complete request body and
  signature. The endpoint is determined using the existing activity type mapping.

  ## Parameters
  - `stamped_body` - The complete binary request body from the client
  - `stamp` - The signature from the client (OAuth, WebAuthn, or Passkey)
  - `activity_type` - The activity type (e.g., "ACTIVITY_TYPE_OAUTH_LOGIN", "ACTIVITY_TYPE_STAMP_LOGIN")
  - `opts` - Additional options:
    - `:auth_type` - Authentication type (`:api_key`, `:webauthn`, `:passkey`) (default: `:passkey`)

  ## Returns
  - `{:ok, response}` - Success response from Turnkey
  - `{:error, status_code, error_message}` - Failure response

  ## Examples
      # OAuth login
      execute_signed_request(body, stamp, "ACTIVITY_TYPE_OAUTH_LOGIN", auth_type: :api_key)

      # Stamp/Passkey login
      execute_signed_request(body, stamp, "ACTIVITY_TYPE_STAMP_LOGIN", auth_type: :passkey)

      # Create authenticators
      execute_signed_request(body, stamp, "ACTIVITY_TYPE_CREATE_AUTHENTICATORS_V2", auth_type: :api_key)
  """
  def execute_signed_request(stamped_body, stamp, activity_type, opts \\ []) do
    auth_type = Keyword.get(opts, :auth_type, :passkey)

    if auth_type in [:api_key, :webauthn, :passkey] do
      # Build headers with client signature
      headers = build_client_signed_headers(stamp, auth_type)

      # Use existing endpoint mapping to get the correct URL
      endpoint = get_activity_endpoint(activity_type)
      url = build_activities_url(endpoint)
      payload = http_client().build_payload(:post, url, headers, stamped_body)

      case http_client().request(payload) do
        {:ok, data} ->
          Logger.info("Successfully executed signed Turnkey request",
            activity_type: activity_type,
            auth_type: auth_type,
            endpoint: endpoint,
            activity_id: get_in(data, ["activity", "id"])
          )

          {:ok, data}

        {:error, status_code, error_message} ->
          Logger.error("Failed to execute signed Turnkey request",
            activity_type: activity_type,
            auth_type: auth_type,
            endpoint: endpoint,
            status_code: status_code,
            error: inspect(error_message)
          )

          {:error, status_code, error_message}
      end
    else
      Logger.error("Invalid auth type for signed request", auth_type: auth_type)
      {:error, 400, "Invalid auth type for signed request"}
    end
  end

  defp execute_activity(opts) when is_list(opts) do
    # Required arguments - use fetch! for clear error messages
    activity_type = Keyword.fetch!(opts, :activity_type)
    params = Keyword.fetch!(opts, :params)

    # Optional arguments with defaults
    org_id = Keyword.get(opts, :organization_id) || get_default_organization_id()
    auth_type = Keyword.get(opts, :auth_type, :api_key)
    client_signature = Keyword.get(opts, :client_signature)

    activity_body = build_activity_request(activity_type, params, org_id)
    json_body = Jason.encode!(activity_body)

    # Build headers with signature using specified auth type and optional client signature
    headers = build_activities_headers_with_signature(json_body, auth_type, client_signature)

    # Use specific endpoint based on activity type
    endpoint = get_activity_endpoint(activity_type)
    url = build_activities_url(endpoint)
    payload = http_client().build_payload(:post, url, headers, json_body)

    case http_client().request(payload) do
      {:ok, data} ->
        Logger.info("Successfully executed Turnkey activity",
          type: activity_type,
          organization_id: org_id,
          activity_id: get_in(data, ["activity", "id"])
        )

        {:ok, data}

      {:error, status_code, error_message} ->
        Logger.error("Failed to execute Turnkey activity",
          type: activity_type,
          organization_id: org_id,
          status_code: status_code,
          error: inspect(error_message)
        )

        {:error, status_code, error_message}
    end
  end

  defp build_activity_request(activity_type, params, organization_id) do
    %{
      "type" => activity_type,
      "timestampMs" => to_string(System.system_time(:millisecond)),
      "organizationId" => organization_id,
      "parameters" => params
    }
  end

  defp build_activities_headers_with_signature(request_body, auth_type, client_signature) do
    base_headers = build_activities_headers()

    case {auth_type, client_signature} do
      # Client-provided signature (WebAuthn/Passkey) - mobile clients
      {:webauthn, signature} when is_binary(signature) ->
        stamp_header = get_stamp_header_name(:webauthn)
        [{stamp_header, signature} | base_headers]

      {:passkey, signature} when is_binary(signature) ->
        stamp_header = get_stamp_header_name(:passkey)
        [{stamp_header, signature} | base_headers]

      # Server-side API key signing
      {:api_key, nil} ->
        case get_config(:api_private_key) do
          nil ->
            Logger.warning("No API private key configured - request will be unsigned")
            base_headers

          private_key_pem when is_binary(private_key_pem) ->
            case Crypto.create_request_stamp(request_body, private_key_pem) do
              {:ok, stamp} ->
                stamp_header = get_stamp_header_name(:api_key)
                [{stamp_header, stamp} | base_headers]

              {:error, reason} ->
                Logger.error("Failed to sign request", reason: reason)
                base_headers
            end

          invalid_key ->
            Logger.error("Invalid API private key format", key_type: inspect(invalid_key))
            base_headers
        end

      # Error cases
      {auth_type, nil} when auth_type in [:webauthn, :passkey] ->
        Logger.error("Client signature required for #{auth_type} authentication")
        base_headers

      {auth_type, signature} when auth_type == :api_key and is_binary(signature) ->
        Logger.warning("Ignoring client signature for API key authentication")
        build_activities_headers_with_signature(request_body, :api_key, nil)

      _ ->
        Logger.warning("Unknown auth configuration",
          auth_type: auth_type,
          has_signature: is_binary(client_signature)
        )

        base_headers
    end
  end

  defp get_stamp_header_name(:api_key), do: "X-Stamp"
  defp get_stamp_header_name(:webauthn), do: "X-Stamp-WebAuthn"
  # Alias for passkey
  defp get_stamp_header_name(:passkey), do: "X-Stamp-WebAuthn"

  defp build_activities_url(path) do
    base_url = get_config(:base_url)
    base_url <> path
  end

  defp build_activities_headers do
    api_key = get_config(:api_key)

    [
      {"Content-Type", "application/json"},
      {"X-Turnkey-API-Key", api_key}
    ]
  end

  defp build_client_signed_headers(stamp, auth_type) do
    base_headers = build_activities_headers()
    stamp_header = get_stamp_header_name(auth_type)
    [{stamp_header, stamp} | base_headers]
  end

  defp get_default_organization_id do
    get_config(:organization_id)
  end

  defp get_config(key) do
    case Keyword.get(turnkey_config(), key) do
      {:system, env_var} -> System.get_env(env_var)
      value -> value
    end
  end

  # Map activity types to their specific Turnkey endpoints
  defp get_activity_endpoint(activity_type) do
    case activity_type do
      # Authentication endpoints
      "ACTIVITY_TYPE_STAMP_LOGIN" ->
        Path.join(@path, "/stamp_login")

      "ACTIVITY_TYPE_OAUTH_LOGIN" ->
        Path.join(@path, "/oauth_login")

      "ACTIVITY_TYPE_OTP_LOGIN" ->
        Path.join(@path, "/otp_login")

      "ACTIVITY_TYPE_OAUTH" ->
        Path.join(@path, "/oauth")

      "ACTIVITY_TYPE_OTP_AUTH" ->
        Path.join(@path, "/otp_auth")

      "ACTIVITY_TYPE_EMAIL_AUTH_V2" ->
        Path.join(@path, "/email_auth")

      # Organization and user management
      "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION_V7" ->
        Path.join(@path, "/create_sub_organization")

      "ACTIVITY_TYPE_CREATE_USERS_V7" ->
        Path.join(@path, "/create_users")

      "ACTIVITY_TYPE_CREATE_USERS_V3" ->
        Path.join(@path, "/create_users")

      "ACTIVITY_TYPE_DELETE_SUB_ORGANIZATION" ->
        Path.join(@path, "/delete_sub_organization")

      "ACTIVITY_TYPE_DELETE_USERS" ->
        Path.join(@path, "/delete_users")

      "ACTIVITY_TYPE_UPDATE_USER" ->
        Path.join(@path, "/update_user")

      "ACTIVITY_TYPE_UPDATE_USER_NAME" ->
        Path.join(@path, "/update_user_name")

      "ACTIVITY_TYPE_UPDATE_USER_EMAIL" ->
        Path.join(@path, "/update_user_email")

      "ACTIVITY_TYPE_UPDATE_USER_PHONE_NUMBER" ->
        Path.join(@path, "/update_user_phone_number")

      "ACTIVITY_TYPE_RECOVER_USER" ->
        Path.join(@path, "/recover_user")

      "ACTIVITY_TYPE_INIT_USER_EMAIL_RECOVERY" ->
        Path.join(@path, "/init_user_email_recovery")

      # API keys and authentication credentials
      "ACTIVITY_TYPE_CREATE_API_KEYS_V2" ->
        Path.join(@path, "/create_api_keys")

      "ACTIVITY_TYPE_DELETE_API_KEYS" ->
        Path.join(@path, "/delete_api_keys")

      "ACTIVITY_TYPE_CREATE_AUTHENTICATORS_V2" ->
        Path.join(@path, "/create_authenticators")

      "ACTIVITY_TYPE_DELETE_AUTHENTICATORS" ->
        Path.join(@path, "/delete_authenticators")

      # Sessions
      "ACTIVITY_TYPE_CREATE_READ_ONLY_SESSION" ->
        Path.join(@path, "/create_read_only_session")

      "ACTIVITY_TYPE_CREATE_READ_WRITE_SESSION_V2" ->
        Path.join(@path, "/create_read_write_session")

      # Wallet management
      "ACTIVITY_TYPE_CREATE_WALLET" ->
        Path.join(@path, "/create_wallet")

      "ACTIVITY_TYPE_CREATE_WALLET_ACCOUNTS" ->
        Path.join(@path, "/create_wallet_accounts")

      "ACTIVITY_TYPE_DELETE_WALLETS" ->
        Path.join(@path, "/delete_wallets")

      "ACTIVITY_TYPE_UPDATE_WALLET" ->
        Path.join(@path, "/update_wallet")

      "ACTIVITY_TYPE_EXPORT_WALLET" ->
        Path.join(@path, "/export_wallet")

      "ACTIVITY_TYPE_EXPORT_WALLET_ACCOUNT" ->
        Path.join(@path, "/export_wallet_account")

      "ACTIVITY_TYPE_IMPORT_WALLET" ->
        Path.join(@path, "/import_wallet")

      "ACTIVITY_TYPE_INIT_IMPORT_WALLET" ->
        Path.join(@path, "/init_import_wallet")

      # Private key management
      "ACTIVITY_TYPE_CREATE_PRIVATE_KEYS_V2" ->
        Path.join(@path, "/create_private_keys")

      "ACTIVITY_TYPE_DELETE_PRIVATE_KEYS" ->
        Path.join(@path, "/delete_private_keys")

      "ACTIVITY_TYPE_EXPORT_PRIVATE_KEY" ->
        Path.join(@path, "/export_private_key")

      "ACTIVITY_TYPE_IMPORT_PRIVATE_KEY" ->
        Path.join(@path, "/import_private_key")

      "ACTIVITY_TYPE_INIT_IMPORT_PRIVATE_KEY" ->
        Path.join(@path, "/init_import_private_key")

      "ACTIVITY_TYPE_CREATE_PRIVATE_KEY_TAG" ->
        Path.join(@path, "/create_private_key_tag")

      "ACTIVITY_TYPE_DELETE_PRIVATE_KEY_TAGS" ->
        Path.join(@path, "/delete_private_key_tags")

      "ACTIVITY_TYPE_UPDATE_PRIVATE_KEY_TAG" ->
        Path.join(@path, "/update_private_key_tag")

      # Transaction signing
      "ACTIVITY_TYPE_SIGN_TRANSACTION_V2" ->
        Path.join(@path, "/sign_transaction")

      "ACTIVITY_TYPE_SIGN_RAW_PAYLOAD_V2" ->
        Path.join(@path, "/sign_raw_payload")

      "ACTIVITY_TYPE_SIGN_RAW_PAYLOADS" ->
        Path.join(@path, "/sign_raw_payloads")

      # Policy management
      "ACTIVITY_TYPE_CREATE_POLICIES" ->
        Path.join(@path, "/create_policies")

      "ACTIVITY_TYPE_CREATE_POLICY_V3" ->
        Path.join(@path, "/create_policy")

      "ACTIVITY_TYPE_DELETE_POLICY" ->
        Path.join(@path, "/delete_policy")

      "ACTIVITY_TYPE_UPDATE_POLICY_V2" ->
        Path.join(@path, "/update_policy")

      "ACTIVITY_TYPE_APPROVE_ACTIVITY" ->
        Path.join(@path, "/approve_activity")

      "ACTIVITY_TYPE_REJECT_ACTIVITY" ->
        Path.join(@path, "/reject_activity")

      # OAuth providers
      "ACTIVITY_TYPE_CREATE_OAUTH_PROVIDERS" ->
        Path.join(@path, "/create_oauth_providers")

      "ACTIVITY_TYPE_DELETE_OAUTH_PROVIDERS" ->
        Path.join(@path, "/delete_oauth_providers")

      # OTP management
      "ACTIVITY_TYPE_INIT_OTP" ->
        Path.join(@path, "/init_otp")

      "ACTIVITY_TYPE_INIT_OTP_AUTH_V2" ->
        Path.join(@path, "/init_otp_auth")

      "ACTIVITY_TYPE_VERIFY_OTP" ->
        Path.join(@path, "/verify_otp")

      # Organization features
      "ACTIVITY_TYPE_SET_ORGANIZATION_FEATURE" ->
        Path.join(@path, "/set_organization_feature")

      "ACTIVITY_TYPE_REMOVE_ORGANIZATION_FEATURE" ->
        Path.join(@path, "/remove_organization_feature")

      "ACTIVITY_TYPE_UPDATE_ROOT_QUORUM" ->
        Path.join(@path, "/update_root_quorum")

      # User and key tagging
      "ACTIVITY_TYPE_CREATE_USER_TAG" ->
        Path.join(@path, "/create_user_tag")

      "ACTIVITY_TYPE_DELETE_USER_TAGS" ->
        Path.join(@path, "/delete_user_tags")

      "ACTIVITY_TYPE_UPDATE_USER_TAG" ->
        Path.join(@path, "/update_user_tag")

      # Invitations
      "ACTIVITY_TYPE_CREATE_INVITATIONS" ->
        Path.join(@path, "/create_invitations")

      "ACTIVITY_TYPE_DELETE_INVITATION" ->
        Path.join(@path, "/delete_invitation")

      # Smart contracts
      "ACTIVITY_TYPE_CREATE_SMART_CONTRACT_INTERFACE" ->
        Path.join(@path, "/create_smart_contract_interface")

      "ACTIVITY_TYPE_DELETE_SMART_CONTRACT_INTERFACE" ->
        Path.join(@path, "/delete_smart_contract_interface")

      # Fiat on-ramp
      "ACTIVITY_TYPE_INIT_FIAT_ON_RAMP" ->
        Path.join(@path, "/init_fiat_on_ramp")

      # Default fallback for any unknown activity types
      _ ->
        Path.join(@path, "/activity")
    end
  end
end
