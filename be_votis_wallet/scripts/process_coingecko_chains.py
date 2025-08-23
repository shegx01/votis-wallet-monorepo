#!/usr/bin/env python3
"""
Script to process top 65 chains from CoinGecko by TVL and identify which ones are missing from our ChainConfig.

This script analyzes the CoinGecko chains data and generates configuration entries for chains
that are not already supported in our wallet configuration.
"""

# Current chains we already have configured (from both DeFiLlama and our original work)
EXISTING_CHAINS = {
    # Non-EVM chains
    'bitcoin', 'solana', 'tron', 'kava', 'litecoin', 'dogecoin', 'iotex', 'terra',
    'aptos', 'hedera', 'cosmos', 'kusama', 'polkadot', 'flow', 'zilliqa', 'neo', 'secret',
    
    # EVM chains we already have
    'ethereum', 'polygon', 'arbitrum', 'optimism', 'bnb_smart_chain', 'avalanche', 'base',
    'harmony', 'mantle', 'celo', 'sonic', 'moonbeam', 'fraxtal', 'taiko', 'gnosis',
    'fantom', 'zksync_era', 'linea', 'scroll', 'blast', 'polygon_zkevm', 'arbitrum_nova',
    'cronos', 'manta', 'zircuit', 'klaytn'
}

# CoinGecko top 65 chains data (extracted from the web page content)
COINGECKO_CHAINS = [
    {"rank": 1, "name": "Ethereum", "symbol": "ETH", "chain_id": 1, "existing": True},
    {"rank": 2, "name": "Solana", "symbol": "SOL", "chain_id": None, "existing": True},
    {"rank": 3, "name": "Bitcoin", "symbol": "BTC", "chain_id": None, "existing": True},
    {"rank": 4, "name": "BNB Smart Chain", "symbol": "BNB", "chain_id": 56, "existing": True},
    {"rank": 5, "name": "TRON", "symbol": "TRX", "chain_id": None, "existing": True},
    {"rank": 6, "name": "Base", "symbol": "ETH", "chain_id": 8453, "existing": True},
    {"rank": 7, "name": "Arbitrum One", "symbol": "ARB", "chain_id": 42161, "existing": True},
    {"rank": 8, "name": "Hyperliquid", "symbol": "HYPE", "chain_id": None, "is_evm": False},
    {"rank": 9, "name": "Sui", "symbol": "SUI", "chain_id": None, "is_evm": False},
    {"rank": 10, "name": "Avalanche", "symbol": "AVAX", "chain_id": 43114, "existing": True},
    {"rank": 11, "name": "Polygon POS", "symbol": "POL", "chain_id": 137, "existing": True},
    {"rank": 12, "name": "Aptos", "symbol": "APT", "chain_id": None, "existing": True},
    {"rank": 13, "name": "Cronos", "symbol": "CRO", "chain_id": 25, "existing": True},
    {"rank": 14, "name": "Linea", "symbol": "ETH", "chain_id": 59144, "existing": True},
    {"rank": 15, "name": "Sei Network", "symbol": "SEI", "chain_id": None, "is_evm": False},
    {"rank": 16, "name": "Unichain", "symbol": "ETH", "chain_id": 1301, "is_evm": True},
    {"rank": 17, "name": "Katana", "symbol": "ETH", "chain_id": 747474, "is_evm": True},
    {"rank": 18, "name": "Optimism", "symbol": "OP", "chain_id": 10, "existing": True},
    {"rank": 19, "name": "Sonic", "symbol": "S", "chain_id": 146, "existing": True},
    {"rank": 20, "name": "Cardano", "symbol": "ADA", "chain_id": None, "is_evm": False},
    {"rank": 21, "name": "Pulsechain", "symbol": "PLS", "chain_id": 369, "is_evm": True},
    {"rank": 22, "name": "Berachain", "symbol": "BERA", "chain_id": 80084, "is_evm": True},
    {"rank": 23, "name": "Core", "symbol": "CORE", "chain_id": 1116, "is_evm": True},
    {"rank": 24, "name": "Gnosis Chain", "symbol": "GNO", "chain_id": 100, "existing": True},
    {"rank": 25, "name": "Scroll", "symbol": "ETH", "chain_id": 534352, "existing": True},
    {"rank": 26, "name": "Rootstock RSK", "symbol": "RBTC", "chain_id": 30, "is_evm": True},
    {"rank": 27, "name": "Plume Network", "symbol": "PLUME", "chain_id": 98865, "is_evm": True},
    {"rank": 28, "name": "Mantle", "symbol": "MNT", "chain_id": 5000, "existing": True},
    {"rank": 29, "name": "Bob Network", "symbol": "ETH", "chain_id": 60808, "is_evm": True},
    {"rank": 30, "name": "Near Protocol", "symbol": "NEAR", "chain_id": None, "is_evm": False},
    {"rank": 31, "name": "TON", "symbol": "TON", "chain_id": None, "is_evm": False},
    {"rank": 32, "name": "Stellar", "symbol": "XLM", "chain_id": None, "is_evm": False},
    {"rank": 33, "name": "Hemi", "symbol": "ETH", "chain_id": 43111, "is_evm": True},
    {"rank": 34, "name": "Flare Network", "symbol": "FLR", "chain_id": 14, "is_evm": True},
    {"rank": 35, "name": "StarkNet", "symbol": "STRK", "chain_id": None, "is_evm": False},  # Uses Cairo VM
    {"rank": 36, "name": "Hedera Hashgraph", "symbol": "HBAR", "chain_id": 295, "existing": True},
    {"rank": 37, "name": "Soneium", "symbol": "ETH", "chain_id": 1868, "is_evm": True},
    {"rank": 38, "name": "Kava", "symbol": "KAVA", "chain_id": 2222, "is_evm": True},  # Kava has EVM compatibility
    {"rank": 39, "name": "Stacks", "symbol": "STX", "chain_id": None, "is_evm": False},
    {"rank": 40, "name": "Provenance", "symbol": "HASH", "chain_id": None, "is_evm": False},
    {"rank": 41, "name": "Movement", "symbol": "MOVE", "chain_id": None, "is_evm": False},
    {"rank": 42, "name": "XRP Ledger", "symbol": "XRP", "chain_id": None, "is_evm": False},
    {"rank": 43, "name": "Kaia", "symbol": "KAIA", "chain_id": 8217, "is_evm": True},  # Formerly Klaytn
    {"rank": 44, "name": "Celo", "symbol": "CELO", "chain_id": 42220, "existing": True},
    {"rank": 45, "name": "Flow", "symbol": "FLOW", "chain_id": 747, "existing": True},
    {"rank": 46, "name": "Fraxtal", "symbol": "FXTL", "chain_id": 252, "existing": True},
    {"rank": 47, "name": "BounceBit", "symbol": "BB", "chain_id": 6001, "is_evm": True},
    {"rank": 48, "name": "Blast", "symbol": "ETH", "chain_id": 81457, "existing": True},
    {"rank": 49, "name": "World Chain", "symbol": "WLD", "chain_id": 480, "is_evm": True},
    {"rank": 50, "name": "Osmosis", "symbol": "OSMO", "chain_id": None, "is_evm": False},
    {"rank": 51, "name": "Ronin", "symbol": "RON", "chain_id": 2020, "is_evm": True},
    {"rank": 52, "name": "zkSync", "symbol": "ZK", "chain_id": 324, "existing": True},
    {"rank": 53, "name": "Abstract", "symbol": "ETH", "chain_id": 11124, "is_evm": True},
    {"rank": 54, "name": "Merlin Chain", "symbol": "MERL", "chain_id": 4200, "is_evm": True},
    {"rank": 55, "name": "Goat", "symbol": "GOAT", "chain_id": None, "is_evm": False},
    {"rank": 56, "name": "MultiversX", "symbol": "EGLD", "chain_id": None, "is_evm": False},
    {"rank": 57, "name": "Etherlink", "symbol": "ETH", "chain_id": 42793, "is_evm": True},
    {"rank": 58, "name": "Morph L2", "symbol": "ETH", "chain_id": 2818, "is_evm": True},
    {"rank": 59, "name": "Metis Andromeda", "symbol": "METIS", "chain_id": 1088, "is_evm": True},
    {"rank": 60, "name": "Filecoin", "symbol": "FIL", "chain_id": 314, "is_evm": True},
    {"rank": 61, "name": "Corn", "symbol": "CORN", "chain_id": 21000000, "is_evm": True},
    {"rank": 62, "name": "Story", "symbol": "STORY", "chain_id": 1513, "is_evm": True},
    {"rank": 63, "name": "Waves", "symbol": "WAVES", "chain_id": None, "is_evm": False},
    {"rank": 64, "name": "Rollux", "symbol": "ETH", "chain_id": 570, "is_evm": True},
    {"rank": 65, "name": "Swellchain", "symbol": "ETH", "chain_id": 1923, "is_evm": True},
]

# Known SLIP-44 coin types for non-EVM chains
SLIP44_COIN_TYPES = {
    'cardano': 1815,
    'stellar': 148,
    'near': 397,
    'ton': 607,
    'xrp': 144,
    'stacks': 5757,
    'osmosis': 118,  # Uses Cosmos SDK
    'multiversx': 508,
    'waves': 5741564,
    'sui': 784,
}

def normalize_name_to_atom(name: str) -> str:
    """Convert chain name to a valid Elixir atom."""
    # Replace spaces and special chars with underscores
    sanitized = name.lower().replace(' ', '_').replace('-', '_')
    sanitized = sanitized.replace('chain', '').replace('network', '').replace('protocol', '')
    sanitized = sanitized.replace('_l2', '_layer2').replace('__', '_').strip('_')
    
    # Handle special cases
    replacements = {
        'gnosis_': 'gnosis',
        'rootstock_rsk': 'rootstock',
        'plume_': 'plume',
        'bob_': 'bob',
        'near_': 'near',
        'flare_': 'flare',
        'hedera_hashgraph': 'hedera',
        'xrp_ledger': 'xrp',
        'world_': 'world',
        'merlin_': 'merlin',
        'metis_andromeda': 'metis',
        'morph_layer2': 'morph',
    }
    
    return replacements.get(sanitized, sanitized)

def generate_new_chains():
    """Generate configuration for new chains that aren't in our existing set."""
    new_evm_chains = []
    new_non_evm_chains = []
    
    for chain in COINGECKO_CHAINS:
        if chain.get("existing", False):
            continue  # Skip chains we already have
            
        atom_name = normalize_name_to_atom(chain["name"])
        
        # Skip if we already have this chain
        if atom_name in EXISTING_CHAINS:
            continue
            
        is_evm = chain.get("is_evm", True)  # Default to EVM if not specified
        
        chain_config = {
            'atom_name': atom_name,
            'chain_id': chain.get('chain_id'),
            'name': chain['name'],
            'symbol': chain['symbol'],
            'is_evm_compatible': is_evm,
            'rank': chain['rank']
        }
        
        if is_evm:
            new_evm_chains.append(chain_config)
        else:
            # Get SLIP-44 coin type for non-EVM chains
            coin_type = SLIP44_COIN_TYPES.get(atom_name, 60)  # Default to Ethereum if unknown
            chain_config['slip44_coin_type'] = coin_type
            new_non_evm_chains.append(chain_config)
    
    return new_evm_chains, new_non_evm_chains

def print_elixir_config():
    """Print the Elixir configuration for new chains."""
    new_evm_chains, new_non_evm_chains = generate_new_chains()
    
    print("="*60)
    print("NEW CHAINS TO ADD FROM COINGECKO TOP 65")
    print("="*60)
    
    print(f"\nFound {len(new_evm_chains)} new EVM chains and {len(new_non_evm_chains)} new non-EVM chains to add\n")
    
    if new_non_evm_chains:
        print("# New Non-EVM chains:")
        for chain in sorted(new_non_evm_chains, key=lambda x: x['rank']):
            path = f"m/44'/{chain['slip44_coin_type']}'/0'/0/0"
            if chain['atom_name'] in ['sui']:  # Special path format for some chains
                path = f"m/44'/{chain['slip44_coin_type']}'/0'/0'"
                
            print(f"""      {chain['atom_name']}: %Chain{{
        chain_id: {f"{chain['chain_id']}" if chain['chain_id'] else "nil"},
        name: "{chain['name']}",
        symbol: "{chain['symbol']}",
        symbol_aliases: [],
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "{path}",
        path_format: @path_format,
        slip44_coin_type: {chain['slip44_coin_type']},
        is_evm_compatible: false
      }},""")
    
    if new_evm_chains:
        print(f"\n# New EVM-compatible chains (all use Ethereum derivation path):")
        for chain in sorted(new_evm_chains, key=lambda x: x['rank']):
            print(f"""      {chain['atom_name']}: %Chain{{
        chain_id: {f"{chain['chain_id']}" if chain['chain_id'] else "nil"},
        name: "{chain['name']}",
        symbol: "{chain['symbol']}",
        symbol_aliases: [],
        curve: @evm_curve,
        address_format: @evm_address_format,
        path: "m/44'/60'/0'/0/0",  # Same as Ethereum for fund accessibility
        path_format: @path_format,
        slip44_coin_type: 60,        # Same as Ethereum for fund accessibility
        is_evm_compatible: true
      }},""")
    
    # Generate chain ID mappings
    print(f"\n# Chain ID mappings for new chains:")
    all_new_chains = new_evm_chains + new_non_evm_chains
    for chain in sorted(all_new_chains, key=lambda x: x['rank']):
        if chain['chain_id'] and str(chain['chain_id']).isdigit():
            print(f"      {chain['chain_id']} => :{chain['atom_name']},")

if __name__ == "__main__":
    print_elixir_config()
