package finance.votis.wallet.core.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of GoogleAuthClient using Google Sign-In SDK
 * Note: This is a simplified version for testing purposes.
 * In a real implementation, you would integrate with Activity for the sign-in intent.
 */
class AndroidGoogleAuthClient(
    private val context: Context,
) : GoogleAuthClient {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val options =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(OAuthConfig.getGoogleClientId())
                .requestEmail()
                .build()

        GoogleSignIn.getClient(context, options)
    }

    override suspend fun signIn(): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                // Check if already signed in
                val account = getSignedInAccount()
                if (account != null) {
                    return@withContext AuthResult.Success(account.toAuthUser())
                }

                // For testing purposes, return an error indicating Activity integration needed
                // In a real app, this would start the sign-in intent
                AuthResult.Error(
                    message = "Sign-in requires Activity integration. This is a test implementation.",
                )
            } catch (e: Exception) {
                AuthResult.Error(
                    exception = e,
                    message = "Failed to check sign-in status: ${e.message}",
                )
            }
        }

    override suspend fun signOut(): Unit =
        withContext(Dispatchers.IO) {
            try {
                googleSignInClient.signOut()
            } catch (e: Exception) {
                // Log error but don't throw - sign out should be best effort
            }
        }

    override suspend fun getCurrentUser(): AuthUser? =
        withContext(Dispatchers.IO) {
            getSignedInAccount()?.toAuthUser()
        }

    override suspend fun isSignedIn(): Boolean =
        withContext(Dispatchers.IO) {
            getSignedInAccount() != null
        }

    private fun getSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    private fun GoogleSignInAccount.toAuthUser(): AuthUser =
        AuthUser(
            id = id ?: "",
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            idToken = idToken ?: "",
        )
}
