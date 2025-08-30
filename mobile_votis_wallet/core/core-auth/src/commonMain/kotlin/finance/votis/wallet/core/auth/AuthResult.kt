package finance.votis.wallet.core.auth

/**
 * Sealed class representing the result of an authentication attempt
 */
sealed class AuthResult {
    /**
     * Authentication was successful
     * @param user The authenticated user data
     */
    data class Success(val user: AuthUser) : AuthResult()

    /**
     * User cancelled the authentication process
     */
    data object Cancelled : AuthResult()

    /**
     * Authentication failed with an error
     * @param exception The underlying exception that caused the failure
     * @param message A human-readable error message
     */
    data class Error(
        val exception: Throwable? = null,
        val message: String = exception?.message ?: "Authentication failed"
    ) : AuthResult()
}

/**
 * Data class representing an authenticated user
 */
data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val idToken: String
)
