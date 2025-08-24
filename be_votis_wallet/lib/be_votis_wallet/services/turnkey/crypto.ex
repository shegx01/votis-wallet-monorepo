defmodule BeVotisWallet.Services.Turnkey.Crypto do
  @moduledoc """
  Production cryptographic utilities for Turnkey integration.
  """

  require Logger

  @type keypair :: {public_key :: binary(), private_key :: binary()}
  @type stamp_result :: {:ok, binary()} | {:error, term()}


  ## ECDSA Key Generation and Signing

  @doc """
  Generate ECDSA P-256 keypair for Turnkey API authentication.
  Returns {public_key_pem, private_key_pem}.
  """
  @spec generate_api_keypair() :: keypair()
  def generate_api_keypair do
    # Generate P-256 ECDSA key using the correct function that returns proper structure
    ec_private_key = :public_key.generate_key({:namedCurve, :secp256r1})

    # Encode to DER first, then to PEM
    private_der = :public_key.der_encode(:ECPrivateKey, ec_private_key)
    private_pem = :public_key.pem_encode([{:ECPrivateKey, private_der, :not_encrypted}])

    # Extract public key from the private key structure
    {:ECPrivateKey, _, _, _, public_key_point, _} = ec_private_key

    # Create SubjectPublicKeyInfo structure for public key
    public_key_info = {
      :SubjectPublicKeyInfo,
      {:AlgorithmIdentifier, {1, 2, 840, 10045, 2, 1},
       {:namedCurve, {1, 2, 840, 10045, 3, 1, 7}}},
      public_key_point
    }

    # Encode public key to DER first, then to PEM
    public_der = :public_key.der_encode(:SubjectPublicKeyInfo, public_key_info)
    public_pem = :public_key.pem_encode([{:SubjectPublicKeyInfo, public_der, :not_encrypted}])

    {public_pem, private_pem}
  end

  @doc """
  Generate HPKE P-256 keypair for session encryption.
  Returns {public_key_hex, private_key_hex} formatted for Turnkey.
  """
  @spec generate_hpke_keypair() :: keypair()
  def generate_hpke_keypair do
    # Generate raw P-256 point for HPKE
    {public_point, private_scalar} = :crypto.generate_key(:ecdh, :secp256r1)

    # Ensure uncompressed format (0x04 + x + y coordinates)
    public_uncompressed = ensure_uncompressed_point(public_point)

    # Encode as hex strings for Turnkey API
    public_hex = Base.encode16(public_uncompressed, case: :lower)
    private_hex = Base.encode16(private_scalar, case: :lower)

    {public_hex, private_hex}
  end

  @doc """
  Create Turnkey API stamp for request authentication according to official specification.

  ## Parameters
  - request_body: JSON request body as string
  - private_key_pem: PEM-encoded ECDSA private key

  ## Returns
  - {:ok, stamp} - Base64URL-encoded JSON stamp
  - {:error, reason} - Error details

  ## Stamp Format (per Turnkey docs)
  Creates a JSON stamp with:
  - publicKey: the public key of API key (P-256 only)
  - signature: hex-encoded DER signature
  - scheme: "SIGNATURE_SCHEME_TK_API_P256"
  
  Then Base64URL encodes the JSON for the X-Stamp header.
  """
  @spec create_request_stamp(binary(), binary()) :: stamp_result()
  def create_request_stamp(request_body, private_key_pem) when is_binary(request_body) do
    with {:ok, private_key} <- parse_private_key(private_key_pem),
         {:ok, public_key_pem} <- extract_public_key_from_private(private_key_pem),
         {:ok, signature} <- sign_message(request_body, private_key),
         {:ok, stamp_json} <- build_api_key_stamp(public_key_pem, signature) do
      # Base64URL encode the JSON stamp as per Turnkey spec
      stamp = Base.url_encode64(stamp_json, padding: false)
      {:ok, stamp}
    else
      error -> error
    end
  end

  @doc """
  Decrypt HPKE credential bundle from Turnkey read-write session.
  """
  @spec decrypt_credential_bundle(binary(), binary()) :: {:ok, map()} | {:error, term()}
  def decrypt_credential_bundle(encrypted_bundle_hex, private_key_hex) do
    with {:ok, encrypted_data} <- decode_hex_safe(encrypted_bundle_hex),
         {:ok, private_key} <- decode_hex_safe(private_key_hex),
         {:ok, {encap_key, ciphertext}} <- parse_hpke_ciphertext(encrypted_data),
         {:ok, plaintext} <- decrypt_hpke_payload(encap_key, ciphertext, private_key),
         {:ok, credentials} <- decode_json_safe(plaintext) do
      {:ok, credentials}
    else
      error -> error
    end
  end

  ## Private Implementation Functions

  defp ensure_uncompressed_point(<<0x04, _x::256, _y::256>> = point), do: point

  defp ensure_uncompressed_point(compressed_point) when byte_size(compressed_point) == 33 do
    try do
      %Curvy.Key{point: %Curvy.Point{x: x, y: y}} = Curvy.Key.from_pubkey(compressed_point)
      x_bin = :binary.encode_unsigned(x) |> String.pad_leading(32, <<0>>)
      y_bin = :binary.encode_unsigned(y) |> String.pad_leading(32, <<0>>)
      <<0x04, x_bin::binary, y_bin::binary>>
    rescue
      error ->
        Logger.error("Point decompression failed", error: inspect(error))
        {:error, :point_decompression_failed}
    end
  end

  defp ensure_uncompressed_point(invalid_point) do
    Logger.error("Invalid point format", point_size: byte_size(invalid_point))
    {:error, :invalid_point_format}
  end

  defp parse_private_key(private_key_pem) do
    try do
      [{:ECPrivateKey, key_der, _}] = :public_key.pem_decode(private_key_pem)

      {:ECPrivateKey, _version, raw_private_key, _params, _public_key, _attrs} =
        :public_key.der_decode(:ECPrivateKey, key_der)

      {:ok, raw_private_key}
    rescue
      error ->
        Logger.error("Failed to parse private key", error: inspect(error))
        {:error, :invalid_private_key}
    end
  end

  defp sign_message(message, raw_private_key) do
    try do
      message_hash = :crypto.hash(:sha256, message)
      signature = :crypto.sign(:ecdsa, :sha256, message_hash, [raw_private_key, :secp256r1])
      {:ok, signature}
    rescue
      error ->
        Logger.error("Failed to sign message", error: inspect(error))
        {:error, :signing_failed}
    end
  end

  defp decode_hex_safe(hex_string) do
    try do
      decoded = Base.decode16!(hex_string, case: :mixed)
      {:ok, decoded}
    rescue
      error ->
        Logger.error("Hex decode failed", error: inspect(error))
        {:error, :invalid_hex}
    end
  end

  defp decode_json_safe(json_string) do
    try do
      decoded = Jason.decode!(json_string)
      {:ok, decoded}
    rescue
      error ->
        Logger.error("JSON decode failed", error: inspect(error))
        {:error, :invalid_json}
    end
  end

  defp parse_hpke_ciphertext(<<encap_key_len::32-big, data::binary>>)
       when byte_size(data) >= encap_key_len do
    <<encap_key::binary-size(encap_key_len), ciphertext::binary>> = data
    {:ok, {encap_key, ciphertext}}
  end

  defp parse_hpke_ciphertext(_data) do
    Logger.error("Invalid HPKE ciphertext format")
    {:error, :invalid_ciphertext_format}
  end

  defp decrypt_hpke_payload(encap_key, ciphertext, private_key) do
    try do
      shared_secret = :crypto.compute_key(:ecdh, encap_key, private_key, :secp256r1)
      aes_key = HKDF.expand(:sha256, shared_secret, 32, "turnkey-hpke-v1")
      decrypt_aes_gcm(ciphertext, aes_key)
    rescue
      error ->
        Logger.error("HPKE decryption failed", error: inspect(error))
        {:error, :hpke_decryption_failed}
    end
  end

  defp decrypt_aes_gcm(<<iv::binary-size(12), tag::binary-size(16), ciphertext::binary>>, key) do
    case :crypto.crypto_one_time_aead(:aes_256_gcm, key, iv, ciphertext, <<>>, tag, false) do
      plaintext when is_binary(plaintext) -> {:ok, plaintext}
      error -> {:error, {:aes_decrypt_failed, error}}
    end
  end

  defp decrypt_aes_gcm(invalid_format, _key) do
    Logger.error("Invalid AES-GCM ciphertext format", size: byte_size(invalid_format))
    {:error, :invalid_aes_format}
  end

  defp extract_public_key_from_private(private_key_pem) do
    try do
      [{:ECPrivateKey, key_der, _}] = :public_key.pem_decode(private_key_pem)
      {:ECPrivateKey, _version, _raw_private_key, _params, public_key_point, _attrs} =
        :public_key.der_decode(:ECPrivateKey, key_der)

      # Create SubjectPublicKeyInfo structure for public key
      public_key_info = {
        :SubjectPublicKeyInfo,
        {:AlgorithmIdentifier, {1, 2, 840, 10045, 2, 1},
         {:namedCurve, {1, 2, 840, 10045, 3, 1, 7}}},
        public_key_point
      }

      # Encode public key to DER first, then to PEM
      public_der = :public_key.der_encode(:SubjectPublicKeyInfo, public_key_info)
      public_pem = :public_key.pem_encode([{:SubjectPublicKeyInfo, public_der, :not_encrypted}])

      {:ok, public_pem}
    rescue
      error ->
        Logger.error("Failed to extract public key from private key", error: inspect(error))
        {:error, :public_key_extraction_failed}
    end
  end

  defp build_api_key_stamp(public_key_pem, signature) do
    try do
      # Extract the raw public key point from PEM for hex encoding
      [{:SubjectPublicKeyInfo, public_der, _}] = :public_key.pem_decode(public_key_pem)
      {:SubjectPublicKeyInfo, _alg_id, public_key_point} =
        :public_key.der_decode(:SubjectPublicKeyInfo, public_der)

      # Ensure uncompressed format and convert to hex
      public_key_uncompressed = ensure_uncompressed_point(public_key_point)
      public_key_hex = case public_key_uncompressed do
        {:error, _} = error -> throw(error)
        point -> Base.encode16(point, case: :lower)
      end

      # Convert DER signature to hex
      signature_hex = Base.encode16(signature, case: :lower)

      # Create the stamp JSON according to Turnkey spec
      stamp_map = %{
        "publicKey" => public_key_hex,
        "signature" => signature_hex,
        "scheme" => "SIGNATURE_SCHEME_TK_API_P256"
      }

      # Encode to JSON string
      stamp_json = Jason.encode!(stamp_map)
      {:ok, stamp_json}
    rescue
      error ->
        Logger.error("Failed to build API key stamp", error: inspect(error))
        {:error, :stamp_build_failed}
    catch
      {:error, reason} ->
        {:error, reason}
    end
  end
end
