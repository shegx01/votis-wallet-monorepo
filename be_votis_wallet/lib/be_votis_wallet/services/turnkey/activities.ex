defmodule BeVotisWallet.Services.Turnkey.Activities do
  @moduledoc """
  Turnkey Activities API service for state-changing operations.
  
  This module handles all Turnkey activity operations that modify state,
  such as creating organizations, users, wallets, and signing transactions.
  
  Activities require more customization for authentication, signing,
  and request formatting compared to simple query operations.
  """

  require Logger

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
  - `opts` - Additional options (e.g., `:root_users`, `:root_quorum_threshold`)
  
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
    
    execute_activity("ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION", activity_params)
  end

  @doc """
  Create a user within a Turnkey organization.
  
  ## Parameters
  - `organization_id` - Target organization UUID
  - `user_name` - Unique username within the organization
  - `user_email` - User email address
  - `opts` - Additional options (e.g., `:api_keys`, `:authenticators`)
  
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
    
    execute_activity("ACTIVITY_TYPE_CREATE_USERS", activity_params, organization_id)
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
  def create_wallet(organization_id, user_id, wallet_name, accounts) do
    activity_params = %{
      "userId" => user_id,
      "walletName" => wallet_name,
      "accounts" => accounts
    }
    
    execute_activity("ACTIVITY_TYPE_CREATE_WALLET", activity_params, organization_id)
  end

  @doc """
  Create an account within an existing wallet.
  
  ## Parameters
  - `organization_id` - Organization UUID
  - `wallet_id` - Target wallet UUID
  - `account_spec` - Account specification with curve, path, and address format
  
  ## Returns
  - `{:ok, response}` - Success with account data
  - `{:error, status_code, error_message}` - Failure response
  """
  def create_account(organization_id, wallet_id, account_spec) do
    activity_params = %{
      "walletId" => wallet_id,
      "accounts" => [account_spec]
    }
    
    execute_activity("ACTIVITY_TYPE_CREATE_WALLET_ACCOUNTS", activity_params, organization_id)
  end

  @doc """
  Sign a transaction using a Turnkey wallet account.
  
  ## Parameters
  - `organization_id` - Organization UUID
  - `sign_with` - Signing specification (private key ID or path)
  - `unsigned_transaction` - Transaction data to sign
  - `opts` - Additional signing options
  
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
    
    execute_activity("ACTIVITY_TYPE_SIGN_TRANSACTION_V2", activity_params, organization_id)
  end

  # Private helper functions

  defp execute_activity(activity_type, params, organization_id \\ nil) do
    org_id = organization_id || get_default_organization_id()
    
    activity_body = build_activity_request(activity_type, params, org_id)
    
    # Sign the request if API secret is configured
    signed_body = maybe_sign_request(activity_body)
    
    url = build_activities_url("/public/v1/submit/activity")
    headers = build_activities_headers()
    json_body = Jason.encode!(signed_body)
    
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

  defp maybe_sign_request(activity_body) do
    # In a real implementation, this would use the API secret to sign the request
    # For now, we'll just return the unsigned body
    # TODO: Implement proper request signing with API key pairs
    activity_body
  end

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
