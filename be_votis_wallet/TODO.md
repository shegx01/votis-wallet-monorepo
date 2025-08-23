# Votis Wallet Backend - TODO Roadmap

## Overview

This document tracks pending development tasks for the Votis Wallet Backend, organized by thematic areas. Each task includes priority levels and actionable checkboxes to facilitate team coordination and progress tracking.

### Priority Legend
- **(P0)** - Critical/Blocking - Must be completed before release
- **(P1)** - High Priority - Important for core functionality  
- **(P2)** - Medium Priority - Nice to have, non-blocking

### Status Legend
- `- [ ]` - Pending
- `- [x]` - Completed
- `- [~]` - In Progress
- `- [!]` - Blocked

---

## 1. Wallet & Account Workflow Extensions

### Core Wallet Operations
- [ ] **(P0)** Design multi-chain wallet creation workflow
  - [ ] Define wallet schema for multiple blockchain support
  - [ ] Implement wallet metadata storage (name, description, chain configs)
  - [ ] Add wallet hierarchy management (sub-wallets, accounts)
  
- [ ] **(P0)** Implement account management extensions
  - [ ] Add account labeling and categorization
  - [ ] Create account balance aggregation views
  - [ ] Implement account transaction history pagination
  
- [ ] **(P1)** Enhanced wallet security features
  - [ ] Add wallet backup/recovery workflows
  - [ ] Implement spending limits per account
  - [ ] Create transaction approval workflows

### Multi-Chain Support
- [ ] **(P1)** Extend chain configuration system
  - [ ] Add support for Polygon, BSC, and Arbitrum
  - [ ] Implement chain-specific gas estimation
  - [ ] Create chain health monitoring endpoints
  
- [ ] **(P2)** Cross-chain transaction support
  - [ ] Design cross-chain bridge integration
  - [ ] Add bridge transaction tracking
  - [ ] Implement bridge fee estimation

---

## 2. Turnkey Session & Rate-Limit Handling

### Session Management Improvements
- [ ] **(P0)** Complete session manager implementation
  - [ ] Finalize JWT verification tests (currently excluded)
  - [ ] Add session refresh automation
  - [ ] Implement session invalidation on security events
  
- [ ] **(P0)** Rate-limit handling enhancements
  - [ ] Implement client-side rate limiting
  - [ ] Add exponential backoff for Turnkey API calls
  - [ ] Create rate-limit monitoring dashboard

### Auto-start & Monitoring
- [ ] **(P1)** Session auto-start mechanisms
  - [ ] Add startup session pre-warming
  - [ ] Implement health check endpoints for sessions
  - [ ] Create session metrics collection
  
- [ ] **(P1)** Advanced session features
  - [ ] Add session pooling for high-load scenarios
  - [ ] Implement session context switching
  - [ ] Create session debugging tools

---

## 3. Security Best Practices

### Cryptographic Security
- [ ] **(P0)** Audit and harden crypto module
  - [ ] Complete security review of ECDSA implementation
  - [ ] Add key rotation capabilities
  - [ ] Implement secure key storage patterns
  
- [ ] **(P0)** Request authentication security
  - [ ] Add replay attack prevention
  - [ ] Implement request timestamping validation
  - [ ] Create signature verification audit logging

### Application Security
- [ ] **(P1)** Input validation and sanitization
  - [ ] Add comprehensive input validation for all endpoints
  - [ ] Implement SQL injection prevention
  - [ ] Create CSRF protection for state-changing operations
  
- [ ] **(P1)** Secure communication
  - [ ] Implement certificate pinning guidelines
  - [ ] Add TLS 1.3 enforcement
  - [ ] Create secure header configurations

### Compliance & Auditing
- [ ] **(P2)** Security audit preparation
  - [ ] Document security architecture
  - [ ] Create security test suites
  - [ ] Implement security event logging

---

## 4. Code Implementation Tasks

### Core Backend Services
- [ ] **(P0)** Complete Turnkey integration services
  - [ ] Finalize HTTP client behavior implementation
  - [ ] Add comprehensive error handling patterns
  - [ ] Implement service health monitoring
  
- [ ] **(P1)** Database layer optimizations
  - [ ] Add proper database indexes for performance
  - [ ] Implement connection pooling optimization
  - [ ] Create database migration rollback strategies

### API Development
- [ ] **(P0)** RESTful API endpoints
  - [ ] Complete wallet management endpoints
  - [ ] Add transaction history endpoints with pagination
  - [ ] Implement account balance streaming endpoints
  
- [ ] **(P1)** GraphQL API (if applicable)
  - [ ] Design GraphQL schema for wallet operations
  - [ ] Implement real-time subscriptions
  - [ ] Add GraphQL query optimization

### Background Job Processing
- [ ] **(P1)** Oban job implementation
  - [ ] Create balance synchronization jobs
  - [ ] Add webhook processing jobs
  - [ ] Implement retry logic for failed operations
  
- [ ] **(P2)** Advanced job features
  - [ ] Add job prioritization
  - [ ] Implement job monitoring dashboard
  - [ ] Create job performance metrics

---

## 5. Testing, CI/CD & Release

### Test Coverage & Quality
- [ ] **(P0)** Comprehensive test suite
  - [ ] Achieve 90%+ test coverage for core modules
  - [ ] Add property-based tests for cryptographic functions
  - [ ] Implement integration tests for Turnkey workflows
  - [ ] **Note**: Use stubs for mocking (per project guidelines)
  
- [ ] **(P0)** Test automation
  - [ ] Set up automated test runs on PR
  - [ ] Add performance regression tests
  - [ ] Create load testing for session management

### CI/CD Pipeline
- [ ] **(P0)** Continuous Integration setup
  - [ ] Configure GitHub Actions/CI pipeline
  - [ ] Add automated code formatting checks (`mix format`)
  - [ ] Implement Dialyzer static analysis in CI
  - [ ] Add security dependency scanning
  
- [ ] **(P1)** Deployment automation
  - [ ] Create staging deployment pipeline
  - [ ] Implement blue-green production deployments
  - [ ] Add database migration automation
  
- [ ] **(P1)** Release management
  - [ ] Define semantic versioning strategy
  - [ ] Create automated changelog generation
  - [ ] Implement release candidate testing

### Code Quality Assurance
- [ ] **(P0)** Static analysis and linting
  - [ ] Ensure Dialyzer passes without warnings
  - [ ] Add Credo for code consistency
  - [ ] Implement commit hooks for formatting
  
- [ ] **(P2)** Documentation generation
  - [ ] Add ExDoc for API documentation
  - [ ] Create code examples and guides
  - [ ] Generate module documentation

---

## 6. Revised Authentication Flow - Client API

### Client Authentication Architecture
- [ ] **(P0)** Mobile client authentication
  - [ ] Design passkey/FIDO2 integration flow
  - [ ] Implement biometric authentication support
  - [ ] Add Google Auth as alternative IdP
  
- [ ] **(P0)** JWT session management
  - [ ] Create short-lived token issuance (15min RW, 1hr RO)
  - [ ] Implement token refresh mechanisms
  - [ ] Add session invalidation capabilities

### Client SDK Development
- [ ] **(P1)** Mobile SDK features
  - [ ] Create client-side request stamping utilities
  - [ ] Add ECDSA key generation helpers
  - [ ] Implement secure storage patterns
  
- [ ] **(P2)** SDK documentation and examples
  - [ ] Create integration guides for iOS/Android
  - [ ] Add code examples for common operations
  - [ ] Implement SDK testing frameworks

---

## 7. Service Endpoint Exposure for Clients

### Mobile Client Endpoints
- [ ] **(P0)** Core mobile API endpoints
  - [ ] `/api/mobile/auth/login` - Authentication endpoint
  - [ ] `/api/mobile/wallet/create` - Wallet creation
  - [ ] `/api/mobile/wallet/balance` - Balance queries
  - [ ] `/api/mobile/transactions/send` - Transaction creation
  
- [ ] **(P0)** Real-time communication
  - [ ] WebSocket endpoints for live updates
  - [ ] Push notification integration
  - [ ] Transaction status streaming

### Extension Client Endpoints
- [ ] **(P1)** Browser extension API
  - [ ] `/api/extension/auth/connect` - Extension connection
  - [ ] `/api/extension/wallet/list` - Wallet enumeration
  - [ ] `/api/extension/sign/transaction` - Transaction signing
  
- [ ] **(P1)** Extension security
  - [ ] Origin validation for extension requests
  - [ ] Extension-specific rate limiting
  - [ ] Secure communication protocols

### API Versioning & Documentation
- [ ] **(P1)** API versioning strategy
  - [ ] Implement semantic API versioning
  - [ ] Add backward compatibility support
  - [ ] Create API deprecation policies
  
- [ ] **(P2)** API documentation
  - [ ] Generate OpenAPI/Swagger documentation
  - [ ] Create interactive API explorer
  - [ ] Add client-specific integration guides

---

## 8. Separate Account Creation Endpoint

### Account Creation Architecture
- [ ] **(P0)** Dedicated account creation service
  - [ ] Design `/api/accounts/create` endpoint
  - [ ] Implement sub-organization creation workflow
  - [ ] Add account validation and verification
  
- [ ] **(P0)** Onboarding workflow optimization
  - [ ] Optimize onboarding latency (<800ms target)
  - [ ] Add progress tracking for account creation
  - [ ] Implement rollback mechanisms for failed creation

### Account Management Features
- [ ] **(P1)** Enhanced account operations
  - [ ] Add account recovery mechanisms
  - [ ] Implement account linking (multi-device)
  - [ ] Create account migration tools
  
- [ ] **(P2)** Account analytics and insights
  - [ ] Add account creation metrics
  - [ ] Implement user onboarding analytics
  - [ ] Create account health monitoring

---

## Cross-Cutting Implementation Guidelines

### Development Workflow
1. **Before any code changes:**
   - [ ] Review existing patterns in similar modules
   - [ ] Check AGENTS.md for project-specific guidelines
   - [ ] Verify Turnkey documentation for latest API changes

2. **During development:**
   - [ ] Follow Phoenix/Elixir conventions from AGENTS.md
   - [ ] Use `mix precommit` alias before committing
   - [ ] Prefer `:req` library for HTTP requests (avoid `:httpoison`)
   - [ ] Use stubs for mocking in tests

3. **Before committing:**
   - [ ] Run `mix format` to format code
   - [ ] Run `mix dialyzer` for static analysis
   - [ ] Ensure all tests pass with `mix test`
   - [ ] Create concise commit messages (<30 words)

### Security Checklist (Apply to All Features)
- [ ] Input validation implemented
- [ ] Authentication/authorization verified  
- [ ] No sensitive data in logs
- [ ] Secure communication protocols
- [ ] Error handling doesn't leak information

### Performance Checklist
- [ ] Database queries optimized
- [ ] Proper connection pooling
- [ ] Caching strategies implemented
- [ ] Monitoring and telemetry added

---

## Progress Tracking Template

### Adding New Tasks
When adding new tasks to this document, use this template:

```markdown
- [ ] **(P0|P1|P2)** Task title
  - [ ] Subtask 1 description
  - [ ] Subtask 2 description
  - Additional context or links if needed
```

### Status Updates
Update task status by changing checkbox symbols:
- `- [x]` when completed
- `- [~]` when in progress  
- `- [!]` when blocked

### Weekly Review
- Review and update task priorities
- Move completed tasks to archive
- Add new discovered tasks
- Update blocked task status

---

*Last updated: 2024-08-23*  
*Next review: Weekly team sync*
