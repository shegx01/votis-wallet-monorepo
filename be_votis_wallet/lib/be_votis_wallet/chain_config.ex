defmodule BeVotisWallet.ChainConfig do
  @moduledoc """
  Centralized configuration for blockchain networks with support for EVM-compatible chains.
  
  This module provides comprehensive chain configurations including chain IDs, SLIP-44 coin types,
  derivation paths, and automatic fallback to Ethereum configuration for EVM-compatible chains
  without registered SLIP-44 coin types.

  ## Features

  * Support for major blockchain networks (Bitcoin, Ethereum, Solana, Tron)
  * EVM-compatible chain configurations with proper SLIP-44 coin types
  * Automatic fallback to Ethereum derivation path for unregistered EVM chains
  * Chain lookup by chain_id, symbol, or name
  * Integration with Turnkey API address formats and curves
  * Symbol aliases support (e.g., POL/MATIC for Polygon)

  ## Supported Chains with Custom Derivation Paths

  ### Non-EVM Chains:
  These chains have registered SLIP-44 coin types and use their own unique derivation paths:
  
  * **Bitcoin (BTC)** - SLIP-44: 0, Path: `m/44'/0'/0'/0/0`
  * **Solana (SOL)** - SLIP-44: 501, Path: `m/44'/501'/0'/0'`
  * **Tron (TRX)** - SLIP-44: 195, Path: `m/44'/195'/0'/0/0`

  ## EVM-Compatible Chains (All Use Ethereum Derivation)

  **IMPORTANT:** All EVM-compatible chains use the same derivation path for user fund accessibility.
  This ensures users can access their funds from any EVM chain configuration without risk of
  losing access due to derivation path differences.

  ### All EVM Chains Use:
  * **SLIP-44 coin type:** 60 (Ethereum's)
  * **Derivation path:** `m/44'/60'/0'/0/0` 
  * **Curve:** `CURVE_SECP256K1`
  * **Address format:** `ADDRESS_FORMAT_ETHEREUM`
  * **Same address across all EVM chains:** ✅

  ### Configured EVM Chains:
  * **Ethereum (ETH)** - Chain ID: 1
  * **Polygon (POL/MATIC)** - Chain ID: 137
  * **Arbitrum One (ARB)** - Chain ID: 42161
  * **Optimism (OP)** - Chain ID: 10
  * **BNB Smart Chain (BNB)** - Chain ID: 56
  * **Avalanche C-Chain (AVAX)** - Chain ID: 43114
  * **Base (BASE)** - Chain ID: 8453

  ### Fallback EVM Chains:
  Any EVM-compatible chain not explicitly configured above will automatically use the same
  Ethereum derivation path. Examples:
  
  * **Linea** (Chain ID: 59144)
  * **Mantle** (Chain ID: 5000)
  * **Scroll** (Chain ID: 534352)
  * **zkSync Era** (Chain ID: 324)
  * Any new or custom EVM chain

  ## Symbol Aliases

  Some chains support multiple symbols for backward compatibility:
  * **Polygon**: Primary symbol "POL", alias "MATIC"

  ## Usage Examples

      # Get chain by chain_id (all EVM chains use same derivation path)
      iex> ChainConfig.get_by_chain_id(137)
      {:ok, %ChainConfig.Chain{name: "Polygon", path: "m/44'/60'/0'/0/0", ...}}
      
      iex> ChainConfig.get_by_chain_id(42161) 
      {:ok, %ChainConfig.Chain{name: "Arbitrum One", path: "m/44'/60'/0'/0/0", ...}}

      # Get chain by symbol or alias
      iex> ChainConfig.get("POL")   # Primary symbol
      {:ok, %ChainConfig.Chain{name: "Polygon", symbol: "POL", ...}}
      
      iex> ChainConfig.get("MATIC") # Alias - same chain, same address
      {:ok, %ChainConfig.Chain{name: "Polygon", symbol: "POL", ...}}

      # All EVM chains (known and unknown) use Ethereum derivation path
      iex> ChainConfig.get_by_chain_id(999999)
      {:ok, %ChainConfig.Chain{name: "EVM Chain 999999", path: "m/44'/60'/0'/0/0", ...}}

      # List only EVM-compatible chains
      iex> ChainConfig.list_evm_chains()
      [:arbitrum, :avalanche, :base, :bnb_smart_chain, :ethereum, :optimism, :polygon]

  ## HD Wallet Implications

  ### Custom Derivation Paths:
  * **Security**: Each chain has isolated key derivation
  * **Standards Compliance**: Follows BIP-44/SLIP-44 specifications
  * **Audit Trail**: Clear separation between different blockchain networks

  ### Fallback Derivation Path:
  * **Compatibility**: Works with any EVM-compatible chain immediately
  * **Address Consistency**: Same address across all fallback EVM chains
  * **Simplified Management**: Reduces complexity for new EVM chains

  ## References

  - [SLIP-0044: Registered coin types](https://github.com/satoshilabs/slips/blob/master/slip-0044.md)
  - [EVM Chain IDs](https://chainlist.org/)
  - [BIP-44: Multi-Account Hierarchy](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki)
  - [Polygon POL Migration](https://polygon.technology/blog/save-the-date-unpacking-the-pol-migration)
  """

  alias __MODULE__.Chain

  defmodule Chain do
    @moduledoc """
    Represents a blockchain configuration with all parameters needed for wallet operations.
    """

    @type t :: %__MODULE__{
            chain_id: non_neg_integer() | nil,
            name: String.t(),
            symbol: String.t(),
            symbol_aliases: [String.t()],
            curve: String.t(),
            address_format: String.t(),
            path: String.t(),
            path_format: String.t(),
            slip44_coin_type: non_neg_integer(),
            is_evm_compatible: boolean()
          }

    @derive Jason.Encoder
    defstruct [
      :chain_id,
      :name,
      :symbol,
      :symbol_aliases,
      :curve,
      :address_format,
      :path,
      :path_format,
      :slip44_coin_type,
      :is_evm_compatible
    ]
  end

  # Standard path format used by Turnkey API
  @path_format "PATH_FORMAT_BIP32"

  # EVM curve and address format
  @evm_curve "CURVE_SECP256K1"
  @evm_address_format "ADDRESS_FORMAT_ETHEREUM"

  @type chain_identifier :: atom() | String.t() | non_neg_integer()
  @type chain_result :: {:ok, Chain.t()} | {:error, :not_found}

  @doc """
  Retrieves a blockchain configuration by various identifiers.

  Supports lookup by:
  - Chain ID (integer): `1` for Ethereum, `137` for Polygon
  - Symbol (string/atom): `"ETH"`, `:eth`, `"MATIC"`  
  - Name (string): `"Ethereum"`, `"Polygon"`

  ## Examples

      iex> get(1)
      {:ok, %Chain{name: "Ethereum", chain_id: 1, ...}}

      iex> get("MATIC") 
      {:ok, %Chain{name: "Polygon", symbol: "MATIC", ...}}

      iex> get(:bitcoin)
      {:ok, %Chain{name: "Bitcoin", ...}}
  """
  @spec get(chain_identifier()) :: chain_result()
  def get(chain_id) when is_integer(chain_id) do
    get_by_chain_id(chain_id)
  end

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

    # Try to find by name, symbol, or symbol alias match
    found_chain =
      Enum.find_value(chains, fn {key, chain} ->
        cond do
          # Direct key match (e.g., "ethereum")
          Atom.to_string(key) == identifier_lower -> chain
          # Symbol match (e.g., "ETH") 
          String.downcase(chain.symbol) == identifier_lower -> chain
          # Symbol alias match (e.g., "MATIC" for Polygon)
          Enum.any?(chain.symbol_aliases, &(String.downcase(&1) == identifier_lower)) -> chain
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
  Retrieves a blockchain configuration by chain ID.

  For unknown EVM chain IDs, returns a fallback configuration using Ethereum's
  derivation path and EVM-compatible settings.

  ## Examples

      iex> get_by_chain_id(1)
      {:ok, %Chain{name: "Ethereum", chain_id: 1, ...}}

      iex> get_by_chain_id(137)
      {:ok, %Chain{name: "Polygon", chain_id: 137, ...}}

      # Unknown EVM chain - falls back to Ethereum config
      iex> get_by_chain_id(999999)
      {:ok, %Chain{name: "EVM Chain 999999", path: "m/44'/60'/0'/0/0", ...}}
  """
  @spec get_by_chain_id(non_neg_integer()) :: {:ok, Chain.t()}
  def get_by_chain_id(chain_id) when is_integer(chain_id) do
    case Map.get(chain_id_mapping(), chain_id) do
      nil ->
        # Unknown chain ID - assume EVM-compatible and fallback to Ethereum config
        {:ok, create_fallback_evm_chain(chain_id)}

      chain_key ->
        case Map.get(all_chains(), chain_key) do
          nil -> {:ok, create_fallback_evm_chain(chain_id)}
          chain -> {:ok, chain}
        end
    end
  end

  @doc """
  Retrieves a blockchain configuration, raising if not found.

  ## Examples

      iex> get!(1)
      %Chain{name: "Ethereum", ...}

      iex> get!(:unknown)
      ** (ArgumentError) Unknown blockchain: :unknown
  """
  @spec get!(chain_identifier()) :: Chain.t()
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
      [:arbitrum, :avalanche, :base, :bitcoin, :bnb_smart_chain, :ethereum, :optimism, :polygon, :solana, :tron]
  """
  @spec list() :: [atom()]
  def list do
    all_chains()
    |> Map.keys()
    |> Enum.sort()
  end

  @doc """
  Returns a list of all supported EVM-compatible chains.

  ## Examples

      iex> list_evm_chains()
      [:arbitrum, :avalanche, :base, :bnb_smart_chain, :ethereum, :optimism, :polygon]
  """
  @spec list_evm_chains() :: [atom()]
  def list_evm_chains do
    all_chains()
    |> Enum.filter(fn {_key, chain} -> chain.is_evm_compatible end)
    |> Enum.map(fn {key, _chain} -> key end)
    |> Enum.sort()
  end

  @doc """
  Checks if a chain ID represents an EVM-compatible chain.

  ## Examples

      iex> is_evm_compatible?(1)
      true

      iex> get_by_chain_id(999999) |> elem(1) |> Map.get(:is_evm_compatible)
      true

      iex> is_evm_compatible?(0) # Bitcoin
      false
  """
  @spec is_evm_compatible?(non_neg_integer()) :: boolean()
  def is_evm_compatible?(chain_id) when is_integer(chain_id) do
    case get_by_chain_id(chain_id) do
      {:ok, chain} -> chain.is_evm_compatible
    end
  end

  @doc """
  Returns a list of all chains that have custom derivation paths (non-fallback).

  ## Examples

      iex> list_chains_with_custom_paths()
      [:arbitrum, :avalanche, :base, :bitcoin, :bnb_smart_chain, :ethereum, :optimism, :polygon, :solana, :tron]
  """
  @spec list_chains_with_custom_paths() :: [atom()]
  def list_chains_with_custom_paths do
    # All explicitly configured chains have custom paths
    list()
  end

  @doc """
  Returns information about which derivation path a chain uses.

  ## Examples

      iex> get_derivation_info(1)
      {:custom, "m/44'/60'/0'/0/0", "Ethereum has registered SLIP-44 coin type 60"}

      iex> get_derivation_info(999999)
      {:fallback, "m/44'/60'/0'/0/0", "Uses Ethereum fallback derivation"}
  """
  @spec get_derivation_info(non_neg_integer()) :: 
    {:custom | :fallback, String.t(), String.t()}
  def get_derivation_info(chain_id) when is_integer(chain_id) do
    case Map.get(chain_id_mapping(), chain_id) do
      nil ->
        {:fallback, "m/44'/60'/0'/0/0", "Uses Ethereum fallback derivation"}
        
      chain_key ->
        {:ok, chain} = get(chain_key)
        reason = case chain.is_evm_compatible do
          true -> "EVM chain with registered SLIP-44 coin type #{chain.slip44_coin_type}"
          false -> "Non-EVM chain with registered SLIP-44 coin type #{chain.slip44_coin_type}"
        end
        {:custom, chain.path, reason}
    end
  end

  @doc """
  Checks if a chain ID uses a custom derivation path or fallback.

  ## Examples

      iex> has_custom_derivation_path?(137)
      true

      iex> has_custom_derivation_path?(999999)
      false
  """
  @spec has_custom_derivation_path?(non_neg_integer()) :: boolean()
  def has_custom_derivation_path?(chain_id) when is_integer(chain_id) do
    case get_derivation_info(chain_id) do
      {:custom, _path, _reason} -> true
      {:fallback, _path, _reason} -> false
    end
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
    built_in_chains()
  end

  defp built_in_chains do
    %{
      # Non-EVM chains
      bitcoin: %Chain{
        chain_id: nil,
        name: "Bitcoin",
        symbol: "BTC",
        symbol_aliases: [],
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH",
        path: "m/44'/0'/0'/0/0",
        path_format: @path_format,
        slip44_coin_type: 0,
        is_evm_compatible: false
      },
      solana: %Chain{
        chain_id: nil,
        name: "Solana",
        symbol: "SOL",
        symbol_aliases: [],
        curve: "CURVE_ED25519",
        address_format: "ADDRESS_FORMAT_SOLANA",
        path: "m/44'/501'/0'/0'",
        path_format: @path_format,
        slip44_coin_type: 501,
        is_evm_compatible: false
      },
      tron: %Chain{
        chain_id: nil,
        name: "Tron",
        symbol: "TRX",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: "ADDRESS_FORMAT_TRON",
        path: "m/44'/195'/0'/0/0",
        path_format: @path_format,
        slip44_coin_type: 195,
        is_evm_compatible: false
      },

      # EVM-compatible chains - ALL USE ETHEREUM DERIVATION PATH FOR FUND ACCESSIBILITY
      ethereum: %Chain{
        chain_id: 1,
        name: "Ethereum",
        symbol: "ETH",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",
        path_format: @path_format,
        slip44_coin_type: 60,
        is_evm_compatible: true
      },
      polygon: %Chain{
        chain_id: 137,
        name: "Polygon",
        symbol: "POL",
        symbol_aliases: ["MATIC"],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",  # Same as Ethereum for fund accessibility
        path_format: @path_format,
        slip44_coin_type: 60,        # Same as Ethereum for fund accessibility
        is_evm_compatible: true
      },
      arbitrum: %Chain{
        chain_id: 42161,
        name: "Arbitrum One",
        symbol: "ARB",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",  # Same as Ethereum for fund accessibility
        path_format: @path_format,
        slip44_coin_type: 60,        # Same as Ethereum for fund accessibility
        is_evm_compatible: true
      },
      optimism: %Chain{
        chain_id: 10,
        name: "Optimism",
        symbol: "OP",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",  # Same as Ethereum for fund accessibility
        path_format: @path_format,
        slip44_coin_type: 60,        # Same as Ethereum for fund accessibility
        is_evm_compatible: true
      },
      bnb_smart_chain: %Chain{
        chain_id: 56,
        name: "BNB Smart Chain",
        symbol: "BNB",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",  # Same as Ethereum for fund accessibility
        path_format: @path_format,
        slip44_coin_type: 60,        # Same as Ethereum for fund accessibility
        is_evm_compatible: true
      },
      avalanche: %Chain{
        chain_id: 43114,
        name: "Avalanche C-Chain",
        symbol: "AVAX",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",  # Same as Ethereum for fund accessibility
        path_format: @path_format,
        slip44_coin_type: 60,        # Same as Ethereum for fund accessibility
        is_evm_compatible: true
      },
      base: %Chain{
        chain_id: 8453,
        name: "Base",
        symbol: "BASE",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",  # Same as Ethereum for fund accessibility
        path_format: @path_format,
        slip44_coin_type: 60,        # Same as Ethereum for fund accessibility
        is_evm_compatible: true
      }
    }
  end

  # Mapping of chain IDs to chain keys
  defp chain_id_mapping do
    %{
      1 => :ethereum,
      137 => :polygon,
      42161 => :arbitrum,
      10 => :optimism,
      56 => :bnb_smart_chain,
      43114 => :avalanche,
      8453 => :base
    }
  end

  defp create_fallback_evm_chain(chain_id) do
    %Chain{
      chain_id: chain_id,
      name: "EVM Chain #{chain_id}",
      symbol: "ETH",
      symbol_aliases: [],
      curve: @evm_curve,
      address_format: @evm_address_format,
      path: "m/44'/60'/0'/0/0", # Use Ethereum's derivation path
      path_format: @path_format,
      slip44_coin_type: 60, # Use Ethereum's SLIP-44 coin type
      is_evm_compatible: true
    }
  end

  defp find_by_symbol(chains, symbol_str) do
    symbol_lower = String.downcase(symbol_str)

    found_chain =
      Enum.find_value(chains, fn {_key, chain} ->
        cond do
          # Primary symbol match
          String.downcase(chain.symbol) == symbol_lower -> chain
          # Symbol alias match
          Enum.any?(chain.symbol_aliases, &(String.downcase(&1) == symbol_lower)) -> chain
          true -> nil
        end
      end)

    case found_chain do
      nil -> {:error, :not_found}
      chain -> {:ok, chain}
    end
  end
end
