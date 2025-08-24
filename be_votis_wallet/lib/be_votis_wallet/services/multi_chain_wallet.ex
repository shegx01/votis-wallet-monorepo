defmodule BeVotisWallet.Services.MultiChainWallet do
  @moduledoc """
  Multi-chain wallet service for creating and managing wallets across different blockchain networks.

  This service provides a high-level interface for wallet operations that abstracts away
  blockchain-specific details like cryptographic curves and address formats. It builds on
  top of the Turnkey Activities service and uses the ChainConfig to manage supported chains.

  ## Features

  * Create wallets for specific blockchain networks
  * Create multi-chain wallets supporting multiple networks at once
  * Add accounts to existing wallets for new blockchain networks
  * Automatic curve and address format management based on blockchain type
  * Support for Bitcoin, Ethereum, Solana, Tron and custom chains

  ## Usage

      # Create an Ethereum-only wallet
      {:ok, response} = MultiChainWallet.create_wallet_for_chain(
        "org_123",
        "user_456",
        "My ETH Wallet",
        :ethereum
      )

      # Create a multi-chain wallet supporting multiple networks
      {:ok, response} = MultiChainWallet.create_multi_chain_wallet(
        "org_123",
        "user_456",
        "Universal Wallet",
        [:ethereum, :bitcoin, :solana]
      )

      # Add Tron support to an existing wallet
      {:ok, response} = MultiChainWallet.add_account(
        "org_123",
        "wallet_789",
        :tron
      )

  ## Error Handling

  This service propagates errors from the underlying Activities service and adds
  its own error handling for unsupported blockchain networks:

      # Unsupported chain
      {:error, :unsupported_chain} = MultiChainWallet.create_wallet_for_chain(
        "org_123",
        "user_456",
        "Invalid Wallet",
        :unknown_chain
      )

      # Activities service error (network issues, API errors, etc.)
      {:error, 500, "Internal server error"} = MultiChainWallet.create_wallet_for_chain(...)

  """

  require Logger

  alias BeVotisWallet.ChainConfig
  alias BeVotisWallet.ChainConfig.Chain
  alias BeVotisWallet.Services.Turnkey.Activities

  @type organization_id :: String.t()
  @type user_id :: String.t()
  @type wallet_id :: String.t()
  @type wallet_name :: String.t()
  @type chain_identifier :: ChainConfig.chain_identifier()
  @type activity_result :: Activities.activity_result()

  @doc """
  Creates a wallet for a specific blockchain network.

  This is a convenience function for creating single-chain wallets.
  For multi-chain support, use `create_multi_chain_wallet/4`.

  ## Parameters

  * `organization_id` - Turnkey organization identifier
  * `user_id` - User identifier who will own the wallet
  * `wallet_name` - Human-readable name for the wallet
  * `chain` - Blockchain identifier (atom or string)

  ## Returns

  * `{:ok, response}` - Success with wallet creation response from Turnkey
  * `{:error, :unsupported_chain}` - Unknown blockchain identifier
  * `{:error, status_code, error_message}` - Activities service error

  ## Examples

      iex> create_wallet_for_chain("org_123", "user_456", "My BTC Wallet", :bitcoin)
      {:ok, %{
        "activity" => %{
          "id" => "activity_789",
          "result" => %{"createWalletResult" => %{"walletId" => "wallet_abc"}}
        }
      }}

      iex> create_wallet_for_chain("org_123", "user_456", "ETH Wallet", "ethereum")
      {:ok, %{...}}

      iex> create_wallet_for_chain("org_123", "user_456", "Invalid", :unknown)
      {:error, :unsupported_chain}
  """
  @spec create_wallet_for_chain(organization_id(), user_id(), wallet_name(), chain_identifier()) ::
          activity_result() | {:error, :unsupported_chain}
  def create_wallet_for_chain(organization_id, user_id, wallet_name, chain) do
    Logger.info("Creating single-chain wallet",
      organization_id: organization_id,
      user_id: user_id,
      wallet_name: wallet_name,
      chain: chain
    )

    case ChainConfig.get(chain) do
      {:ok, chain_config} ->
        account_spec = build_account_spec(chain_config)

        result =
          Activities.create_wallet(organization_id, user_id, wallet_name, [account_spec])

        case result do
          {:ok, response} ->
            Logger.info("Successfully created single-chain wallet",
              organization_id: organization_id,
              wallet_name: wallet_name,
              chain: chain,
              activity_id: get_in(response, ["activity", "id"])
            )

            {:ok, response}

          {:error, status_code, error_message} ->
            Logger.error("Failed to create single-chain wallet",
              organization_id: organization_id,
              wallet_name: wallet_name,
              chain: chain,
              status_code: status_code,
              error: inspect(error_message)
            )

            {:error, status_code, error_message}
        end

      {:error, :not_found} ->
        Logger.error("Unsupported blockchain for wallet creation",
          chain: chain,
          organization_id: organization_id
        )

        {:error, :unsupported_chain}
    end
  end

  @doc """
  Creates a multi-chain wallet supporting multiple blockchain networks.

  This function creates a single wallet with accounts for all specified chains.
  Each chain will have its own account with the appropriate derivation path and address format.

  ## Parameters

  * `organization_id` - Turnkey organization identifier
  * `user_id` - User identifier who will own the wallet
  * `wallet_name` - Human-readable name for the wallet
  * `chains` - List of blockchain identifiers to support

  ## Returns

  * `{:ok, response}` - Success with wallet creation response from Turnkey
  * `{:error, {:unsupported_chain, chain}}` - One or more unknown blockchain identifiers
  * `{:error, status_code, error_message}` - Activities service error

  ## Examples

      iex> create_multi_chain_wallet("org_123", "user_456", "Universal", [:ethereum, :bitcoin])
      {:ok, %{
        "activity" => %{
          "result" => %{
            "createWalletResult" => %{
              "walletId" => "wallet_def",
              "addresses" => [
                %{"address" => "0x...", "format" => "ADDRESS_FORMAT_ETHEREUM"},
                %{"address" => "bc1...", "format" => "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH"}
              ]
            }
          }
        }
      }}

      iex> create_multi_chain_wallet("org_123", "user_456", "All Chains", [:eth, :btc, :sol, :tron])
      {:ok, %{...}}

      iex> create_multi_chain_wallet("org_123", "user_456", "Invalid", [:bitcoin, :unknown])
      {:error, {:unsupported_chain, :unknown}}
  """
  @spec create_multi_chain_wallet(
          organization_id(),
          user_id(),
          wallet_name(),
          [chain_identifier()]
        ) ::
          activity_result() | {:error, {:unsupported_chain, chain_identifier()}}
  def create_multi_chain_wallet(organization_id, user_id, wallet_name, chains) do
    Logger.info("Creating multi-chain wallet",
      organization_id: organization_id,
      user_id: user_id,
      wallet_name: wallet_name,
      chains: chains,
      chain_count: length(chains)
    )

    case build_account_specs_for_chains(chains) do
      {:ok, account_specs} ->
        result =
          Activities.create_wallet(organization_id, user_id, wallet_name, account_specs)

        case result do
          {:ok, response} ->
            Logger.info("Successfully created multi-chain wallet",
              organization_id: organization_id,
              wallet_name: wallet_name,
              chains: chains,
              account_count: length(account_specs),
              activity_id: get_in(response, ["activity", "id"])
            )

            {:ok, response}

          {:error, status_code, error_message} ->
            Logger.error("Failed to create multi-chain wallet",
              organization_id: organization_id,
              wallet_name: wallet_name,
              chains: chains,
              status_code: status_code,
              error: inspect(error_message)
            )

            {:error, status_code, error_message}
        end

      {:error, {:unsupported_chain, chain}} ->
        Logger.error("Unsupported blockchain in multi-chain wallet creation",
          chain: chain,
          chains: chains,
          organization_id: organization_id
        )

        {:error, {:unsupported_chain, chain}}
    end
  end

  @doc """
  Adds an account for a new blockchain to an existing wallet.

  This allows extending an existing wallet with support for additional blockchain networks
  without creating a new wallet.

  ## Parameters

  * `organization_id` - Turnkey organization identifier
  * `wallet_id` - Existing wallet identifier
  * `chain` - Blockchain identifier to add support for

  ## Returns

  * `{:ok, response}` - Success with account creation response from Turnkey
  * `{:error, :unsupported_chain}` - Unknown blockchain identifier
  * `{:error, status_code, error_message}` - Activities service error

  ## Examples

      iex> add_account("org_123", "wallet_456", :solana)
      {:ok, %{
        "activity" => %{
          "result" => %{
            "createWalletAccountsResult" => %{
              "addresses" => [
                %{"address" => "So11...", "format" => "ADDRESS_FORMAT_SOLANA"}
              ]
            }
          }
        }
      }}

      iex> add_account("org_123", "wallet_456", "TRON")
      {:ok, %{...}}

      iex> add_account("org_123", "wallet_456", :unknown)
      {:error, :unsupported_chain}
  """
  @spec add_account(organization_id(), wallet_id(), chain_identifier()) ::
          activity_result() | {:error, :unsupported_chain}
  def add_account(organization_id, wallet_id, chain) do
    Logger.info("Adding account to existing wallet",
      organization_id: organization_id,
      wallet_id: wallet_id,
      chain: chain
    )

    case ChainConfig.get(chain) do
      {:ok, chain_config} ->
        account_spec = build_account_spec(chain_config)

        result = Activities.create_wallet_account(organization_id, wallet_id, account_spec)

        case result do
          {:ok, response} ->
            Logger.info("Successfully added account to wallet",
              organization_id: organization_id,
              wallet_id: wallet_id,
              chain: chain,
              activity_id: get_in(response, ["activity", "id"])
            )

            {:ok, response}

          {:error, status_code, error_message} ->
            Logger.error("Failed to add account to wallet",
              organization_id: organization_id,
              wallet_id: wallet_id,
              chain: chain,
              status_code: status_code,
              error: inspect(error_message)
            )

            {:error, status_code, error_message}
        end

      {:error, :not_found} ->
        Logger.error("Unsupported blockchain for account creation",
          chain: chain,
          wallet_id: wallet_id,
          organization_id: organization_id
        )

        {:error, :unsupported_chain}
    end
  end

  # Private helper functions

  @spec build_account_spec(Chain.t()) :: map()
  defp build_account_spec(%Chain{} = chain) do
    %{
      "curve" => chain.curve,
      "pathFormat" => chain.path_format,
      "path" => chain.path,
      "addressFormat" => chain.address_format
    }
  end

  @spec build_account_specs_for_chains([chain_identifier()]) ::
          {:ok, [map()]} | {:error, {:unsupported_chain, chain_identifier()}}
  defp build_account_specs_for_chains(chains) do
    chains
    |> Enum.reduce_while({:ok, []}, fn chain, {:ok, acc} ->
      case ChainConfig.get(chain) do
        {:ok, chain_config} ->
          account_spec = build_account_spec(chain_config)
          {:cont, {:ok, [account_spec | acc]}}

        {:error, :not_found} ->
          {:halt, {:error, {:unsupported_chain, chain}}}
      end
    end)
    |> case do
      {:ok, account_specs} -> {:ok, Enum.reverse(account_specs)}
      error -> error
    end
  end
end
