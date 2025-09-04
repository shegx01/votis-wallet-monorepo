# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Repository Overview

Votis Wallet is a monorepo containing a cryptocurrency wallet platform with two main components:
- **Backend**: Elixir/Phoenix API server (`be_votis_wallet/`)
- **Mobile**: Kotlin Multiplatform Mobile app (`mobile_votis_wallet/`)

## Quick Development Setup

### Backend (Elixir/Phoenix)
```bash
cd be_votis_wallet
mix setup                    # Install deps, create/migrate DB, seed data
mix phx.server               # Start server on localhost:4000
```

### Mobile (Kotlin Multiplatform)
```bash
cd mobile_votis_wallet
./gradlew composeApp:build   # Build mobile app
./gradlew ktlintFormat       # Format code (required before commits)
./gradlew test               # Run tests
```

## Common Development Commands

### Backend Commands
```bash
# Development workflow
mix setup                           # One-time setup: deps + DB + seeds
mix phx.server                      # Start development server
iex -S mix phx.server              # Start with interactive shell

# Testing and quality
mix test                           # Run all tests
mix test_cover                     # Run tests with coverage report
mix test test/path/to/test.exs     # Run single test file
mix coveralls.html                 # Generate HTML coverage report

# Code quality (run before commits)
mix format                         # Format code
mix dialyzer                       # Static analysis
mix precommit                      # Full pre-commit check (format + test + dialyzer)

# Database operations
mix ecto.migrate                   # Run migrations
mix ecto.reset                     # Drop, recreate, migrate, seed
mix ecto.gen.migration name        # Generate new migration
```

### Mobile Commands
```bash
# Build and run
./gradlew composeApp:build                    # Build app
./gradlew composeApp:installDebug             # Install debug APK
./gradlew composeApp:iosSimulatorArm64Test    # Run iOS tests

# Code quality (always run before commits)
./gradlew ktlintFormat                        # Format code
./gradlew ktlintCheck                         # Check formatting
./gradlew detekt                              # Static analysis
./gradlew test                                # Run unit tests

# iOS-specific (if developing on macOS)
cd iosApp && pod install                      # Install CocoaPods dependencies
./iosApp/sync-frameworks.sh                  # Sync iOS frameworks
```

## Architecture Overview

### Backend Architecture (Phoenix/Elixir)
```
Phoenix Router
    ↓
Controllers (JSON API endpoints)
    ↓
Services (Business logic)
    ↓
Contexts (Domain boundaries)
    ↓
Schemas/Repo (Database layer)
```

**Key Components:**
- **Turnkey Integration**: Non-custodial wallet provider integration with user-controlled request stamping
- **Authentication**: Passkey/biometric authentication with encrypted credential bundles
- **Session Management**: HPKE-encrypted sessions (15min read-write, 1hr read-only)
- **Job Processing**: Oban for background tasks (rate limits, balance sync, webhooks)
- **Cryptography**: Production-grade ECDSA P-256, HKDF, X.509 certificate handling

**Backend Services Structure:**
- `lib/be_votis_wallet/services/turnkey/` - Wallet provider integration
- `lib/be_votis_wallet/services/http_client_behavior.ex` - HTTP client abstraction
- `lib/be_votis_wallet_web/` - Phoenix web layer (controllers, views, router)

### Mobile Architecture (Kotlin Multiplatform + MVI)
```
UI Layer (Compose Multiplatform)
    ↓ (User Intents)
Presentation Layer (ViewModels + MVI State)
    ↓ (Use Cases)
Domain Layer (Business Logic)
    ↓ (Repository Interfaces)
Data Layer (API + Local Storage)
```

**Module Structure:**
```
mobile_votis_wallet/
├── app/                        # Main application module
├── composeApp/                 # Legacy module (being migrated)
├── core/
│   ├── core-common/           # Shared utilities, extensions
│   ├── core-ui/               # Design system, MVI foundation
│   ├── core-domain/           # Business logic, use cases
│   ├── core-data/             # Repository implementations
│   ├── core-network/          # Backend API client
│   └── core-di/               # Dependency injection (Koin)
└── features/
    ├── feature-auth/          # Authentication flow
    ├── feature-onboarding/    # User registration
    ├── feature-wallet/        # Home dashboard, balance
    ├── feature-transactions/  # Send/receive/swap
    └── feature-settings/      # User preferences
```

**Key Patterns:**
- **MVI State Management**: Single immutable state per screen, user intents trigger state updates
- **expect/actual**: Platform-specific implementations (biometrics, keychain, etc.)
- **Backend Integration**: Mobile only communicates with Votis Phoenix backend (provider-agnostic)
- **Client-Side Signing**: Users sign their own Turnkey requests with ECDSA keypairs

## Testing Strategy

### Backend Testing
```bash
# Test structure follows Phoenix conventions
test/
├── be_votis_wallet/          # Context tests
├── be_votis_wallet_web/      # Controller tests
├── services/                 # Service layer tests
└── support/                  # Test helpers

# Testing patterns:
# - Setup blocks with context for user creation/cleanup
# - Mox for external service mocking (prefer stub/1 over expect/3)
# - ExCoveralls for coverage reporting
# - Always use on_exit/1 for test data cleanup
```

### Mobile Testing
```bash
# Unit tests per module
./gradlew :core-domain:test              # Domain logic tests
./gradlew :feature-auth:test             # Feature-specific tests
./gradlew test                           # All unit tests

# Test structure:
# - JUnit for common/android tests
# - iOS tests use Kotlin Test framework
# - Robolectric for Android UI testing
# - Compose UI testing for screen validation
```

## Code Quality Standards

### Backend Standards (Elixir)
- **Formatting**: Always run `mix format` before committing
- **Static Analysis**: `mix dialyzer` must pass
- **Testing**: Maintain >90% test coverage
- **Anti-patterns**: Follow Elixir anti-pattern guide (see AGENT.md)
- **Mock Strategy**: Prefer `stub/2` over `expect/3` for better test reliability

### Mobile Standards (Kotlin)
- **Formatting**: `./gradlew ktlintFormat` (enforced)
- **Linting**: `./gradlew ktlintCheck detekt` must pass
- **String Resources**: Extract all UI strings to resources, auto-generate translations
- **Material Icons**: Do not remove Material Icons dependencies (project rule)
- **Backward Compatibility**: Exempt in onboarding features only

## Development Workflow Rules

### Pre-commit Requirements
**Backend:**
```bash
cd be_votis_wallet
mix precommit    # Runs compile, format, dialyzer, test
```

**Mobile:**
```bash
cd mobile_votis_wallet
./gradlew ktlintFormat test
```

### Git Commit Standards
- Use conventional commit format: `type(scope): description`
- Keep messages under 72 characters
- Examples:
  - `feat(auth): add passkey authentication flow`
  - `fix(wallet): resolve balance calculation error`
  - `refactor(turnkey): improve session management`

### Testing Requirements
- **Backend**: All controller tests must use setup blocks with context for user creation/cleanup
- **Mobile**: Write tests for all new features, validate with coverage reports
- **Integration**: Test backend-mobile communication thoroughly

## Architecture Guidelines

### Backend-Mobile Communication
- **Single API**: Mobile only talks to Votis Phoenix backend
- **Provider Agnostic**: Backend handles all wallet provider logic (Turnkey, etc.)
- **Authentication**: Client-side passkey + server session management
- **Request Signing**: Mobile signs its own API requests with ECDSA keys

### Session Management
- **Client Sessions**: Mobile receives encrypted credential bundles, decrypts locally
- **Server Sessions**: Backend manages read-only sessions for resource queries
- **Security**: 15-minute read-write, 1-hour read-only session expiry

### Error Handling
- **Fail Fast**: Wallet operations, auth, transactions (immediate user feedback)
- **Retry with Backoff**: Rate limits, server errors, network issues (background jobs)
- **User Experience**: Provide clear error messages, never expose internal errors

## Platform-Specific Notes

### iOS Development
- **Setup**: Run `cd iosApp && pod install && ./sync-frameworks.sh`
- **Build**: Use `iosApp.xcworkspace` (not .xcodeproj)
- **Framework Sync**: Run `./sync-frameworks.sh` when switching between Xcode/Android Studio

### Android Development
- **Build**: Standard Android Studio workflow
- **Resources**: Use Compose Resources (not traditional Android resources)
- **Signing**: Configured for debug builds automatically

## Common Issues & Solutions

### Backend Issues
| Issue | Solution |
|-------|----------|
| Database connection errors | Run `mix ecto.reset` |
| Dependency conflicts | Delete `_build/` and run `mix deps.clean --all && mix setup` |
| Test failures | Ensure test DB is created: `MIX_ENV=test mix ecto.create` |

### Mobile Issues
| Issue | Solution |
|-------|----------|
| iOS framework errors | Run `./iosApp/sync-frameworks.sh` |
| Build cache issues | `./gradlew clean` (only when explicitly needed) |
| Compose resources not found | Rebuild project: `./gradlew composeApp:build` |

## Documentation References
- **Elixir Anti-patterns**: `AGENT.md` - Comprehensive guide to avoiding common Elixir code smells
- **Turnkey Integration**: `be_votis_wallet/TURNKEY_ARCHITECTURE.md`
- **Backend API**: Phoenix LiveDashboard at `localhost:4000/dev/dashboard`
- **Mobile Setup**: `mobile_votis_wallet/README.md` and `mobile_votis_wallet/.warp.md`
- **Turnkey Documentation**: https://docs.turnkey.com/llms-full.txt

## Important Project Rules
- Backend handles provider selection, not mobile client
- Extract string resources and auto-generate translations
- Use setup blocks with context for test user creation/cleanup  
- Prefer stub/2 over expect/3 in mocks
- Always run code formatting before commits
- Maintain test coverage and run dialyzer for Elixir code
- Backward compatibility exempt in mobile onboarding features only
