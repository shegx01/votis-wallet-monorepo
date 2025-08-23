defmodule BeVotisWallet.BlockchainRegistryTest do
  use ExUnit.Case, async: true

  alias BeVotisWallet.BlockchainRegistry
  alias BeVotisWallet.BlockchainRegistry.Chain

  setup do
    # Reset custom chains before each test
    Application.put_env(:be_votis_wallet, :custom_chains, %{})
    :ok
  end

  describe "get/1 with atom identifiers" do
    test "returns ethereum chain for :ethereum" do
      {:ok, chain} = BlockchainRegistry.get(:ethereum)

      assert %Chain{
               name: "Ethereum",
               symbol: "ETH",
               curve: "CURVE_SECP256K1",
               address_format: "ADDRESS_FORMAT_ETHEREUM",
               path: "m/44'/60'/0'/0/0",
               path_format: "PATH_FORMAT_BIP32",
               slip44_coin_type: 60
             } = chain
    end

    test "returns bitcoin chain for :bitcoin" do
      {:ok, chain} = BlockchainRegistry.get(:bitcoin)

      assert %Chain{
               name: "Bitcoin",
               symbol: "BTC",
               curve: "CURVE_SECP256K1",
               address_format: "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH",
               path: "m/44'/0'/0'/0/0",
               path_format: "PATH_FORMAT_BIP32",
               slip44_coin_type: 0
             } = chain
    end

    test "returns solana chain for :solana" do
      {:ok, chain} = BlockchainRegistry.get(:solana)

      assert %Chain{
               name: "Solana",
               symbol: "SOL",
               curve: "CURVE_ED25519",
               address_format: "ADDRESS_FORMAT_SOLANA",
               path: "m/44'/501'/0'/0'",
               path_format: "PATH_FORMAT_BIP32",
               slip44_coin_type: 501
             } = chain
    end

    test "returns tron chain for :tron" do
      {:ok, chain} = BlockchainRegistry.get(:tron)

      assert %Chain{
               name: "Tron",
               symbol: "TRX",
               curve: "CURVE_SECP256K1",
               address_format: "ADDRESS_FORMAT_TRON",
               path: "m/44'/195'/0'/0/0",
               path_format: "PATH_FORMAT_BIP32",
               slip44_coin_type: 195
             } = chain
    end

    test "returns ethereum chain for :eth symbol" do
      {:ok, chain} = BlockchainRegistry.get(:eth)

      assert chain.name == "Ethereum"
      assert chain.symbol == "ETH"
    end

    test "returns bitcoin chain for :btc symbol" do
      {:ok, chain} = BlockchainRegistry.get(:btc)

      assert chain.name == "Bitcoin"
      assert chain.symbol == "BTC"
    end

    test "returns solana chain for :sol symbol" do
      {:ok, chain} = BlockchainRegistry.get(:sol)

      assert chain.name == "Solana"
      assert chain.symbol == "SOL"
    end

    test "returns error for unknown atom identifier" do
      assert {:error, :not_found} = BlockchainRegistry.get(:unknown)
      assert {:error, :not_found} = BlockchainRegistry.get(:invalid_chain)
    end
  end

  describe "get/1 with string identifiers" do
    test "returns ethereum chain for various string formats" do
      # Direct key match
      {:ok, chain} = BlockchainRegistry.get("ethereum")
      assert chain.name == "Ethereum"

      # Case insensitive key match
      {:ok, chain} = BlockchainRegistry.get("ETHEREUM")
      assert chain.name == "Ethereum"

      # Symbol match
      {:ok, chain} = BlockchainRegistry.get("ETH")
      assert chain.name == "Ethereum"

      {:ok, chain} = BlockchainRegistry.get("eth")
      assert chain.name == "Ethereum"

      # Name match
      {:ok, chain} = BlockchainRegistry.get("Ethereum")
      assert chain.name == "Ethereum"
    end

    test "returns bitcoin chain for various string formats" do
      {:ok, chain} = BlockchainRegistry.get("bitcoin")
      assert chain.name == "Bitcoin"

      {:ok, chain} = BlockchainRegistry.get("Bitcoin")
      assert chain.name == "Bitcoin"

      {:ok, chain} = BlockchainRegistry.get("BTC")
      assert chain.name == "Bitcoin"

      {:ok, chain} = BlockchainRegistry.get("btc")
      assert chain.name == "Bitcoin"
    end

    test "returns error for unknown string identifier" do
      assert {:error, :not_found} = BlockchainRegistry.get("unknown")
      assert {:error, :not_found} = BlockchainRegistry.get("INVALID")
      assert {:error, :not_found} = BlockchainRegistry.get("xyz")
    end
  end

  describe "get!/1" do
    test "returns chain struct for valid identifier" do
      chain = BlockchainRegistry.get!(:ethereum)
      assert %Chain{name: "Ethereum"} = chain
    end

    test "raises ArgumentError for unknown identifier" do
      assert_raise ArgumentError, "Unknown blockchain: :unknown", fn ->
        BlockchainRegistry.get!(:unknown)
      end

      assert_raise ArgumentError, "Unknown blockchain: \"invalid\"", fn ->
        BlockchainRegistry.get!("invalid")
      end
    end
  end

  describe "list/0" do
    test "returns all built-in chain identifiers sorted" do
      chains = BlockchainRegistry.list()

      assert chains == [:bitcoin, :ethereum, :solana, :tron]
    end

    test "includes custom chains when present" do
      # Add a custom chain
      custom_chain = %Chain{
        name: "Polygon",
        symbol: "MATIC",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/966'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 966
      }

      Application.put_env(:be_votis_wallet, :custom_chains, %{polygon: custom_chain})

      chains = BlockchainRegistry.list()

      assert chains == [:bitcoin, :ethereum, :polygon, :solana, :tron]
    end
  end

  describe "register/2" do
    test "registers a new custom chain" do
      custom_chain = %Chain{
        name: "Polygon",
        symbol: "MATIC",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/966'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 966
      }

      assert :ok = BlockchainRegistry.register(:polygon, custom_chain)

      # Should be retrievable
      {:ok, retrieved} = BlockchainRegistry.get(:polygon)
      assert retrieved == custom_chain

      # Should be in list
      assert :polygon in BlockchainRegistry.list()
    end

    test "can register chain with symbol lookup" do
      custom_chain = %Chain{
        name: "Cardano",
        symbol: "ADA",
        curve: "CURVE_ED25519",
        address_format: "ADDRESS_FORMAT_CARDANO",
        path: "m/44'/1815'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 1815
      }

      BlockchainRegistry.register(:cardano, custom_chain)

      # Should be retrievable by symbol
      {:ok, retrieved} = BlockchainRegistry.get(:ada)
      assert retrieved.name == "Cardano"

      {:ok, retrieved} = BlockchainRegistry.get("ADA")
      assert retrieved.name == "Cardano"
    end

    test "updates existing custom chain" do
      # Register initial version
      chain_v1 = %Chain{
        name: "Test Chain",
        symbol: "TEST",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/1000'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 1000
      }

      BlockchainRegistry.register(:test_chain, chain_v1)

      # Update with new version
      chain_v2 = %{chain_v1 | name: "Updated Test Chain"}
      BlockchainRegistry.register(:test_chain, chain_v2)

      # Should return updated version
      {:ok, retrieved} = BlockchainRegistry.get(:test_chain)
      assert retrieved.name == "Updated Test Chain"
    end

    test "persists custom chains across get calls" do
      custom_chain = %Chain{
        name: "Avalanche",
        symbol: "AVAX",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/9000'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 9000
      }

      BlockchainRegistry.register(:avalanche, custom_chain)

      # Multiple get calls should work
      {:ok, chain1} = BlockchainRegistry.get(:avalanche)
      {:ok, chain2} = BlockchainRegistry.get("AVAX")

      assert chain1 == chain2
      assert chain1.name == "Avalanche"
    end

    test "does not override built-in chains" do
      # Try to register over a built-in chain
      fake_ethereum = %Chain{
        name: "Fake Ethereum",
        symbol: "FAKE",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/60'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 60
      }

      BlockchainRegistry.register(:ethereum, fake_ethereum)

      # Built-in should take precedence (built-ins are merged first)
      {:ok, chain} = BlockchainRegistry.get(:ethereum)
      assert chain.name == "Ethereum"
      assert chain.symbol == "ETH"
    end
  end

  describe "path_format/0" do
    test "returns the standard BIP32 path format" do
      assert BlockchainRegistry.path_format() == "PATH_FORMAT_BIP32"
    end
  end

  describe "Chain struct" do
    test "has all required fields with correct types" do
      chain = %Chain{
        name: "Test",
        symbol: "TST",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/1'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 1
      }

      assert is_binary(chain.name)
      assert is_binary(chain.symbol)
      assert is_binary(chain.curve)
      assert is_binary(chain.address_format)
      assert is_binary(chain.path)
      assert is_binary(chain.path_format)
      assert is_integer(chain.slip44_coin_type)
      assert chain.slip44_coin_type >= 0
    end

    test "can be JSON encoded" do
      chain = %Chain{
        name: "Test",
        symbol: "TST",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/1'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 1
      }

      assert {:ok, json} = Jason.encode(chain)
      assert is_binary(json)

      # Should be decodable back
      assert {:ok, decoded} = Jason.decode(json)
      assert decoded["name"] == "Test"
      assert decoded["symbol"] == "TST"
    end
  end

  describe "integration with Application config" do
    test "reads custom chains from application environment" do
      custom_chains = %{
        polygon: %Chain{
          name: "Polygon",
          symbol: "MATIC",
          curve: "CURVE_SECP256K1",
          address_format: "ADDRESS_FORMAT_ETHEREUM",
          path: "m/44'/966'/0'/0/0",
          path_format: "PATH_FORMAT_BIP32",
          slip44_coin_type: 966
        },
        avalanche: %Chain{
          name: "Avalanche",
          symbol: "AVAX",
          curve: "CURVE_SECP256K1",
          address_format: "ADDRESS_FORMAT_ETHEREUM",
          path: "m/44'/9000'/0'/0/0",
          path_format: "PATH_FORMAT_BIP32",
          slip44_coin_type: 9000
        }
      }

      Application.put_env(:be_votis_wallet, :custom_chains, custom_chains)

      # Should find both custom chains
      {:ok, polygon} = BlockchainRegistry.get(:polygon)
      assert polygon.name == "Polygon"

      {:ok, avax} = BlockchainRegistry.get(:avalanche)
      assert avax.name == "Avalanche"

      # List should include both
      chains = BlockchainRegistry.list()
      assert :polygon in chains
      assert :avalanche in chains
    end

    test "handles empty custom chains config" do
      Application.put_env(:be_votis_wallet, :custom_chains, %{})

      # Should still work with built-ins
      {:ok, eth} = BlockchainRegistry.get(:ethereum)
      assert eth.name == "Ethereum"

      # List should only have built-ins
      assert BlockchainRegistry.list() == [:bitcoin, :ethereum, :solana, :tron]
    end

    test "handles missing custom chains config" do
      Application.delete_env(:be_votis_wallet, :custom_chains)

      # Should still work with built-ins
      {:ok, btc} = BlockchainRegistry.get(:bitcoin)
      assert btc.name == "Bitcoin"

      assert BlockchainRegistry.list() == [:bitcoin, :ethereum, :solana, :tron]
    end
  end
end
