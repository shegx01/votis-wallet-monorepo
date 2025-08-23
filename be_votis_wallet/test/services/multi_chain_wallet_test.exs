defmodule BeVotisWallet.Services.MultiChainWalletTest do
  use ExUnit.Case, async: true

  import Mox

  alias BeVotisWallet.Services.MultiChainWallet
  alias BeVotisWallet.HTTPClient.Mock
  alias BeVotisWallet.BlockchainRegistry.Chain

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  setup do
    # Reset custom chains and ensure clean state
    Application.put_env(:be_votis_wallet, :custom_chains, %{})

    # Common test data
    test_data = %{
      organization_id: "org_123",
      user_id: "user_456",
      wallet_id: "wallet_789",
      wallet_name: "Test Wallet"
    }

    {:ok, test_data}
  end

  describe "create_wallet_for_chain/4" do
    test "successfully creates ethereum wallet", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      expected_response = %{
        "activity" => %{
          "id" => "activity_123",
          "result" => %{
            "createWalletResult" => %{
              "walletId" => "wallet_abc",
              "addresses" => [
                %{"address" => "0x1234...", "format" => "ADDRESS_FORMAT_ETHEREUM"}
              ]
            }
          }
        }
      }

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")
        assert [{"Content-Type", "application/json"}, {"X-Turnkey-API-Key", _}] = headers

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_WALLET"
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["parameters"]["userId"] == user_id
        assert decoded_body["parameters"]["walletName"] == wallet_name

        # Verify Ethereum account specification
        accounts = decoded_body["parameters"]["accounts"]
        assert length(accounts) == 1

        account = hd(accounts)
        assert account["curve"] == "CURVE_SECP256K1"
        assert account["pathFormat"] == "PATH_FORMAT_BIP32"
        assert account["path"] == "m/44'/60'/0'/0/0"
        assert account["addressFormat"] == "ADDRESS_FORMAT_ETHEREUM"

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :ethereum)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "successfully creates bitcoin wallet with correct specifications", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      expected_response = %{
        "activity" => %{
          "id" => "activity_456",
          "result" => %{"createWalletResult" => %{"walletId" => "wallet_def"}}
        }
      }

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        account = hd(decoded_body["parameters"]["accounts"])

        # Verify Bitcoin account specification
        assert account["curve"] == "CURVE_SECP256K1"
        assert account["pathFormat"] == "PATH_FORMAT_BIP32"
        assert account["path"] == "m/44'/0'/0'/0/0"
        assert account["addressFormat"] == "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :bitcoin)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "successfully creates solana wallet with ed25519 curve", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        account = hd(decoded_body["parameters"]["accounts"])

        # Verify Solana account specification (uses ed25519)
        assert account["curve"] == "CURVE_ED25519"
        assert account["pathFormat"] == "PATH_FORMAT_BIP32"
        assert account["path"] == "m/44'/501'/0'/0'"
        assert account["addressFormat"] == "ADDRESS_FORMAT_SOLANA"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_sol"}}}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :solana)
      assert {:ok, _response} = result
    end

    test "successfully creates tron wallet", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        account = hd(decoded_body["parameters"]["accounts"])

        # Verify Tron account specification
        assert account["curve"] == "CURVE_SECP256K1"
        assert account["pathFormat"] == "PATH_FORMAT_BIP32"
        assert account["path"] == "m/44'/195'/0'/0/0"
        assert account["addressFormat"] == "ADDRESS_FORMAT_TRON"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_tron"}}}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :tron)
      assert {:ok, _response} = result
    end

    test "handles string identifiers", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        account = hd(decoded_body["parameters"]["accounts"])

        assert account["addressFormat"] == "ADDRESS_FORMAT_ETHEREUM"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_str"}}}
      end)

      # Test various string formats
      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, "ethereum")
      assert {:ok, _response} = result
    end

    test "returns error for unsupported chain", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      result =
        MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :unknown_chain)

      assert {:error, :unsupported_chain} = result
    end

    test "propagates Activities service errors", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: "test"}
      end)
      |> expect(:request, fn _payload ->
        {:error, 500, "Internal server error"}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :ethereum)

      assert {:error, 500, "Internal server error"} = result
    end
  end

  describe "create_multi_chain_wallet/4" do
    test "successfully creates wallet with multiple chains", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      expected_response = %{
        "activity" => %{
          "id" => "activity_multi",
          "result" => %{
            "createWalletResult" => %{
              "walletId" => "wallet_multi",
              "addresses" => [
                %{"address" => "0x1234...", "format" => "ADDRESS_FORMAT_ETHEREUM"},
                %{"address" => "bc1...", "format" => "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH"},
                %{"address" => "So11...", "format" => "ADDRESS_FORMAT_SOLANA"}
              ]
            }
          }
        }
      }

      chains = [:ethereum, :bitcoin, :solana]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        accounts = decoded_body["parameters"]["accounts"]

        # Should have 3 accounts for the 3 chains
        assert length(accounts) == 3

        # Verify account specifications are correct
        ethereum_account =
          Enum.find(accounts, &(&1["addressFormat"] == "ADDRESS_FORMAT_ETHEREUM"))

        assert ethereum_account["curve"] == "CURVE_SECP256K1"
        assert ethereum_account["path"] == "m/44'/60'/0'/0/0"

        bitcoin_account =
          Enum.find(accounts, &(&1["addressFormat"] == "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH"))

        assert bitcoin_account["curve"] == "CURVE_SECP256K1"
        assert bitcoin_account["path"] == "m/44'/0'/0'/0/0"

        solana_account = Enum.find(accounts, &(&1["addressFormat"] == "ADDRESS_FORMAT_SOLANA"))
        assert solana_account["curve"] == "CURVE_ED25519"
        assert solana_account["path"] == "m/44'/501'/0'/0'"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, chains)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "creates wallet with all 4 supported chains", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      chains = [:ethereum, :bitcoin, :solana, :tron]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        accounts = decoded_body["parameters"]["accounts"]

        # Should have 4 accounts
        assert length(accounts) == 4

        # Verify all chains are represented
        address_formats = Enum.map(accounts, & &1["addressFormat"])

        assert "ADDRESS_FORMAT_ETHEREUM" in address_formats
        assert "ADDRESS_FORMAT_BITCOIN_MAINNET_P2WPKH" in address_formats
        assert "ADDRESS_FORMAT_SOLANA" in address_formats
        assert "ADDRESS_FORMAT_TRON" in address_formats

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_all_chains"}}}
      end)

      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, chains)
      assert {:ok, _response} = result
    end

    test "handles mixed identifier formats", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      # Mix of atoms and strings, including symbol lookups
      chains = [:ethereum, "bitcoin", "SOL", :tron]

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        accounts = decoded_body["parameters"]["accounts"]

        assert length(accounts) == 4

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_mixed"}}}
      end)

      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, chains)
      assert {:ok, _response} = result
    end

    test "returns error for unsupported chain in list", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      chains = [:ethereum, :bitcoin, :unknown_chain, :solana]

      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, chains)

      assert {:error, {:unsupported_chain, :unknown_chain}} = result
    end

    test "fails fast on first unsupported chain", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      chains = [:unknown1, :ethereum, :unknown2]

      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, chains)

      assert {:error, {:unsupported_chain, :unknown1}} = result
    end

    test "propagates Activities service errors", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: "test"}
      end)
      |> expect(:request, fn _payload ->
        {:error, 400, "Invalid request"}
      end)

      chains = [:ethereum, :bitcoin]

      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, chains)

      assert {:error, 400, "Invalid request"} = result
    end

    test "handles empty chain list", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        accounts = decoded_body["parameters"]["accounts"]

        # Should have no accounts
        assert accounts == []

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_empty"}}}
      end)

      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, [])
      assert {:ok, _response} = result
    end
  end

  describe "add_account/3" do
    test "successfully adds ethereum account to existing wallet", %{
      organization_id: org_id,
      wallet_id: wallet_id
    } do
      expected_response = %{
        "activity" => %{
          "id" => "activity_add",
          "result" => %{
            "createWalletAccountsResult" => %{
              "addresses" => [
                %{"address" => "0x5678...", "format" => "ADDRESS_FORMAT_ETHEREUM"}
              ]
            }
          }
        }
      }

      Mock
      |> expect(:build_payload, fn method, url, headers, body ->
        assert method == :post
        assert String.contains?(url, "/public/v1/submit/activity")

        decoded_body = Jason.decode!(body)
        assert decoded_body["type"] == "ACTIVITY_TYPE_CREATE_WALLET_ACCOUNTS"
        assert decoded_body["organizationId"] == org_id
        assert decoded_body["parameters"]["walletId"] == wallet_id

        # Verify Ethereum account specification
        accounts = decoded_body["parameters"]["accounts"]
        assert length(accounts) == 1

        account = hd(accounts)
        assert account["curve"] == "CURVE_SECP256K1"
        assert account["pathFormat"] == "PATH_FORMAT_BIP32"
        assert account["path"] == "m/44'/60'/0'/0/0"
        assert account["addressFormat"] == "ADDRESS_FORMAT_ETHEREUM"

        %{method: method, url: url, headers: headers, body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, expected_response}
      end)

      result = MultiChainWallet.add_account(org_id, wallet_id, :ethereum)

      assert {:ok, response} = result
      assert response == expected_response
    end

    test "successfully adds solana account", %{organization_id: org_id, wallet_id: wallet_id} do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        account = hd(decoded_body["parameters"]["accounts"])

        # Verify Solana specifications
        assert account["curve"] == "CURVE_ED25519"
        assert account["addressFormat"] == "ADDRESS_FORMAT_SOLANA"
        assert account["path"] == "m/44'/501'/0'/0'"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_add_solana"}}}
      end)

      result = MultiChainWallet.add_account(org_id, wallet_id, :solana)
      assert {:ok, _response} = result
    end

    test "handles string chain identifiers", %{organization_id: org_id, wallet_id: wallet_id} do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        account = hd(decoded_body["parameters"]["accounts"])

        assert account["addressFormat"] == "ADDRESS_FORMAT_TRON"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_add_tron"}}}
      end)

      result = MultiChainWallet.add_account(org_id, wallet_id, "TRON")
      assert {:ok, _response} = result
    end

    test "returns error for unsupported chain", %{organization_id: org_id, wallet_id: wallet_id} do
      result = MultiChainWallet.add_account(org_id, wallet_id, :unsupported)

      assert {:error, :unsupported_chain} = result
    end

    test "propagates Activities service errors", %{organization_id: org_id, wallet_id: wallet_id} do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: "test"}
      end)
      |> expect(:request, fn _payload ->
        {:error, 404, "Wallet not found"}
      end)

      result = MultiChainWallet.add_account(org_id, wallet_id, :bitcoin)

      assert {:error, 404, "Wallet not found"} = result
    end
  end

  describe "integration with custom chains" do
    test "works with custom registered chains", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      # Register a custom chain
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

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        account = hd(decoded_body["parameters"]["accounts"])

        # Verify custom chain specifications
        assert account["curve"] == "CURVE_SECP256K1"
        assert account["addressFormat"] == "ADDRESS_FORMAT_ETHEREUM"
        assert account["path"] == "m/44'/966'/0'/0/0"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_polygon"}}}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :polygon)
      assert {:ok, _response} = result
    end

    test "includes custom chains in multi-chain wallet", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      # Register custom chain
      custom_chain = %Chain{
        name: "Avalanche",
        symbol: "AVAX",
        curve: "CURVE_SECP256K1",
        address_format: "ADDRESS_FORMAT_ETHEREUM",
        path: "m/44'/9000'/0'/0/0",
        path_format: "PATH_FORMAT_BIP32",
        slip44_coin_type: 9000
      }

      Application.put_env(:be_votis_wallet, :custom_chains, %{avalanche: custom_chain})

      Mock
      |> expect(:build_payload, fn _method, _url, _headers, body ->
        decoded_body = Jason.decode!(body)
        accounts = decoded_body["parameters"]["accounts"]

        assert length(accounts) == 2

        # Find custom chain account
        avax_account = Enum.find(accounts, &(&1["path"] == "m/44'/9000'/0'/0/0"))
        assert avax_account["curve"] == "CURVE_SECP256K1"

        %{method: :post, url: "test", headers: [], body: body}
      end)
      |> expect(:request, fn _payload ->
        {:ok, %{"activity" => %{"id" => "activity_multi_custom"}}}
      end)

      chains = [:ethereum, :avalanche]
      result = MultiChainWallet.create_multi_chain_wallet(org_id, user_id, wallet_name, chains)
      assert {:ok, _response} = result
    end
  end

  describe "error edge cases" do
    test "handles malformed responses from Activities service", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: "test"}
      end)
      |> expect(:request, fn _payload ->
        # Return malformed success response
        {:ok, %{"malformed" => "response"}}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :ethereum)

      # Should still return the response as-is, letting caller handle it
      assert {:ok, %{"malformed" => "response"}} = result
    end

    test "handles network timeout errors", %{
      organization_id: org_id,
      user_id: user_id,
      wallet_name: wallet_name
    } do
      Mock
      |> expect(:build_payload, fn _method, _url, _headers, _body ->
        %{method: :post, url: "test", headers: [], body: "test"}
      end)
      |> expect(:request, fn _payload ->
        {:error, 0, :timeout}
      end)

      result = MultiChainWallet.create_wallet_for_chain(org_id, user_id, wallet_name, :bitcoin)

      assert {:error, 0, :timeout} = result
    end
  end
end
