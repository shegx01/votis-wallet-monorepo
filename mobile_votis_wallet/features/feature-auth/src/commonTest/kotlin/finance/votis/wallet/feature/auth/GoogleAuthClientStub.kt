package finance.votis.wallet.feature.auth

import finance.votis.wallet.core.auth.AuthResult
import finance.votis.wallet.core.auth.AuthUser
import finance.votis.wallet.core.auth.GoogleAuthClient

/**
 * Test stub for GoogleAuthClient following the rule to use stubs over mocks
 */
class GoogleAuthClientStub : GoogleAuthClient {
    
    private var currentUser: AuthUser? = null
    private var signInResult: AuthResult = AuthResult.Success(
        AuthUser(
            id = "test_user_id",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/photo.jpg",
            idToken = "test_id_token"
        )
    )
    
    fun setSignInResult(result: AuthResult) {
        this.signInResult = result
        if (result is AuthResult.Success) {
            currentUser = result.user
        }
    }
    
    fun setCurrentUser(user: AuthUser?) {
        this.currentUser = user
    }
    
    override suspend fun signIn(): AuthResult {
        if (signInResult is AuthResult.Success) {
            currentUser = (signInResult as AuthResult.Success).user
        }
        return signInResult
    }
    
    override suspend fun signOut() {
        currentUser = null
    }
    
    override suspend fun getCurrentUser(): AuthUser? {
        return currentUser
    }
    
    override suspend fun isSignedIn(): Boolean {
        return currentUser != null
    }
}
