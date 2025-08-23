defmodule BeVotisWallet.BlockchainRegistry do
  @moduledoc """
  Registry for managing blockchain configurations used in multi-chain wallet operations.

  This module provides a centralized way to manage blockchain-specific parameters 
  required for wallet creation, including cryptographic curves, address formats, 
  and derivation paths following BIP-44 standards.

  ## Supported Chains

  * **Ethereum** - secp256k1 curve, Ethereum address format
  * **Bitcoin** - secp256k1 curve, P2WPKH (SegWit) address format  
  * **Solana** - ed25519 curve, Solana address format
  * **Tron** - secp256k1 curve, Tron address format

  ## Usage

      iex> BeVotisWallet.BlockchainRegistry.get(:eth)
      %BeVotisWallet.BlockchainRegistry.Chain{
        name: "Ethereum",
        symbol: "ETH", 
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/60'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 60
      }

      iex> BeVotisWallet.BlockchainRegistry.get("bitcoin")
      %BeVotisWallet.BlockchainRegistry.Chain{...}

      iex> BeVotisWallet.BlockchainRegistry.list()
      [:bitcoin, :ethereum, :solana, :tron]

  ## Adding Custom Chains

  You can extend the registry with custom chains via configuration:

      # config/config.exs
      config :be_votis_wallet, :custom_chains, %{
        polygon: %BeVotisWallet.BlockchainRegistry.Chain{
          name: "Polygon",
          symbol: "MATIC",
          curve: "CURVE_SECP256K1",
          address_format: "ADDRESS_FORMAT_ETHEREUM",
          path: "m/44'/966'/0'/0/0", 
          path_format: "PATH_FORMAT_BIP32",
          slip44_coin_type: 966
        }
      }

  ## References

  - [BIP-44: Multi-Account Hierarchy for Deterministic Wallets](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki)
  - [SLIP-0044: Registered coin types](https://github.com/satoshilabs/slips/blob/master/slip-0044.md)  
  - [Turnkey API Documentation](https://docs.turnkey.com)
  """

  alias __MODULE__.Chain

  defmodule Chain do
    @moduledoc """
    Represents a blockchain configuration with all necessary parameters
    for wallet creation and account management.
    """

    @type t :: %__MODULE__{
            name: String.t(),
            symbol: String.t(),
            curve: String.t(),
            address_format: String.t(),
            path: String.t(),
            path_format: String.t(),
            slip44_coin_type: non_neg_integer()
          }

    @derive Jason.Encoder
    defstruct [
      :name,
      :symbol,
      :curve,
      :address_format,
      :path,
      :path_format,
      :slip44_coin_type
    ]
  end

  # Standard path format used by Turnkey API
  @path_format "PATH_FORMAT_BIP32"

  # Built-in chain configurations - defined as function to avoid compile-time struct issues

  @type chain_identifier :: atom() | String.t()
  @type chain_result :: {:ok, __MODULE__.Chain.t()} | {:error, :not_found}

  @doc """
  Retrieves a blockchain configuration by identifier.

  Accepts various formats:
  - Atom: `:eth`, `:ethereum`, `:btc`, `:bitcoin` 
  - String: `"ETH"`, `"ethereum"`, `"Bitcoin"`

  ## Examples

      iex> get(:eth)
      {:ok, %Chain{name: "Ethereum", ...}}

      iex> get("ethereum")  
      {:ok, %Chain{name: "Ethereum", ...}}

      iex> get("ETH")
      {:ok, %Chain{name: "Ethereum", ...}}

      iex> get(:unknown)
      {:error, :not_found}
  """
  @spec get(chain_identifier()) :: chain_result()
  def get(identifier) when is_atom(identifier) do
    chains = all_chains()

    # Try direct atom lookup first
    case Map.get(chains, identifier) do
      nil ->
        # Try to find by symbol match (e.g., :eth -> :ethereum)
        find_by_symbol(chains, Atom.to_string(identifier))

      chain ->
        {:ok, chain}
    end
  end

  def get(identifier) when is_binary(identifier) do
    chains = all_chains()
    identifier_lower = String.downcase(identifier)

    # Try to find by name or symbol match
    found_chain =
      Enum.find_value(chains, fn {key, chain} ->
        cond do
          # Direct key match (e.g., "ethereum")
          Atom.to_string(key) == identifier_lower -> chain
          # Symbol match (e.g., "ETH") 
          String.downcase(chain.symbol) == identifier_lower -> chain
          # Name match (e.g., "Bitcoin")
          String.downcase(chain.name) == identifier_lower -> chain
          true -> nil
        end
      end)

    case found_chain do
      nil -> {:error, :not_found}
      chain -> {:ok, chain}
    end
  end

  @doc """
  Retrieves a blockchain configuration by identifier, raising if not found.

  ## Examples

      iex> get!(:eth)
      %Chain{name: "Ethereum", ...}

      iex> get!(:unknown)
      ** (ArgumentError) Unknown blockchain: unknown
  """
  @spec get!(chain_identifier()) :: __MODULE__.Chain.t()
  def get!(identifier) do
    case get(identifier) do
      {:ok, chain} -> chain
      {:error, :not_found} -> raise ArgumentError, "Unknown blockchain: #{inspect(identifier)}"
    end
  end

  @doc """
  Returns a list of all supported blockchain identifiers.

  ## Examples

      iex> list()
      [:bitcoin, :ethereum, :solana, :tron]
  """
  @spec list() :: [atom()]
  def list do
    all_chains()
    |> Map.keys()
    |> Enum.sort()
  end

  @doc """
  Registers a new blockchain configuration at runtime.

  This allows extending the registry with additional chains
  beyond the built-in ones.

  ## Examples

      iex> chain = %Chain{
      ...>   name: "Polygon",
      ...>   symbol: "MATIC", 
      ...>   curve: "CURVE_SECP256K1",
      ...>   address_format: "ADDRESS_FORMAT_ETHEREUM",
      ...>   path: "m/44'/966'/0'/0/0",
      ...>   path_format: "PATH_FORMAT_BIP32",
      ...>   slip44_coin_type: 966
      ...> }
      iex> register(:polygon, chain)
      :ok

      iex> get(:polygon)
      {:ok, %Chain{name: "Polygon", ...}}
  """
  @spec register(atom(), __MODULE__.Chain.t()) :: :ok
  def register(key, %__MODULE__.Chain{} = chain) when is_atom(key) do
    current_chains = get_custom_chains()
    updated_chains = Map.put(current_chains, key, chain)
    Application.put_env(:be_votis_wallet, :custom_chains, updated_chains)
    :ok
  end

  @doc """
  Returns the standard path format used by all chains.

  ## Examples

      iex> path_format()
      "PATH_FORMAT_BIP32"
  """
  @spec path_format() :: String.t()
  def path_format, do: @path_format

  # Private functions

  defp all_chains do
    Map.merge(get_custom_chains(), built_in_chains())
  end

  defp built_in_chains do
    %{
      ethereum: %Chain{
        name: "Ethereum",
        symbol: "ETH",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/60'/0'/0/0",
        path_format: @path_format,
        slip44_coin_type: 60
      },
      bitcoin: %Chain{
        name: "Bitcoin",
        symbol: "BTC",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH",
        path: "m/44'/0'/0'/0/0",
        path_format: @path_format,
        slip44_coin_type: 0
      },
      solana: %Chain{
        name: "Solana",
        symbol: "SOL",
        curve: "CURVE_ED25519",
        address_format: "ADDRESS_FORMAT_SOLANA",
        path: "m/44'/501'/0'/0'",
        path_format: @path_format,
        slip44_coin_type: 501
      },
      tron: %Chain{
        name: "Tron",
        symbol: "TRX",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_TRON",
        path: "m/44'/195'/0'/0/0",
        path_format: @path_format,
        slip44_coin_type: 195
      }
    }
  end

  defp get_custom_chains do
    Application.get_env(:be_votis_wallet, :custom_chains, %{})
  end

  defp find_by_symbol(chains, symbol_str) do
    symbol_lower = String.downcase(symbol_str)

    found_chain =
      Enum.find_value(chains, fn {_key, chain} ->
        if String.downcase(chain.symbol) == symbol_lower, do: chain, else: nil
      end)

    case found_chain do
      nil -> {:error, :not_found}
      chain -> {:ok, chain}
    end
  end
end
