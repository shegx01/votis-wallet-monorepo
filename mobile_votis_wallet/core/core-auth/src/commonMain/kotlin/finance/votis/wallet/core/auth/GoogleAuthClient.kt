package finance.votis.wallet.core.auth

/**
 * Common interface for Google authentication across platforms
 */
interface GoogleAuthClient {
    /**
     * Initiates Google Sign-In process
     * @return AuthResult indicating success, cancellation, or error
     */
    suspend fun signIn(): AuthResult

    /**
     * Signs out the current user
     */
    suspend fun signOut()

    /**
     * Gets the currently signed-in user, if any
     * @return The current user or null if not signed in
     */
    suspend fun getCurrentUser(): AuthUser?

    /**
     * Checks if a user is currently signed in
     * @return true if user is signed in, false otherwise
     */
    suspend fun isSignedIn(): Boolean
}
