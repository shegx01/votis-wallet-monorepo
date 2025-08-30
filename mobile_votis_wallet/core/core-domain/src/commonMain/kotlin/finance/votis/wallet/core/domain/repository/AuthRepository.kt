package finance.votis.wallet.core.domain.repository

import finance.votis.wallet.core.domain.Result
import finance.votis.wallet.core.domain.model.AuthSession
import finance.votis.wallet.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Handles user registration, login, and session management.
 */
interface AuthRepository {
    /**
     * Registers a new user with the backend.
     */
    suspend fun registerUser(userId: String): Result<User>

    /**
     * Authenticates user with biometric/passkey.
     */
    suspend fun authenticateWithBiometric(): Result<AuthSession>

    /**
     * Creates a new session after successful authentication.
     */
    suspend fun createSession(authCredential: AuthCredential): Result<AuthSession>

    /**
     * Refreshes the current session.
     */
    suspend fun refreshSession(): Result<AuthSession>

    /**
     * Gets the current valid session if available.
     */
    suspend fun getCurrentSession(): AuthSession?

    /**
     * Logs out the user and clears session.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Observes authentication state changes.
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Checks if user is currently authenticated.
     */
    suspend fun isAuthenticated(): Boolean
}

/**
 * Authentication credential from biometric/passkey authentication.
 */
data class AuthCredential(
    val signature: String,
    val clientData: String,
    val authenticatorData: String,
    val credentialId: String,
)

/**
 * Current authentication state.
 */
sealed class AuthState {
    object Unauthenticated : AuthState()

    object Authenticated : AuthState()

    object SessionExpired : AuthState()

    data class Error(
        val message: String,
    ) : AuthState()
}
