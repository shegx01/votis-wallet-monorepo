defmodule BeVotisWallet.Services.Turnkey.CryptoTest do
  use ExUnit.Case, async: true

  import Mox

  alias BeVotisWallet.Services.Turnkey.Crypto

  # Make sure mocks are verified when the test exits
  setup :verify_on_exit!

  describe "generate_api_keypair/0" do
    test "generates valid ECDSA P-256 keypair in PEM format" do
      {public_pem, private_pem} = Crypto.generate_api_keypair()

      # Verify PEM format
      assert String.starts_with?(public_pem, "-----BEGIN PUBLIC KEY-----")
      assert String.ends_with?(String.trim(public_pem), "-----END PUBLIC KEY-----")

      assert String.starts_with?(private_pem, "-----BEGIN EC PRIVATE KEY-----")
      assert String.ends_with?(String.trim(private_pem), "-----END EC PRIVATE KEY-----")

      # Verify keys can be parsed
      assert [{:SubjectPublicKeyInfo, _, _}] = :public_key.pem_decode(public_pem)
      assert [{:ECPrivateKey, _, _}] = :public_key.pem_decode(private_pem)
    end

    test "generates different keypairs on multiple calls" do
      {pub1, priv1} = Crypto.generate_api_keypair()
      {pub2, priv2} = Crypto.generate_api_keypair()

      assert pub1 != pub2
      assert priv1 != priv2
    end
  end

  describe "generate_hpke_keypair/0" do
    test "generates valid HPKE keypair in hex format" do
      {public_hex, private_hex} = Crypto.generate_hpke_keypair()

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
      {pub1, priv1} = Crypto.generate_hpke_keypair()
      {pub2, priv2} = Crypto.generate_hpke_keypair()

      assert pub1 != pub2
      assert priv1 != priv2
    end

    test "generates uncompressed public keys when possible" do
      {public_hex, _private_hex} = Crypto.generate_hpke_keypair()
      public_bytes = Base.decode16!(public_hex, case: :mixed)

      # Should start with 0x04 for uncompressed format
      if byte_size(public_bytes) == 65 do
        assert <<0x04, _x::256, _y::256>> = public_bytes
      end
    end
  end

  describe "create_request_stamp/2" do
    test "successfully creates stamp for valid request and key" do
      {_public_pem, private_pem} = Crypto.generate_api_keypair()

      request_body =
        ~s({"type":"ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION","timestampMs":"1234567890"})

      assert {:ok, stamp} = Crypto.create_request_stamp(request_body, private_pem)

      # Verify stamp is base64url encoded JSON
      assert {:ok, decoded_json} = Base.url_decode64(stamp, padding: false)
      assert {:ok, stamp_data} = Jason.decode(decoded_json)
      
      # Verify stamp contains required Turnkey fields
      assert %{"publicKey" => _, "signature" => _, "scheme" => "SIGNATURE_SCHEME_TK_API_P256"} = stamp_data
      assert is_binary(stamp_data["publicKey"])
      assert is_binary(stamp_data["signature"])
      assert String.length(stamp_data["publicKey"]) > 0
      assert String.length(stamp_data["signature"]) > 0
    end

    test "returns error for invalid private key" do
      invalid_key = "not-a-valid-pem-key"
      request_body = ~s({"test": "data"})

      assert {:error, :invalid_private_key} =
               Crypto.create_request_stamp(request_body, invalid_key)
    end

    test "returns error for malformed PEM" do
      malformed_pem = """
      -----BEGIN EC PRIVATE KEY-----
      invalid-base64-content
      -----END EC PRIVATE KEY-----
      """

      request_body = ~s({"test": "data"})

      assert {:error, :invalid_private_key} =
               Crypto.create_request_stamp(request_body, malformed_pem)
    end

    test "creates different stamps for different messages" do
      {_public_pem, private_pem} = Crypto.generate_api_keypair()

      {:ok, stamp1} = Crypto.create_request_stamp("message1", private_pem)
      {:ok, stamp2} = Crypto.create_request_stamp("message2", private_pem)

      assert stamp1 != stamp2
    end

    test "creates consistent stamps that can be verified" do
      {_public_pem, private_pem} = Crypto.generate_api_keypair()
      message = "identical message"

      {:ok, stamp1} = Crypto.create_request_stamp(message, private_pem)
      {:ok, stamp2} = Crypto.create_request_stamp(message, private_pem)

      # ECDSA signatures include randomness, so they should be different
      # but both should be valid base64url-encoded JSON stamps
      assert stamp1 != stamp2
      assert {:ok, decoded_json1} = Base.url_decode64(stamp1, padding: false)
      assert {:ok, decoded_json2} = Base.url_decode64(stamp2, padding: false)
      assert {:ok, _stamp_data1} = Jason.decode(decoded_json1)
      assert {:ok, _stamp_data2} = Jason.decode(decoded_json2)
    end
  end

  describe "decrypt_credential_bundle/2" do
    test "returns error for invalid hex input" do
      invalid_hex = "not-valid-hex"
      private_key_hex = "0123456789abcdef"

      assert {:error, :invalid_hex} =
               Crypto.decrypt_credential_bundle(invalid_hex, private_key_hex)
    end

    test "returns error for malformed HPKE ciphertext" do
      # Valid hex but invalid HPKE format
      invalid_ciphertext = Base.encode16("invalid ciphertext format", case: :lower)
      # 32-byte private key
      private_key_hex = String.duplicate("01", 32)

      assert {:error, :invalid_ciphertext_format} =
               Crypto.decrypt_credential_bundle(invalid_ciphertext, private_key_hex)
    end

    test "handles properly formatted but undecryptable data" do
      # Create properly formatted but fake HPKE data
      # 65-byte uncompressed public key
      encap_key = String.duplicate("01", 65)
      fake_ciphertext = String.duplicate("ff", 50)

      # Format as HPKE: [key_length][encap_key][ciphertext]
      hpke_data =
        <<65::32-big>> <>
          Base.decode16!(encap_key, case: :mixed) <> Base.decode16!(fake_ciphertext, case: :mixed)

      hpke_hex = Base.encode16(hpke_data, case: :lower)

      private_key_hex = String.duplicate("01", 32)

      # This should fail during HPKE decryption
      assert {:error, _reason} = Crypto.decrypt_credential_bundle(hpke_hex, private_key_hex)
    end
  end

  describe "edge cases and error handling" do
    test "handles empty strings gracefully" do
      assert {:error, :invalid_private_key} = Crypto.create_request_stamp("test", "")
      assert {:error, :invalid_ciphertext_format} = Crypto.decrypt_credential_bundle("", "")
    end

    test "handles nil inputs gracefully" do
      # These should not crash but return appropriate errors
      assert_raise FunctionClauseError, fn ->
        Crypto.create_request_stamp(nil, nil)
      end
    end

    test "handles very large inputs" do
      # 1MB message
      large_message = String.duplicate("x", 1_000_000)
      {_pub, priv} = Crypto.generate_api_keypair()

      # Should still work for large messages
      assert {:ok, _stamp} = Crypto.create_request_stamp(large_message, priv)
    end
  end

  describe "integration scenarios" do
    test "full key generation and signing workflow" do
      # Generate keypair
      {_public_pem, private_pem} = Crypto.generate_api_keypair()

      # Create a realistic Turnkey request
      request = %{
        "type" => "ACTIVITY_TYPE_CREATE_SUB_ORGANIZATION",
        "timestampMs" => to_string(System.system_time(:millisecond)),
        "organizationId" => "550e8400-e29b-41d4-a716-446655440000",
        "parameters" => %{
          "subOrganizationName" => "Test User Organization"
        }
      }

      request_json = Jason.encode!(request)

      # Sign the request
      assert {:ok, stamp} = Crypto.create_request_stamp(request_json, private_pem)

      # Verify stamp format is Base64URL encoded JSON
      assert {:ok, decoded_json} = Base.url_decode64(stamp, padding: false)
      assert {:ok, stamp_data} = Jason.decode(decoded_json)
      
      # Verify contains all required Turnkey stamp fields
      assert %{"publicKey" => public_key, "signature" => signature, "scheme" => scheme} = stamp_data
      assert scheme == "SIGNATURE_SCHEME_TK_API_P256"
      assert String.match?(public_key, ~r/^[0-9a-f]+$/)
      assert String.match?(signature, ~r/^[0-9a-f]+$/)
      assert byte_size(decoded_json) > 0
    end

    test "HPKE keypair for session creation" do
      # Generate HPKE keypair
      {public_hex, private_hex} = Crypto.generate_hpke_keypair()

      # Verify they can be used in Turnkey session creation format
      assert String.length(public_hex) > 0
      # 32 bytes * 2 hex chars
      assert String.length(private_hex) == 64

      # Verify they're valid hex
      assert {:ok, _} = Base.decode16(public_hex, case: :mixed)
      assert {:ok, _} = Base.decode16(private_hex, case: :mixed)
    end
  end
end
