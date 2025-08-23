defmodule BeVotisWallet.Services.Turnkey.Sessions do
  @moduledoc """
  Turnkey session management for read-only and read-write sessions.

  This module provides high-level functions to create and manage Turnkey sessions,
  including credential bundle decryption and session-based authentication.
  """

  require Logger

  alias BeVotisWallet.Services.Turnkey.Activities
  alias BeVotisWallet.Services.Turnkey.Crypto

  @type session_type :: :read_only | :read_write
  @type organization_id :: String.t()
  @type user_id :: String.t() | nil
  @type session_result :: {:ok, map()} | {:error, term()}
  @type credentials :: %{
          api_key_id: String.t(),
          api_key: String.t(),
          private_key: String.t(),
          organization_id: String.t(),
          user_id: String.t()
        }

  @doc """
  Create a read-only session and return session information.

  Read-only sessions allow querying Turnkey resources but cannot perform
  state-changing operations.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `user_id` - Optional user UUID (if not provided, uses authenticated user)
  - `opts` - Additional options passed to the activity

  ## Returns
  - `{:ok, session_data}` - Success with session information
  - `{:error, reason}` - Failure response

  ## Example
      {:ok, session} = Sessions.create_read_only_session("org-123")
      # session contains read-only session token and metadata
  """
  @spec create_read_only_session(organization_id(), user_id(), keyword()) :: session_result()
  def create_read_only_session(organization_id, user_id \\ nil, opts \\ []) do
    case Activities.create_read_only_session(organization_id, user_id, opts) do
      {:ok, response} ->
        extract_read_only_session_data(response)

      {:error, status_code, error_message} ->
        Logger.error("Failed to create read-only session",
          organization_id: organization_id,
          user_id: user_id,
          status: status_code,
          error: error_message
        )

        {:error, {status_code, error_message}}
    end
  end

  @doc """
  Create a read-write session with credential bundle decryption.

  Read-write sessions return an encrypted credential bundle that contains
  the actual API key and private key needed for authenticated requests.

  ## Parameters
  - `organization_id` - Target organization UUID
  - `user_id` - Optional user UUID (if not provided, uses authenticated user)
  - `opts` - Additional options:
    - `:api_key_name` - Optional name for the API key
    - `:expiration_seconds` - Session expiration time (default: 900)
    - `:invalidate_existing` - Whether to invalidate other sessions
    - Additional options passed to the activity

  ## Returns
  - `{:ok, %{credentials: credentials, metadata: metadata}}` - Success with decrypted credentials
  - `{:error, reason}` - Failure response

  ## Example
      # Generate HPKE keypair for session encryption
      {public_hex, private_hex} = Crypto.generate_hpke_keypair()
      
      {:ok, session} = Sessions.create_read_write_session("org-123", nil, 
        target_public_key: public_hex,
        hpke_private_key: private_hex
      )
      
      # Use session.credentials for authenticated API requests
  """
  @spec create_read_write_session(organization_id(), user_id(), keyword()) :: session_result()
  def create_read_write_session(organization_id, user_id \\ nil, opts \\ []) do
    # Extract our internal options
    target_public_key = Keyword.fetch!(opts, :target_public_key)
    hpke_private_key = Keyword.fetch!(opts, :hpke_private_key)

    # Remove our internal options before passing to activities
    activity_opts = Keyword.drop(opts, [:target_public_key, :hpke_private_key])

    case Activities.create_read_write_session(
           organization_id,
           target_public_key,
           user_id,
           activity_opts
         ) do
      {:ok, response} ->
        extract_and_decrypt_credentials(response, hpke_private_key)

      {:error, status_code, error_message} ->
        Logger.error("Failed to create read-write session",
          organization_id: organization_id,
          user_id: user_id,
          status: status_code,
          error: error_message
        )

        {:error, {status_code, error_message}}
    end
  end

  @doc """
  Generate HPKE keypair for session creation.

  This is a convenience function that generates the keypair needed for
  read-write session creation.

  ## Returns
  - `{public_key_hex, private_key_hex}` - HPKE keypair in hex format
  """
  @spec generate_session_keypair() :: {String.t(), String.t()}
  def generate_session_keypair do
    Crypto.generate_hpke_keypair()
  end

  @doc """
  Manually decrypt a credential bundle.

  This can be used if you have a credential bundle and need to decrypt it
  outside of the normal session creation flow.

  ## Parameters
  - `encrypted_bundle_hex` - Hex-encoded encrypted credential bundle
  - `private_key_hex` - Hex-encoded HPKE private key

  ## Returns
  - `{:ok, credentials}` - Decrypted credential data
  - `{:error, reason}` - Decryption failure
  """
  @spec decrypt_credential_bundle(String.t(), String.t()) :: {:ok, map()} | {:error, term()}
  def decrypt_credential_bundle(encrypted_bundle_hex, private_key_hex) do
    Crypto.decrypt_credential_bundle(encrypted_bundle_hex, private_key_hex)
  end

  ## Private helper functions

  defp extract_read_only_session_data(response) do
    case get_in(response, ["activity", "result", "createReadOnlySessionResult"]) do
      nil ->
        Logger.error("Invalid read-only session response format", response: inspect(response))
        {:error, :invalid_response_format}

      session_data ->
        {:ok, session_data}
    end
  end

  defp extract_and_decrypt_credentials(response, hpke_private_key) do
    with {:ok, result} <- extract_read_write_session_result(response),
         {:ok, encrypted_bundle} <- extract_credential_bundle(result),
         {:ok, credentials} <-
           Crypto.decrypt_credential_bundle(encrypted_bundle, hpke_private_key) do
      # Structure the response with both decrypted credentials and metadata
      session_data = %{
        credentials: normalize_credentials(credentials),
        metadata: %{
          api_key_id: result["apiKeyId"],
          created_at: System.system_time(:second)
        }
      }

      {:ok, session_data}
    else
      error -> error
    end
  end

  defp extract_read_write_session_result(response) do
    case get_in(response, ["activity", "result", "createReadWriteSessionResultV2"]) do
      nil ->
        Logger.error("Invalid read-write session response format", response: inspect(response))
        {:error, :invalid_response_format}

      result ->
        {:ok, result}
    end
  end

  defp extract_credential_bundle(result) do
    case Map.get(result, "credentialBundle") do
      nil ->
        Logger.error("No credential bundle in response", result: inspect(result))
        {:error, :missing_credential_bundle}

      bundle when is_binary(bundle) ->
        {:ok, bundle}

      invalid ->
        Logger.error("Invalid credential bundle format", bundle: inspect(invalid))
        {:error, :invalid_credential_bundle}
    end
  end

  defp normalize_credentials(raw_credentials) do
    %{
      api_key_id: raw_credentials["apiKeyId"],
      api_key: raw_credentials["apiKey"],
      private_key: raw_credentials["privateKey"],
      organization_id: raw_credentials["organizationId"],
      user_id: raw_credentials["userId"]
    }
  end
end
