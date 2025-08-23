#!/usr/bin/env python3
"""
Script to process top 100 chains from DeFiLlama API and categorize them for ChainConfig integration.

This script fetches chain data from DeFiLlama, categorizes chains as EVM-compatible or non-EVM,
and generates the appropriate configuration entries for the Elixir ChainConfig module.
"""

import json
import requests
from typing import Dict, List, Tuple, Optional
import re

# Known non-EVM chains (these have their own virtual machines/architectures)
NON_EVM_CHAINS = {
    'bitcoin', 'btc', 'litecoin', 'ltc', 'dogecoin', 'doge',
    'solana', 'sol', 'aptos', 'apt', 'flow', 'cosmos', 'atom',
    'polkadot', 'dot', 'kusama', 'ksm', 'near', 'cardano', 'ada',
    'algorand', 'algo', 'tron', 'trx', 'xrp', 'ripple',
    'stellar', 'xlm', 'monero', 'xmr', 'zcash', 'zec',
    'terra', 'luna', 'lunc', 'secret', 'scrt', 'osmosis', 'osmo',
    'juno', 'stafi', 'fis', 'bifrost', 'bnc', 'hedera', 'hbar',
    'zilliqa', 'zil', 'neo', 'gas', 'eos', 'wax', 'iotx',
    'kava', 'hard', 'swp', 'usdx'
}

# SLIP-44 coin types for major non-EVM chains
SLIP44_COIN_TYPES = {
    'bitcoin': 0,
    'litecoin': 2,
    'dogecoin': 3,
    'ethereum': 60,
    'ethereum classic': 61,
    'ripple': 144,
    'tron': 195,
    'eos': 194,
    'stellar': 148,
    'monero': 128,
    'zcash': 133,
    'algorand': 283,
    'solana': 501,
    'polkadot': 354,
    'kusama': 434,
    'cardano': 1815,
    'near': 397,
    'cosmos': 118,
    'terra': 330,
    'secret': 529,
    'kava': 459,
    'osmosis': 118,  # Uses Cosmos SDK
    'juno': 118,     # Uses Cosmos SDK
    'stargaze': 118, # Uses Cosmos SDK
    'akash': 118,    # Uses Cosmos SDK
    'regen': 118,    # Uses Cosmos SDK
    'sentinel': 118, # Uses Cosmos SDK
    'persistence': 118, # Uses Cosmos SDK
    'crypto.org': 394,
    'binance': 714,
    'thorchain': 931,
    'mayachain': 931, # Same as THORChain
    'hedera': 3030,
    'zilliqa': 313,
    'neo': 888,
    'flow': 539,
    'aptos': 637,
    'sui': 784,
    'filecoin': 461,
    'chia': 8444,
    'handshake': 5353,
    'decred': 42,
    'iotex': 304,
    'waves': 5741564,
    'elrond': 508,
    'tezos': 1729,
    'vechain': 818
}

def normalize_chain_name(name: str) -> str:
    """Normalize chain name for comparison."""
    return name.lower().replace(' ', '').replace('-', '').replace('_', '')

def is_evm_compatible(chain_data: dict) -> bool:
    """
    Determine if a chain is EVM-compatible based on various indicators.
    """
    name = chain_data.get('name', '').lower()
    symbol = (chain_data.get('tokenSymbol') or '').lower()
    gecko_id = (chain_data.get('gecko_id') or '').lower()
    
    # Normalize name for checking
    normalized_name = normalize_chain_name(name)
    
    # Check if it's explicitly a non-EVM chain
    if any(non_evm in normalized_name or non_evm in symbol or non_evm in gecko_id 
           for non_evm in NON_EVM_CHAINS):
        return False
    
    # Has a chain ID (strong indicator of EVM compatibility)
    if chain_data.get('chainId') and str(chain_data.get('chainId')).isdigit():
        return True
        
    # Known EVM chain patterns
    evm_patterns = [
        'ethereum', 'eth', 'polygon', 'arbitrum', 'optimism', 'base', 'avalanche',
        'binance', 'bnb', 'fantom', 'harmony', 'moonbeam', 'moonriver', 'aurora',
        'celo', 'gnosis', 'xdai', 'metis', 'boba', 'cronos', 'evmos', 'kava',
        'oasis', 'fuse', 'velas', 'wanchain', 'thundercore', 'tomochain',
        'heco', 'okex', 'zksync', 'loopring', 'immutablex', 'starknet',
        'linea', 'scroll', 'mantle', 'blast', 'zircuit', 'taiko', 'mode',
        'fraxtal', 'manta', 'katana', 'ronin', 'astar', 'shiden',
        'klaytn', 'canto', 'energi', 'syscoin', 'rsk', 'rootstock'
    ]
    
    # Check for EVM patterns in name or gecko_id
    if any(pattern in normalized_name or pattern in gecko_id for pattern in evm_patterns):
        return True
    
    # If it has 'evm' in the name, it's likely EVM
    if 'evm' in normalized_name:
        return True
        
    # Default to EVM if uncertain and has chain ID
    if chain_data.get('chainId'):
        return True
    
    return False

def get_slip44_coin_type(chain_data: dict) -> int:
    """Get SLIP-44 coin type for a chain."""
    name = chain_data.get('name', '').lower()
    symbol = (chain_data.get('tokenSymbol') or '').lower()
    normalized_name = normalize_chain_name(name)
    
    # Check direct matches first
    for chain_key, coin_type in SLIP44_COIN_TYPES.items():
        if chain_key in normalized_name or chain_key in symbol:
            return coin_type
    
    # For EVM chains, use Ethereum's coin type
    if is_evm_compatible(chain_data):
        return 60
    
    # For unknown non-EVM chains, we'll need to assign unique coin types
    # or use a fallback strategy
    return 60  # Default to Ethereum for now

def generate_derivation_path(chain_data: dict, coin_type: int) -> str:
    """Generate derivation path based on chain type and coin type."""
    if is_evm_compatible(chain_data):
        # All EVM chains use Ethereum's derivation path
        return "m/44'/60'/0'/0/0"
    
    # For non-EVM chains, use their specific coin type
    name = chain_data.get('name', '').lower()
    
    # Special cases for different path formats
    if 'solana' in name:
        return f"m/44'/{coin_type}'/0'/0'"  # Solana uses shorter path
    elif any(cosmos_chain in name for cosmos_chain in ['cosmos', 'terra', 'secret', 'kava', 'osmosis', 'juno']):
        return f"m/44'/{coin_type}'/0'/0/0"  # Standard BIP44
    else:
        return f"m/44'/{coin_type}'/0'/0/0"  # Standard BIP44

def get_curve_and_address_format(chain_data: dict) -> Tuple[str, str]:
    """Get curve and address format for a chain."""
    name = chain_data.get('name', '').lower()
    
    if 'solana' in name:
        return "CURVE_ED25519", "ADDRESS_FORMAT_SOLANA"
    elif 'bitcoin' in name and 'bitcoin' == normalize_chain_name(name):
        return "CURVE_SECP256K1", "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH"
    elif 'tron' in name:
        return "CURVE_SECP256K1", "ADDRESS_FORMAT_TRON"
    elif 'flow' in name:
        return "CURVE_SECP256K1", "ADDRESS_FORMAT_FLOW"  # Flow has its own format
    else:
        # Default to EVM format for most chains
        return "CURVE_SECP256K1", "ADDRESS_FORMAT_ETHEREUM"

def sanitize_atom_name(name: str) -> str:
    """Convert chain name to a valid Elixir atom."""
    # Replace spaces and special chars with underscores
    sanitized = re.sub(r'[^a-zA-Z0-9_]', '_', name.lower())
    
    # Remove consecutive underscores
    sanitized = re.sub(r'_+', '_', sanitized)
    
    # Remove leading/trailing underscores
    sanitized = sanitized.strip('_')
    
    # Handle special cases
    replacements = {
        'xdai': 'gnosis',
        'bnb_smart_chain': 'bnb_smart_chain',
        'binance': 'bnb_smart_chain',
        'ethereum_classic': 'ethereum_classic',
        'okexchain': 'okex_chain',
        'op_bnb': 'opbnb',
        'arbitrum_nova': 'arbitrum_nova',
        'polygon_zkevm': 'polygon_zkevm',
        'zksync_era': 'zksync_era',
        'bnb_chain': 'bnb_smart_chain'
    }
    
    return replacements.get(sanitized, sanitized)

def process_chains():
    """Main function to process DeFiLlama chains."""
    print("Fetching top 100 chains from DeFiLlama...")
    
    # Fetch data from API
    response = requests.get("https://api.llama.fi/chains")
    chains_data = response.json()[:100]  # Get top 100
    
    print(f"Processing {len(chains_data)} chains...")
    
    evm_chains = []
    non_evm_chains = []
    
    for chain_data in chains_data:
        chain_id = chain_data.get('chainId')
        name = chain_data.get('name', '')
        symbol = chain_data.get('tokenSymbol', 'ETH')
        
        # Skip chains without proper data
        if not name:
            continue
            
        atom_name = sanitize_atom_name(name)
        coin_type = get_slip44_coin_type(chain_data)
        derivation_path = generate_derivation_path(chain_data, coin_type)
        curve, address_format = get_curve_and_address_format(chain_data)
        is_evm = is_evm_compatible(chain_data)
        
        chain_config = {
            'atom_name': atom_name,
            'chain_id': chain_id,
            'name': name,
            'symbol': symbol if symbol else 'ETH',
            'curve': curve,
            'address_format': address_format,
            'path': derivation_path,
            'slip44_coin_type': coin_type,
            'is_evm_compatible': is_evm,
            'gecko_id': chain_data.get('gecko_id')
        }
        
        if is_evm:
            evm_chains.append(chain_config)
        else:
            non_evm_chains.append(chain_config)
    
    print(f"\nCategorized chains:")
    print(f"EVM-compatible: {len(evm_chains)}")
    print(f"Non-EVM: {len(non_evm_chains)}")
    
    # Generate Elixir code
    generate_elixir_config(evm_chains, non_evm_chains)
    
    return evm_chains, non_evm_chains

def generate_elixir_config(evm_chains: List[dict], non_evm_chains: List[dict]):
    """Generate Elixir configuration code."""
    print("\n" + "="*50)
    print("ELIXIR CONFIGURATION")
    print("="*50)
    
    print("\n# Non-EVM chains with unique derivation paths:")
    for chain in non_evm_chains:
        if chain['atom_name'] not in ['bitcoin', 'solana', 'tron']:  # Skip already configured
            print(f"""      {chain['atom_name']}: %Chain{{
        chain_id: {f"{chain['chain_id']}" if chain['chain_id'] else "nil"},
        name: "{chain['name']}",
        symbol: "{chain['symbol']}",
        symbol_aliases: [],
        curve: "{chain['curve']}",
        address_format: "{chain['address_format']}",
        path: "{chain['path']}",
        path_format: @path_format,
        slip44_coin_type: {chain['slip44_coin_type']},
        is_evm_compatible: false
      }},""")
    
    print(f"\n# EVM-compatible chains (all use Ethereum derivation path):")
    for chain in evm_chains:
        if chain['atom_name'] not in ['ethereum', 'polygon', 'arbitrum', 'optimism', 'bnb_smart_chain', 'avalanche', 'base']:  # Skip already configured
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
    
    # Generate chain ID mapping
    print(f"\n# Chain ID mappings:")
    for chain in evm_chains + non_evm_chains:
        if chain['chain_id'] and str(chain['chain_id']).replace('-', '').isdigit():
            print(f"      {chain['chain_id']} => :{chain['atom_name']},")

if __name__ == "__main__":
    process_chains()
