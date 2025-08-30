package finance.votis.wallet.core.auth

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Android implementation of GoogleAuthClient using Google Sign-In SDK
 */
class AndroidGoogleAuthClient(
    private val context: Context,
    private val activity: ComponentActivity
) : GoogleAuthClient {

    private val googleSignInClient: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(OAuthConfig.getGoogleClientId())
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, options)
    }

    private var signInLauncher: ActivityResultLauncher<Intent>? = null
    private var signInContinuation: kotlin.coroutines.Continuation<AuthResult>? = null

    init {
        signInLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleSignInResult(result.data)
        }
    }

    override suspend fun signIn(): AuthResult = withContext(Dispatchers.Main) {
        try {
            // Check if already signed in
            val account = getSignedInAccount()
            if (account != null) {
                return@withContext AuthResult.Success(account.toAuthUser())
            }

            // Start sign-in flow
            suspendCancellableCoroutine { continuation ->
                signInContinuation = continuation
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher?.launch(signInIntent)
                
                continuation.invokeOnCancellation {
                    signInContinuation = null
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(
                exception = e,
                message = "Failed to start Google Sign-In: ${e.message}"
            )
        }
    }

    private fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            signInContinuation?.resume(AuthResult.Success(account.toAuthUser()))
        } catch (e: ApiException) {
            val result = when (e.statusCode) {
                12501 -> AuthResult.Cancelled // User cancelled
                else -> AuthResult.Error(
                    exception = e,
                    message = "Google Sign-In failed: ${e.message}"
                )
            }
            signInContinuation?.resume(result)
        } catch (e: Exception) {
            signInContinuation?.resume(
                AuthResult.Error(
                    exception = e,
                    message = "Authentication error: ${e.message}"
                )
            )
        } finally {
            signInContinuation = null
        }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            googleSignInClient.signOut().addOnCompleteListener { }
        } catch (e: Exception) {
            // Log error but don't throw - sign out should be best effort
        }
    }

    override suspend fun getCurrentUser(): AuthUser? = withContext(Dispatchers.IO) {
        getSignedInAccount()?.toAuthUser()
    }

    override suspend fun isSignedIn(): Boolean = withContext(Dispatchers.IO) {
        getSignedInAccount() != null
    }

    private fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    private fun GoogleSignInAccount.toAuthUser(): AuthUser {
        return AuthUser(
            id = id ?: "",
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            idToken = idToken ?: ""
        )
    }
}
