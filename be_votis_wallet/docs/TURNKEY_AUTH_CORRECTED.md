# Turnkey Authentication Architecture - Corrected

After reviewing the official Turnkey documentation, here's the correct understanding of Turnkey's authentication system and how it applies to our implementation.

## Overview

Turnkey has multiple authentication patterns that serve different purposes:

### 1. **API Key Authentication** (What we currently implement)
- **Purpose**: Server-to-server authentication for backend operations
- **Method**: ECDSA P-256 signatures on request bodies ("stamps")
- **Usage**: Our backend authenticating to Turnkey API directly
- **Headers**: `X-Stamp` with Base64URL-encoded JSON stamp

### 2. **Session JWTs** (From Turnkey docs)
- **Purpose**: Frontend-to-backend authentication in consumer apps
- **Method**: JWTs issued by Turnkey after user authentication
- **Usage**: Mobile/web clients sending JWTs to their own backends
- **Headers**: Standard `Authorization: Bearer <jwt>` 

### 3. **Credential Bundles** (What our Sessions module uses)
- **Purpose**: Encrypted API credentials for client-side operations
- **Method**: HPKE-encrypted bundles containing temporary API keys
- **Usage**: Mobile clients decrypt and use for direct Turnkey API calls
- **Security**: True non-custodial - backend never sees decrypted credentials

## Our Current Implementation (Correct!)

Our current implementation follows **Pattern #3** - the credential bundle approach, which is actually the most secure pattern for wallet applications:

### Architecture
```
Mobile App → Backend → Turnkey (create session with HPKE public key)
                ↓
Mobile App ← Backend ← Turnkey (encrypted credential bundle)
                ↓
Mobile App → Turnkey (direct API calls with decrypted credentials)
```

### Why This is Correct

1. **True Non-Custodial**: Backend never sees user's decrypted credentials
2. **Hardware Security**: Mobile clients use secure enclaves to decrypt bundles
3. **Minimal Trust**: Backend only facilitates session creation
4. **Turnkey Best Practice**: This is the recommended pattern for wallet apps

## When to Use Each Pattern

### Pattern #1: API Key Authentication (Our current Activities/Crypto modules)
**Use for:**
- Administrative operations (create organizations, users)
- Server-initiated wallet creation
- Backend services that don't represent end users
- Infrastructure operations

**Example:**
```elixir
# Backend creates a wallet for a new user
Activities.create_wallet(org_id, user_id, "User Wallet", accounts)
# Uses our API key + stamp authentication
```

### Pattern #2: Session JWT Validation (Not currently implemented)
**Use for:**
- Traditional web applications where backend manages wallets
- Scenarios where backend needs to act on user's behalf
- When you want centralized access control

**Example:**
```elixir
# User authenticates with Turnkey → JWT → Backend validates JWT
def authenticated_endpoint(conn, params) do
  case validate_turnkey_jwt(get_auth_header(conn)) do
    {:ok, user_claims} -> 
      # Backend performs operations as the authenticated user
    {:error, _} -> 
      unauthorized(conn)
  end
end
```

### Pattern #3: Credential Bundles (Our Sessions module)
**Use for:**
- Wallet applications where users control their keys
- Mobile apps with hardware security modules
- Maximum security and non-custodial requirements
- Direct user-to-Turnkey interactions

**Example:**
```elixir
# Backend creates session, mobile decrypts bundle
Sessions.create_read_write_session_for_client(org_id, client_public_key)
# Mobile app decrypts bundle and makes direct Turnkey API calls
```

## Credential Types (From Turnkey Docs)

The credential types in the documentation refer to **how API keys were created**, not JWT contents:

| Credential Type | How API Key Was Created |
|-----------------|------------------------|
| `CREDENTIAL_TYPE_API_KEY_P256` | Traditional ECDSA API key (our backend keys) |
| `CREDENTIAL_TYPE_WEBAUTHN_AUTHENTICATOR` | Created via passkey authentication |
| `CREDENTIAL_TYPE_EMAIL_AUTH` | Created via email authentication |
| `CREDENTIAL_TYPE_SMS_AUTH` | Created via SMS/OTP authentication |
| `CREDENTIAL_TYPE_OAUTH` | Created via OAuth (Google, etc.) |

These types help Turnkey track the security level and origin of different API keys.

## When You Might Want Session JWT Validation

If you decide to implement **Pattern #2** (session JWT validation) in the future, it would be for scenarios like:

### Traditional Web App Authentication
```
User → Frontend → Turnkey Auth → JWT → Backend validates JWT → Database operations
```

### Centralized Access Control
```
Mobile → JWT → Backend → (Backend uses its API key to call Turnkey on user's behalf)
```

### Hybrid Architecture
```
Mobile → JWT for auth → Backend validates → Returns session bundle for direct operations
```

## Current Implementation Assessment

Our current implementation is **architecturally sound** and follows Turnkey's best practices:

✅ **API Key Authentication**: Correct for server-to-server operations
✅ **Credential Bundles**: Correct for non-custodial mobile wallet apps
✅ **Request Stamping**: Properly implements Turnkey's signature requirements
✅ **HPKE Encryption**: Properly handles session credential encryption

The only addition you might consider is **session JWT validation** if you want to support traditional web app authentication patterns alongside the mobile-first credential bundle approach.

## Conclusion

Your implementation is correct as-is. The confusion arose from thinking we needed to implement session JWT validation, but our credential bundle approach is actually more secure and appropriate for wallet applications. 

The Turnkey backend authentication documentation (JWT validation) is for different use cases than what we're building - it's for traditional web applications where the backend manages user operations, whereas we're building a non-custodial wallet where users control their own keys.

Our architecture choice aligns perfectly with Turnkey's vision for secure, decentralized wallet infrastructure.
