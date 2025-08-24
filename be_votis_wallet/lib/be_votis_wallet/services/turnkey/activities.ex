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
      activity_type: "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION",
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
      activity_type: "ACTIVITY_TYPE_CREATE_USERS",
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

  # Private helper functions

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

    url = build_activities_url("/public/v1/submit/activity")
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

  defp get_default_organization_id do
    get_config(:organization_id)
  end

  defp get_config(key) do
    case Keyword.get(turnkey_config(), key) do
      {:system, env_var} -> System.get_env(env_var)
      value -> value
    end
  end
end
