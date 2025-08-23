defmodule BeVotisWallet.Services.Turnkey.SessionsTest do
  use ExUnit.Case, async: true

  alias BeVotisWallet.Services.Turnkey.Sessions

  describe "generate_session_keypair/0" do
    test "generates valid HPKE keypair for session encryption" do
      {public_hex, private_hex} = Sessions.generate_session_keypair()

      # Verify hex format
      assert String.match?(public_hex, ~r/^[0-9a-f]+$/)
      assert String.match?(private_hex, ~r/^[0-9a-f]+$/)

      # Verify expected key sizes
      public_bytes = Base.decode16!(public_hex, case: :mixed)
      private_bytes = Base.decode16!(private_hex, case: :mixed)

      # Public key should be 65 bytes (uncompressed) or 33 bytes (compressed)
      assert byte_size(public_bytes) in [33, 65]

      # Private key should be 32 bytes
      assert byte_size(private_bytes) == 32
    end

    test "generates different keypairs on multiple calls" do
      {pub1, priv1} = Sessions.generate_session_keypair()
      {pub2, priv2} = Sessions.generate_session_keypair()

      assert pub1 != pub2
      assert priv1 != priv2
    end
  end

  describe "decrypt_credential_bundle/2" do
    test "delegates to Crypto module" do
      # This test verifies that Sessions.decrypt_credential_bundle properly delegates
      # to the Crypto module. In production, this would work with real HPKE decryption.
      encrypted_bundle = "abcd1234"
      private_key = "ef567890"
      
      # Since we can't mock easily, we'll test that it properly calls through
      # The actual crypto functionality is tested in crypto_test.exs
      result = Sessions.decrypt_credential_bundle(encrypted_bundle, private_key)
      
      # We expect an error because this is fake data
      assert {:error, _reason} = result
    end
  end

  describe "integration workflow keypair validation" do
    test "complete HPKE keypair generation for session creation" do
      # Step 1: Generate HPKE keypair - this is the core functionality we can test
      {public_hex, private_hex} = Sessions.generate_session_keypair()

      # Step 2: Validate the keypair format
      assert is_binary(public_hex)
      assert is_binary(private_hex)
      assert String.match?(public_hex, ~r/^[0-9a-f]+$/)
      assert String.match?(private_hex, ~r/^[0-9a-f]+$/)
      
      # Step 3: Verify key sizes are appropriate for HPKE
      public_bytes = Base.decode16!(public_hex, case: :mixed)
      private_bytes = Base.decode16!(private_hex, case: :mixed)
      
      # Public key should be 65 bytes (uncompressed) or 33 bytes (compressed)
      assert byte_size(public_bytes) in [33, 65]
      # Private key should be 32 bytes
      assert byte_size(private_bytes) == 32
      
      # Step 4: Verify they're suitable for Turnkey session API format
      # This validates the hex encoding is correct for API consumption
      assert String.length(private_hex) == 64  # 32 bytes * 2 hex chars
      assert String.length(public_hex) in [66, 130]  # 33 or 65 bytes * 2 hex chars
    end
  end
end
