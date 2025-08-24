# Turnkey API Setup and Request Signing

This document explains how to set up Turnkey API authentication and the request signing process using API stamps.

## Overview

The Votis Wallet application integrates with Turnkey using API key authentication with cryptographic request signing. This follows Turnkey's security model where:

1. **API Key Setup**: You generate an ECDSA P-256 keypair and register the public key with Turnkey
2. **Request Signing**: Each API request is signed using the private key to create a "stamp"
3. **Stamp Format**: Stamps are JSON objects containing the public key, signature, and scheme, Base64URL-encoded

## API Key Setup Process

### 1. Generate ECDSA P-256 Keypair

Use the crypto module to generate a keypair:

```elixir
# In IEx or a setup script
alias BeVotisWallet.Services.Turnkey.Crypto

# Generate the keypair
{public_pem, private_pem} = Crypto.generate_api_keypair()

# Save the private key securely (never commit this to version control!)
File.write!("/secure/path/turnkey_private.pem", private_pem, [:write])
File.chmod!("/secure/path/turnkey_private.pem", 0o600)  # Read-only for owner

# Register the public key with Turnkey
IO.puts("Register this public key with Turnkey:")
IO.puts(public_pem)
```

### 2. Register Public Key with Turnkey

1. Log into the Turnkey dashboard
2. Navigate to your organization's API keys section
3. Create a new API key
4. Paste the public key PEM content from step 1
5. Copy the generated API key ID for configuration

### 3. Configure Environment Variables

Set these environment variables in your production environment:

```bash
# Required for all environments
export TURNKEY_BASE_URL="https://api.turnkey.com"
export TURNKEY_API_KEY="your-api-key-id-from-turnkey"
export TURNKEY_ORG_ID="your-organization-id"

# Private key configuration (choose one method)
# Method 1: Direct PEM content (not recommended for production)
export TURNKEY_PRIVATE_KEY_PEM="-----BEGIN EC PRIVATE KEY-----\n..."

# Method 2: File path (recommended for production)
export TURNKEY_PRIVATE_KEY_PATH="/secure/path/turnkey_private.pem"
```

**Security Note**: Never include the private key in your codebase or commit it to version control. Use secure secret management systems in production.

## Request Signing Implementation

### Stamp Format

Our implementation creates Turnkey stamps according to the official specification:

```json
{
  "publicKey": "04a1b2c3d4...", // Hex-encoded uncompressed P-256 public key
  "signature": "3045022100...", // Hex-encoded DER signature
  "scheme": "SIGNATURE_SCHEME_TK_API_P256"
}
```

This JSON is then Base64URL-encoded (without padding) and sent in the `X-Stamp` header.

### Signing Process

1. **Hash the request body** using SHA-256
2. **Sign the hash** using ECDSA P-256 with the configured private key  
3. **Extract the public key** from the private key
4. **Build the stamp JSON** with public key, signature, and scheme
5. **Base64URL encode** the JSON for the header

### Usage in Application

The signing process is automatic when a private key is configured:

```elixir
# This will automatically sign the request if api_private_key is configured
Activities.create_sub_organization("My Organization")

# For WebAuthn/Passkey authentication, provide client signature
Activities.create_wallet(
  org_id, user_id, wallet_name, accounts,
  auth_type: :webauthn,
  client_signature: client_generated_stamp
)
```

### Headers Used

- `X-Stamp`: For server-side API key authentication
- `X-Stamp-WebAuthn`: For client-side WebAuthn/Passkey authentication

## Development vs Production

### Development Environment

In development, private key configuration is optional. If not configured, requests will be sent unsigned (which may fail against the real Turnkey API but works for testing with mocks).

```bash
# Optional in development
export TURNKEY_PRIVATE_KEY_PATH="/path/to/dev/key.pem"
```

### Production Environment

In production, the private key is required. The application will fail to start if neither `TURNKEY_PRIVATE_KEY_PEM` nor `TURNKEY_PRIVATE_KEY_PATH` is provided.

## Testing

The test suite includes comprehensive tests for:

- ECDSA keypair generation
- Request stamp creation and validation
- Base64URL encoding compliance
- JSON stamp structure validation
- Integration with Activities API

All tests use generated keypairs and don't require actual Turnkey credentials.

## Security Best Practices

1. **Key Storage**: Store private keys in secure, encrypted storage systems
2. **Access Control**: Limit file permissions to 600 (owner read/write only)
3. **Key Rotation**: Regularly rotate API keys according to security policy
4. **Environment Separation**: Use different keys for different environments
5. **Monitoring**: Log authentication failures and unusual activity
6. **Backup**: Securely backup private keys with proper access controls

## Troubleshooting

### "No API private key configured" Warning

This warning appears when the application can't find the private key configuration. Check:

1. Environment variables are set correctly
2. File path is accessible and readable
3. PEM format is valid (starts with `-----BEGIN EC PRIVATE KEY-----`)

### Request Signing Failures

If stamp creation fails:

1. Verify the private key is valid ECDSA P-256 format
2. Check file permissions if using file path method
3. Ensure the key matches the public key registered with Turnkey

### Base64URL Encoding Issues

Our implementation uses Base64URL encoding without padding as per Turnkey specification. This differs from standard Base64:

- Uses `-` and `_` instead of `+` and `/`  
- Omits padding `=` characters
- Required for Turnkey API compatibility

## API Reference

See the module documentation for detailed API references:

- `BeVotisWallet.Services.Turnkey.Crypto` - Cryptographic operations
- `BeVotisWallet.Services.Turnkey.Activities` - Turnkey activity operations
- Configuration in `config/runtime.exs` - Runtime configuration setup
