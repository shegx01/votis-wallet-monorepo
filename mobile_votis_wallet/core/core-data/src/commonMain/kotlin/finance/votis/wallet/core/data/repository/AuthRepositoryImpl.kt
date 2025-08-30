package finance.votis.wallet.core.data.repository

import finance.votis.wallet.core.data.datasource.AuthSessionDataSource
import finance.votis.wallet.core.domain.Result
import finance.votis.wallet.core.domain.model.AuthSession
import finance.votis.wallet.core.domain.model.User
import finance.votis.wallet.core.domain.repository.AuthCredential
import finance.votis.wallet.core.domain.repository.AuthRepository
import finance.votis.wallet.core.domain.repository.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

/**
 * Implementation of AuthRepository with session persistence.
 */
class AuthRepositoryImpl(
    private val authSessionDataSource: AuthSessionDataSource,
    private val clock: Clock = Clock.System,
) : AuthRepository {
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private var currentSession: AuthSession? = null

    init {
        // Initialize authentication state on creation
        // This will be called when DI container creates the repository
    }

    /**
     * Loads session from storage and updates auth state.
     * Should be called during app initialization.
     */
    suspend fun initialize() {
        currentSession = authSessionDataSource.read()
        updateAuthState()
    }

    override suspend fun registerUser(userId: String): Result<User> {
        // TODO: Implement backend registration
        return Result.failure(Exception("Registration not yet implemented"))
    }

    override suspend fun authenticateWithBiometric(): Result<AuthSession> {
        // TODO: Implement biometric authentication
        return Result.failure(Exception("Biometric auth not yet implemented"))
    }

    override suspend fun createSession(authCredential: AuthCredential): Result<AuthSession> {
        // TODO: Implement session creation with backend
        return Result.failure(Exception("Session creation not yet implemented"))
    }

    override suspend fun refreshSession(): Result<AuthSession> {
        // TODO: Implement session refresh with backend
        return Result.failure(Exception("Session refresh not yet implemented"))
    }

    override suspend fun getCurrentSession(): AuthSession? =
        if (isSessionValid(currentSession)) currentSession else null

    override suspend fun logout(): Result<Unit> =
        try {
            currentSession = null
            authSessionDataSource.clear()
            authStateFlow.value = AuthState.Unauthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun observeAuthState(): Flow<AuthState> = authStateFlow.asStateFlow()

    override suspend fun isAuthenticated(): Boolean = isSessionValid(currentSession)

    /**
     * Updates the auth state based on current session validity.
     */
    private fun updateAuthState() {
        authStateFlow.value =
            when {
                isSessionValid(currentSession) -> AuthState.Authenticated
                currentSession != null -> AuthState.SessionExpired
                else -> AuthState.Unauthenticated
            }
    }

    /**
     * Checks if a session is valid (exists and not expired).
     */
    private fun isSessionValid(session: AuthSession?): Boolean {
        if (session == null) return false

        val now = clock.now()
        return session.expiresAt > now
    }

    /**
     * Saves a session and updates the auth state.
     * Internal helper for future backend integration.
     */
    private suspend fun saveSession(session: AuthSession) {
        currentSession = session
        authSessionDataSource.save(session)
        updateAuthState()
    }
}
