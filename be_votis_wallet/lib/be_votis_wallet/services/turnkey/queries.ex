defmodule BeVotisWallet.Services.Turnkey.Queries do
  @moduledoc """
  Turnkey Queries API service for read-only operations.

  This module handles all Turnkey query operations that retrieve data
  without modifying state, such as getting wallets, users, activities,
  and organization information.

  Query operations are simpler than activities and don't require
  request signing or complex authentication flows.
  """

  require Logger

  @type organization_id :: String.t()
  @type user_id :: String.t()
  @type wallet_id :: String.t()
  @type activity_id :: String.t()
  @type query_result :: {:ok, map()} | {:error, integer(), term()}

  # HTTP client module is injected via configuration
  defp http_client do
    Application.get_env(:be_votis_wallet, :http_client)
  end

  defp turnkey_config do
    Application.get_env(:be_votis_wallet, :turnkey, [])
  end

  @doc """
  Get organization information by organization ID.

  ## Parameters
  - `organization_id` - Organization UUID (optional, defaults to configured org)

  ## Returns
  - `{:ok, organization_data}` - Success with organization information
  - `{:error, status_code, error_message}` - Failure response
  """
  def get_organization(organization_id \\ nil) do
    org_id = organization_id || get_default_organization_id()
    url = build_query_url("/public/v1/query/get_organization")

    query_params = %{"organizationId" => org_id}
    execute_query(url, query_params)
  end

  @doc """
  Get user information by user ID.

  ## Parameters
  - `organization_id` - Organization UUID
  - `user_id` - User UUID

  ## Returns
  - `{:ok, user_data}` - Success with user information
  - `{:error, status_code, error_message}` - Failure response
  """
  def get_user(organization_id, user_id) do
    url = build_query_url("/public/v1/query/get_user")

    query_params = %{
      "organizationId" => organization_id,
      "userId" => user_id
    }

    execute_query(url, query_params)
  end

  @doc """
  List users in an organization.

  ## Parameters
  - `organization_id` - Organization UUID
  - `opts` - Query options (e.g., `:limit`, `:paginationToken`)

  ## Returns
  - `{:ok, %{"users" => users, "paginationToken" => token}}` - Success with user list
  - `{:error, status_code, error_message}` - Failure response
  """
  def list_users(organization_id, opts \\ []) do
    url = build_query_url("/public/v1/query/list_users")

    query_params =
      %{"organizationId" => organization_id}
      |> maybe_add_pagination_params(opts)

    execute_query(url, query_params)
  end

  @doc """
  Get wallet information by wallet ID.

  ## Parameters
  - `organization_id` - Organization UUID
  - `wallet_id` - Wallet UUID

  ## Returns
  - `{:ok, wallet_data}` - Success with wallet information including accounts
  - `{:error, status_code, error_message}` - Failure response
  """
  def get_wallet(organization_id, wallet_id) do
    url = build_query_url("/public/v1/query/get_wallet")

    query_params = %{
      "organizationId" => organization_id,
      "walletId" => wallet_id
    }

    execute_query(url, query_params)
  end

  @doc """
  List wallets in an organization.

  ## Parameters
  - `organization_id` - Organization UUID
  - `opts` - Query options (e.g., `:user_id`, `:limit`, `:paginationToken`)

  ## Returns
  - `{:ok, %{"wallets" => wallets, "paginationToken" => token}}` - Success with wallet list
  - `{:error, status_code, error_message}` - Failure response
  """
  def list_wallets(organization_id, opts \\ []) do
    url = build_query_url("/public/v1/query/list_wallets")

    query_params =
      %{"organizationId" => organization_id}
      |> maybe_add_user_filter(opts)
      |> maybe_add_pagination_params(opts)

    execute_query(url, query_params)
  end

  @doc """
  Get activity information by activity ID.

  ## Parameters
  - `organization_id` - Organization UUID
  - `activity_id` - Activity UUID

  ## Returns
  - `{:ok, activity_data}` - Success with activity details and status
  - `{:error, status_code, error_message}` - Failure response
  """
  def get_activity(organization_id, activity_id) do
    url = build_query_url("/public/v1/query/get_activity")

    query_params = %{
      "organizationId" => organization_id,
      "activityId" => activity_id
    }

    execute_query(url, query_params)
  end

  @doc """
  List activities for an organization.

  ## Parameters
  - `organization_id` - Organization UUID
  - `opts` - Query options (e.g., `:limit`, `:paginationToken`, `:activity_type`)

  ## Returns
  - `{:ok, %{"activities" => activities, "paginationToken" => token}}` - Success with activity list
  - `{:error, status_code, error_message}` - Failure response
  """
  def list_activities(organization_id, opts \\ []) do
    url = build_query_url("/public/v1/query/list_activities")

    query_params =
      %{"organizationId" => organization_id}
      |> maybe_add_activity_type_filter(opts)
      |> maybe_add_pagination_params(opts)

    execute_query(url, query_params)
  end

  @doc """
  Get wallet accounts for a specific wallet.

  ## Parameters
  - `organization_id` - Organization UUID
  - `wallet_id` - Wallet UUID

  ## Returns
  - `{:ok, %{"accounts" => accounts}}` - Success with account list
  - `{:error, status_code, error_message}` - Failure response
  """
  def get_wallet_accounts(organization_id, wallet_id) do
    url = build_query_url("/public/v1/query/get_wallet_accounts")

    query_params = %{
      "organizationId" => organization_id,
      "walletId" => wallet_id
    }

    execute_query(url, query_params)
  end

  # Private helper functions

  defp execute_query(url, query_params) do
    headers = build_query_headers()
    json_body = Jason.encode!(query_params)

    payload = http_client().build_payload(:post, url, headers, json_body)

    case http_client().request(payload) do
      {:ok, data} ->
        Logger.debug("Successfully executed Turnkey query",
          url: url,
          params: Map.keys(query_params)
        )

        {:ok, data}

      {:error, status_code, error_message} ->
        Logger.error("Failed to execute Turnkey query",
          url: url,
          status_code: status_code,
          error: inspect(error_message)
        )

        {:error, status_code, error_message}
    end
  end

  defp build_query_url(path) do
    base_url = get_config(:base_url)
    Path.join(base_url, path)
  end

  defp build_query_headers do
    api_key = get_config(:api_key)

    [
      {"Content-Type", "application/json"},
      {"X-Turnkey-API-Key", api_key}
    ]
  end

  defp maybe_add_pagination_params(params, opts) do
    params
    |> maybe_put_param("limit", Keyword.get(opts, :limit))
    |> maybe_put_param("paginationToken", Keyword.get(opts, :pagination_token))
  end

  defp maybe_add_user_filter(params, opts) do
    maybe_put_param(params, "userId", Keyword.get(opts, :user_id))
  end

  defp maybe_add_activity_type_filter(params, opts) do
    maybe_put_param(params, "activityType", Keyword.get(opts, :activity_type))
  end

  defp maybe_put_param(params, _key, nil), do: params
  defp maybe_put_param(params, key, value), do: Map.put(params, key, value)

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
