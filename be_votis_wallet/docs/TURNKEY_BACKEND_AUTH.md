# Turnkey Backend Authentication Guide

This guide explains how to integrate Turnkey authentication into your backend following the official Turnkey documentation patterns.

## Overview

Turnkey's backend authentication system uses **JSON Web Tokens (JWTs)** to provide secure, stateless authentication between your frontend and backend. This is different from direct API key authentication and provides several benefits:

- **User Data**: Store and retrieve user data associated with Turnkey sub-organizations
- **Metrics and Monitoring**: Add custom validations, rate limiting, and logging
- **Co-signing Capabilities**: Enable 2/2 signing patterns where your application is a co-signer

## JWT Authentication Flow

### Architecture Overview

```
Frontend (Mobile/Web) → Turnkey Authentication → Session JWT → Your Backend
```

### High-Level Flow

1. **User authenticates** (via passkey, OTP, email, etc.)
2. **Session is created** with Turnkey, returning a session JWT
3. **Session JWT is used** to make authenticated requests to your backend
4. **Backend validates JWT** and enforces access controls

## Authentication Methods & Credential Types

Based on the Turnkey documentation, different authentication methods produce different credential types:

| Credential Type | Authentication Method(s) |
|----------------|-------------------------|
| `CREDENTIAL_TYPE_API_KEY_P256` | API Key (Backend) |
| `CREDENTIAL_TYPE_WEBAUTHN_AUTHENTICATOR` | Passkeys/WebAuthn |
| `CREDENTIAL_TYPE_EMAIL_AUTH` | Email Authentication |
| `CREDENTIAL_TYPE_SMS_AUTH` | SMS/OTP Authentication |
| `CREDENTIAL_TYPE_OAUTH` | OAuth (Google, etc.) |

## Implementation in Our Backend

### 1. Session JWT Creation (Frontend → Turnkey)

The frontend handles authentication with Turnkey and obtains session JWTs:

```elixir
# This happens on the frontend, but we need to handle the resulting JWTs
# Frontend authenticates via:
# - Passkey authentication
# - OTP authentication  
# - Email authentication
# - OAuth (Google)

# Result: Session JWT that proves user identity
```

### 2. Session JWT Validation (Backend)

Our backend needs to validate incoming session JWTs from the frontend:

```elixir
# In your Phoenix controller or plug
def validate_turnkey_session(conn, _opts) do
  case get_req_header(conn, "authorization") do
    ["Bearer " <> jwt] ->
      case TurnkeyAuth.validate_session_jwt(jwt) do
        {:ok, claims} ->
          conn
          |> assign(:current_user_org_id, claims["aud"])
          |> assign(:current_user_id, claims["sub"])
          |> assign(:session_scope, claims["scope"])
        
        {:error, reason} ->
          conn
          |> put_status(401)
          |> json(%{error: "Invalid session", reason: reason})
          |> halt()
      end
    
    _ ->
      conn
      |> put_status(401) 
      |> json(%{error: "Missing authorization header"})
      |> halt()
  end
end
```

### 3. JWT Content Structure

Session JWTs from Turnkey contain important information:

```json
{
  "iss": "https://api.turnkey.com",
  "aud": "your-organization-id", 
  "sub": "session-subject",
  "exp": 1640995200,
  "iat": 1640991600,
  "scope": "read-write"
}
```

## Updated Implementation Plan

### 1. Create JWT Validation Module

We need a new module to handle JWT validation:

```elixir
# lib/be_votis_wallet/services/turnkey/jwt_auth.ex
defmodule BeVotisWallet.Services.Turnkey.JWTAuth do
  @moduledoc """
  Handles Turnkey session JWT validation for backend authentication.
  """
  
  def validate_session_jwt(jwt_token) do
    # 1. Parse JWT header to get key ID
    # 2. Fetch public key from Turnkey JWKS endpoint
    # 3. Verify JWT signature
    # 4. Validate claims (exp, iat, iss, aud)
    # 5. Return validated claims
  end
  
  def get_user_credentials(jwt_claims) do
    # Extract user/organization information from JWT
    # Return structured data for use in controllers
  end
end
```

### 2. Update Current Architecture

Our current implementation has two distinct patterns:

#### Backend-to-Turnkey (Direct API Calls)
- **Current**: API key + cryptographic stamps for direct Turnkey API calls
- **Use Case**: Administrative operations, wallet creation, transaction signing
- **Keep As-Is**: This is correct for server-to-server operations

#### Frontend-to-Backend (User Authentication)
- **New**: JWT-based session validation  
- **Use Case**: User authentication, access control, user data management
- **Implementation Needed**: JWT validation and user session management

### 3. Authentication Flow Implementation

#### Passkey Authentication Flow
```elixir
# Frontend handles this with Turnkey:
# 1. User initiates passkey authentication
# 2. WebAuthn/FIDO2 challenge/response
# 3. Turnkey validates and returns session JWT
# 4. Frontend sends JWT to our backend

# Backend validation:
def handle_passkey_session(conn, %{"jwt" => jwt}) do
  case JWTAuth.validate_session_jwt(jwt) do
    {:ok, %{"credential_type" => "CREDENTIAL_TYPE_WEBAUTHN_AUTHENTICATOR"} = claims} ->
      # Handle passkey-authenticated user
      create_user_session(conn, claims)
    
    {:error, reason} ->
      handle_auth_error(conn, reason)
  end
end
```

#### OTP Authentication Flow
```elixir
# Similar pattern for SMS/Email OTP
def handle_otp_session(conn, %{"jwt" => jwt}) do
  case JWTAuth.validate_session_jwt(jwt) do
    {:ok, %{"credential_type" => "CREDENTIAL_TYPE_SMS_AUTH"} = claims} ->
      create_user_session(conn, claims)
    
    {:ok, %{"credential_type" => "CREDENTIAL_TYPE_EMAIL_AUTH"} = claims} ->
      create_user_session(conn, claims)
      
    {:error, reason} ->
      handle_auth_error(conn, reason)
  end
end
```

## Configuration Updates

### Environment Variables

Our current configuration is good for direct API operations, but we need to add JWT validation:

```bash
# Existing - for direct Turnkey API calls
export TURNKEY_API_KEY="your-api-key-id"
export TURNKEY_PRIVATE_KEY_PATH="/path/to/private.pem"
export TURNKEY_ORG_ID="your-org-id"

# New - for JWT validation
export TURNKEY_JWKS_URL="https://api.turnkey.com/.well-known/jwks.json"
export TURNKEY_JWT_ISSUER="https://api.turnkey.com"
export TURNKEY_JWT_AUDIENCE="your-organization-id"
```

### Runtime Configuration

```elixir
# config/runtime.exs
config :be_votis_wallet, :turnkey_jwt,
  jwks_url: System.get_env("TURNKEY_JWKS_URL") || "https://api.turnkey.com/.well-known/jwks.json",
  issuer: System.get_env("TURNKEY_JWT_ISSUER") || "https://api.turnkey.com", 
  audience: System.get_env("TURNKEY_JWT_AUDIENCE") || System.get_env("TURNKEY_ORG_ID"),
  # Cache JWKS keys for this duration
  jwks_cache_ttl: :timer.minutes(30)
```

## API Usage Patterns

### 1. User-Initiated Operations (JWT-Based)

When users make requests through your API:

```elixir
# Phoenix Controller
def create_wallet(conn, wallet_params) do
  # JWT validation happens in plug
  user_org_id = conn.assigns.current_user_org_id
  
  # Now use direct Turnkey API with server credentials
  case Activities.create_wallet(user_org_id, wallet_params) do
    {:ok, wallet} -> 
      # Store user data, return response
      json(conn, wallet)
    {:error, reason} ->
      handle_error(conn, reason)
  end
end
```

### 2. Direct Turnkey Operations (API Key-Based)

For server-to-server operations:

```elixir
# Administrative operations
def create_user_organization(user_email) do
  # Uses our existing API key + stamp authentication
  Activities.create_sub_organization("User: #{user_email}")
end
```

## Migration Strategy

### Phase 1: Add JWT Validation (Non-Breaking)
1. Implement `JWTAuth` module
2. Add JWT validation plugs
3. Update configuration
4. Test with frontend integration

### Phase 2: Update Controllers
1. Add JWT validation to user-facing endpoints
2. Maintain existing direct API functionality
3. Add user session management

### Phase 3: Frontend Integration
1. Update mobile/web clients to use Turnkey session JWTs
2. Remove direct API key usage from frontend
3. Test end-to-end authentication flows

## Security Considerations

### JWT Validation
- **Signature Verification**: Always verify JWT signatures using Turnkey's public keys
- **Claims Validation**: Validate `iss`, `aud`, `exp`, `iat` claims
- **Key Rotation**: Handle JWKS key rotation gracefully
- **Error Handling**: Don't leak sensitive information in error responses

### Session Management
- **Short Expiration**: Honor JWT expiration times
- **Scope Enforcement**: Respect session scopes (read-only vs read-write)
- **Credential Types**: Handle different credential types appropriately
- **Rate Limiting**: Implement rate limiting for JWT validation

## Testing

### Unit Tests
```elixir
# Test JWT validation
test "validates valid Turnkey session JWT" do
  jwt = create_test_jwt()
  assert {:ok, claims} = JWTAuth.validate_session_jwt(jwt)
  assert claims["iss"] == "https://api.turnkey.com"
end

# Test different credential types
test "handles passkey credential type" do
  jwt = create_test_jwt(credential_type: "CREDENTIAL_TYPE_WEBAUTHN_AUTHENTICATOR")
  assert {:ok, claims} = JWTAuth.validate_session_jwt(jwt) 
  assert claims["credential_type"] == "CREDENTIAL_TYPE_WEBAUTHN_AUTHENTICATOR"
end
```

### Integration Tests
- Test complete authentication flow
- Test JWT expiration handling
- Test invalid JWT scenarios
- Test different authentication methods

## Next Steps

1. **Implement JWT validation module**
2. **Add authentication plugs to Phoenix**
3. **Update frontend to use session JWTs**
4. **Test with different authentication methods**
5. **Add comprehensive error handling**
6. **Implement session caching if needed**

This approach maintains our existing direct API functionality while adding the JWT-based authentication layer that Turnkey recommends for backend integration.
