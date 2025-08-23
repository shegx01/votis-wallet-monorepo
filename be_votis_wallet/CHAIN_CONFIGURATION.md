# Chain Configuration Documentation

This document provides comprehensive information about blockchain configurations in the ChainConfig module, including which chains use custom derivation paths versus fallback behavior.

## Overview

The ChainConfig module manages blockchain configurations for multi-chain wallet operations, supporting both traditional blockchains and EVM-compatible chains with intelligent fallback behavior.

## Chain Categories

### 1. Chains with Custom Derivation Paths

These chains have registered SLIP-44 coin types and use their own unique BIP-44 derivation paths:

#### Non-EVM Chains

| Chain | Symbol | SLIP-44 | Derivation Path | Address Format |
|-------|--------|---------|-----------------|----------------|
| Bitcoin | BTC | 0 | `m/44'/0'/0'/0/0` | `ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH` |
| Solana | SOL | 501 | `m/44'/501'/0'/0'` | `ADDRESS_FORMAT_SOLANA` |
| Tron | TRX | 195 | `m/44'/195'/0'/0/0` | `ADDRESS_FORMAT_TRON` |

#### EVM-Compatible Chains with Custom Paths

| Chain | Symbol | Chain ID | SLIP-44 | Derivation Path | Aliases |
|-------|--------|----------|---------|-----------------|---------|
| Ethereum | ETH | 1 | 60 | `m/44'/60'/0'/0/0` | - |
| Polygon | POL | 137 | 966 | `m/44'/966'/0'/0/0` | MATIC |
| Arbitrum One | ARB | 42161 | 9001 | `m/44'/9001'/0'/0/0` | - |
| Optimism | OP | 10 | 614 | `m/44'/614'/0'/0/0` | - |
| BNB Smart Chain | BNB | 56 | 714 | `m/44'/714'/0'/0/0` | - |
| Avalanche C-Chain | AVAX | 43114 | 9000 | `m/44'/9000'/0'/0/0` | - |
| Base | BASE | 8453 | 8453 | `m/44'/8453'/0'/0/0` | - |

### 2. EVM Chains Using Fallback Derivation

Any EVM-compatible chain not explicitly configured above automatically uses Ethereum's derivation path for maximum compatibility:

- **SLIP-44 coin type:** 60 (Ethereum's)
- **Derivation path:** `m/44'/60'/0'/0/0`
- **Curve:** `CURVE_SECP256K1`
- **Address format:** `ADDRESS_FORMAT_ETHEREUM`
- **Naming:** "EVM Chain {chain_id}"

#### Examples of Fallback Chains

| Chain | Chain ID | Status | Derivation Path |
|-------|----------|--------|-----------------|
| Linea | 59144 | Uses fallback | `m/44'/60'/0'/0/0` |
| Mantle | 5000 | Uses fallback | `m/44'/60'/0'/0/0` |
| Scroll | 534352 | Uses fallback | `m/44'/60'/0'/0/0` |
| zkSync Era | 324 | Uses fallback | `m/44'/60'/0'/0/0` |
| Any new EVM chain | Various | Uses fallback | `m/44'/60'/0'/0/0` |

## Usage Examples

### Basic Chain Lookup

```elixir
# Get Polygon by primary symbol
{:ok, pol_chain} = ChainConfig.get("POL")

# Get Polygon by alias
{:ok, matic_chain} = ChainConfig.get("MATIC")

# Both return the same chain
pol_chain == matic_chain  # true

# Get chain by Chain ID
{:ok, polygon} = ChainConfig.get_by_chain_id(137)
```

### Derivation Path Analysis

```elixir
# Check if a chain has custom derivation path
ChainConfig.has_custom_derivation_path?(137)    # true (Polygon)
ChainConfig.has_custom_derivation_path?(999999) # false (fallback)

# Get detailed derivation information
{:custom, path, reason} = ChainConfig.get_derivation_info(137)
# Returns: {:custom, "m/44'/966'/0'/0/0", "EVM chain with registered SLIP-44 coin type 966"}

{:fallback, path, reason} = ChainConfig.get_derivation_info(999999)
# Returns: {:fallback, "m/44'/60'/0'/0/0", "Uses Ethereum fallback derivation"}
```

### Listing Chains

```elixir
# List all supported chains
ChainConfig.list()
# [:arbitrum, :avalanche, :base, :bitcoin, :bnb_smart_chain, :ethereum, :optimism, :polygon, :solana, :tron]

# List only EVM-compatible chains
ChainConfig.list_evm_chains()
# [:arbitrum, :avalanche, :base, :bnb_smart_chain, :ethereum, :optimism, :polygon]

# List chains with custom paths (all explicitly configured chains)
ChainConfig.list_chains_with_custom_paths()
# Same as list() - all configured chains have custom paths
```

## HD Wallet Implications

### Custom Derivation Paths

**Advantages:**
- **Security Isolation:** Each chain has separate key derivation, preventing cross-chain key exposure
- **Standards Compliance:** Follows official BIP-44/SLIP-44 specifications
- **Clear Audit Trail:** Each blockchain network has distinct derivation paths for tracking
- **Future-Proof:** Registered SLIP-44 coin types ensure long-term compatibility

**When Generated:**
- Different private keys and addresses for each chain
- Requires separate account creation for each supported chain
- Each chain maintains independent security boundaries

### Fallback Derivation Path

**Advantages:**
- **Immediate Compatibility:** Any new EVM chain works instantly without configuration
- **Address Consistency:** Same address across all fallback EVM chains
- **Simplified Management:** Single key derivation for multiple EVM networks
- **Reduced Complexity:** No need to track multiple derivation paths for similar chains

**When Generated:**
- Same private key and address across all fallback EVM chains
- Single account creation works for multiple EVM networks
- Shared security boundaries across fallback chains

### Security Considerations

1. **Custom Paths (Recommended for Production):**
   - Each chain isolated from others
   - Compromise of one chain doesn't affect others
   - Better for high-value or production environments

2. **Fallback Paths (Convenient for Development):**
   - Shared security across EVM chains
   - Compromise affects all fallback chains using same derivation
   - Better for development or low-risk environments

## Symbol Aliases

The module supports symbol aliases for backward compatibility:

| Chain | Primary Symbol | Aliases | Reason |
|-------|----------------|---------|---------|
| Polygon | POL | MATIC | POL is the new native token, MATIC is legacy |

### Adding New Aliases

To add symbol aliases for other chains:

```elixir
# In the chain configuration
some_chain: %Chain{
  symbol: "NEW_SYMBOL",
  symbol_aliases: ["OLD_SYMBOL", "ALTERNATIVE_SYMBOL"],
  # ... other fields
}
```

## Integration Examples

### Wallet Creation

```elixir
# Create wallet with custom derivation path (Polygon)
{:ok, polygon_chain} = ChainConfig.get_by_chain_id(137)
# Uses: m/44'/966'/0'/0/0

# Create wallet with fallback derivation (unknown EVM chain)  
{:ok, unknown_chain} = ChainConfig.get_by_chain_id(999999)
# Uses: m/44'/60'/0'/0/0 (Ethereum's path)
```

### Multi-Chain Wallet Support

```elixir
# Get configurations for multiple chains
evm_chains = ChainConfig.list_evm_chains()
chains_config = Enum.map(evm_chains, fn chain_key ->
  {:ok, chain} = ChainConfig.get(chain_key)
  {chain_key, chain}
end)

# Each chain will have appropriate derivation path:
# - Custom paths for configured chains
# - Fallback path for unknown chains
```

## References

- [SLIP-0044: Registered coin types](https://github.com/satoshilabs/slips/blob/master/slip-0044.md)
- [BIP-44: Multi-Account Hierarchy](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki)
- [EVM Chain IDs](https://chainlist.org/)
- [Polygon POL Migration](https://polygon.technology/blog/save-the-date-unpacking-the-pol-migration)
- [Turnkey API Documentation](https://docs.turnkey.com)

---

*This documentation is automatically generated based on the ChainConfig module configuration. For the most up-to-date information, refer to the module documentation and source code.*
