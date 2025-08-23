defmodule BeVotisWallet.ChainPathAnalyzer do
  @moduledoc """
  Analyzes potential issues when users interact with different EVM chains
  that have different derivation paths, leading to different addresses.
  
  This module helps identify when users might lose access to their funds
  by switching between chains with different derivation paths.
  """

  alias BeVotisWallet.ChainConfig

  @doc """
  Analyzes what happens when a user tries to access funds sent to one chain
  using a different chain's configuration.
  
  ## Examples
  
      # User sent funds to Arbitrum but tries to access with Base config
      iex> analyze_cross_chain_access(42161, 8453)
      {:incompatible, 
       %{
         source: %{chain: "Arbitrum One", path: "m/44'/9001'/0'/0/0"},
         target: %{chain: "Base", path: "m/44'/8453'/0'/0/0"},
         issue: "Different derivation paths will generate different addresses"
       }}
  """
  @spec analyze_cross_chain_access(non_neg_integer(), non_neg_integer()) :: 
    {:compatible, map()} | {:incompatible, map()}
  def analyze_cross_chain_access(source_chain_id, target_chain_id) do
    {:ok, source_chain} = ChainConfig.get_by_chain_id(source_chain_id)
    {:ok, target_chain} = ChainConfig.get_by_chain_id(target_chain_id)
    
    if source_chain.path == target_chain.path do
      {:compatible, %{
        source: %{chain: source_chain.name, path: source_chain.path},
        target: %{chain: target_chain.name, path: target_chain.path},
        message: "Same derivation path - funds will be accessible"
      }}
    else
      {:incompatible, %{
        source: %{chain: source_chain.name, path: source_chain.path},
        target: %{chain: target_chain.name, path: target_chain.path},
        issue: "Different derivation paths will generate different addresses",
        consequence: "Funds sent to #{source_chain.name} cannot be accessed through #{target_chain.name} configuration"
      }}
    end
  end

  @doc """
  Lists all EVM chains and their derivation path compatibility.
  
  Groups chains by their derivation paths to show which chains
  share the same addresses.
  """
  @spec get_derivation_path_groups() :: map()
  def get_derivation_path_groups do
    ChainConfig.list_evm_chains()
    |> Enum.map(fn chain_key ->
      {:ok, chain} = ChainConfig.get(chain_key)
      {chain_key, chain}
    end)
    |> Enum.group_by(fn {_key, chain} -> chain.path end, fn {key, chain} -> 
      {key, chain.name, chain.chain_id} 
    end)
  end

  @doc """
  Generates a compatibility matrix for EVM chains.
  
  Shows which chains can access funds sent to other chains.
  """
  @spec generate_compatibility_matrix() :: map()
  def generate_compatibility_matrix do
    evm_chains = ChainConfig.list_evm_chains()
    
    for source_chain <- evm_chains, target_chain <- evm_chains, into: %{} do
      {:ok, source} = ChainConfig.get(source_chain)
      {:ok, target} = ChainConfig.get(target_chain)
      
      key = {source_chain, target_chain}
      compatible = source.path == target.path
      
      {key, %{
        compatible: compatible,
        source_path: source.path,
        target_path: target.path,
        same_address: compatible
      }}
    end
  end

  @doc """
  Simulates what addresses would be generated for a given seed phrase
  across different EVM chains.
  
  Note: This is for demonstration only - never use real seed phrases!
  """
  @spec simulate_address_generation(String.t()) :: map()
  def simulate_address_generation(mock_seed_phrase) do
    ChainConfig.list_evm_chains()
    |> Enum.map(fn chain_key ->
      {:ok, chain} = ChainConfig.get(chain_key)
      
      # Simulate address generation (not real cryptography)
      simulated_address = simulate_derive_address(mock_seed_phrase, chain.path)
      
      {chain_key, %{
        chain_name: chain.name,
        chain_id: chain.chain_id,
        derivation_path: chain.path,
        simulated_address: simulated_address
      }}
    end)
    |> Enum.into(%{})
  end

  @doc """
  Finds all chains that would generate the same address for a given seed phrase.
  
  This helps identify which chains share addresses due to same derivation paths.
  """
  @spec find_shared_addresses(String.t()) :: map()
  def find_shared_addresses(mock_seed_phrase) do
    simulate_address_generation(mock_seed_phrase)
    |> Enum.group_by(fn {_chain, info} -> info.simulated_address end, fn {chain, info} ->
      {chain, info.chain_name, info.chain_id}
    end)
    |> Enum.filter(fn {_address, chains} -> length(chains) > 1 end)
    |> Enum.into(%{})
  end

  @doc """
  Provides recommendations for handling cross-chain derivation path issues.
  """
  @spec get_recommendations() :: [String.t()]
  def get_recommendations do
    [
      "1. Always verify the derivation path before sending funds to ensure you can access them later",
      "2. Consider using the same derivation path for all EVM chains if address consistency is more important than security isolation", 
      "3. Implement address lookup across all derivation paths when users report missing funds",
      "4. Provide clear warnings when users switch between chains with different derivation paths",
      "5. Consider storing metadata about which derivation path was used for each transaction",
      "6. For production wallets, implement derivation path migration tools if needed"
    ]
  end

  # Private helper functions

  defp simulate_derive_address(seed_phrase, derivation_path) do
    # This is a mock simulation - NOT real cryptography!
    # In real implementation, this would use proper BIP-44 derivation
    hash = :crypto.hash(:sha256, seed_phrase <> derivation_path)
    hash_hex = Base.encode16(hash, case: :lower)
    "0x" <> String.slice(hash_hex, 0, 40)
  end
end
