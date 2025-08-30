package finance.votis.wallet.core.auth

import cocoapods.GoogleSignIn.GIDConfiguration
import cocoapods.GoogleSignIn.GIDSignIn
import cocoapods.GoogleSignIn.GIDSignInResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

/**
 * iOS implementation of GoogleAuthClient using Google Sign-In SDK
 */
@OptIn(ExperimentalForeignApi::class)
class IOSGoogleAuthClient : GoogleAuthClient {
    init {
        // Configure Google Sign-In
        val configuration = GIDConfiguration(clientID = OAuthConfig.getGoogleClientId())
        GIDSignIn.sharedInstance.configuration = configuration
    }

    override suspend fun signIn(): AuthResult =
        withContext(Dispatchers.Main) {
            try {
                // Check if already signed in
                val currentUser = GIDSignIn.sharedInstance.currentUser
                if (currentUser != null) {
                    return@withContext AuthResult.Success(currentUser.toAuthUser())
                }

                // Start sign-in flow
                suspendCancellableCoroutine<AuthResult> { continuation ->
                    val presentingViewController =
                        UIApplication.sharedApplication.keyWindow?.rootViewController
                            ?: run {
                                continuation.resume(
                                    AuthResult.Error(message = "No presenting view controller available"),
                                )
                                return@suspendCancellableCoroutine
                            }

                    GIDSignIn.sharedInstance.signInWithPresentingViewController(
                        presentingViewController,
                    ) { result: GIDSignInResult?, error ->
                        if (error != null) {
                            val authResult =
                                if (error.code == -5L) { // User cancelled
                                    AuthResult.Cancelled
                                } else {
                                    AuthResult.Error(
                                        message = "Google Sign-In failed: ${error.localizedDescription}",
                                    )
                                }
                            continuation.resume(authResult)
                        } else if (result?.user != null) {
                            continuation.resume(AuthResult.Success(result.user.toAuthUser()))
                        } else {
                            continuation.resume(
                                AuthResult.Error(message = "Unknown error occurred during sign-in"),
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                AuthResult.Error(
                    exception = e,
                    message = "Failed to start Google Sign-In: ${e.message}",
                )
            }
        }

    override suspend fun signOut(): Unit =
        withContext(Dispatchers.Main) {
            try {
                GIDSignIn.sharedInstance.signOut()
            } catch (e: Exception) {
                // Log error but don't throw - sign out should be best effort
            }
        }

    override suspend fun getCurrentUser(): AuthUser? =
        withContext(Dispatchers.Main) {
            GIDSignIn.sharedInstance.currentUser?.toAuthUser()
        }

    override suspend fun isSignedIn(): Boolean =
        withContext(Dispatchers.Main) {
            GIDSignIn.sharedInstance.currentUser != null
        }

    private fun cocoapods.GoogleSignIn.GIDGoogleUser.toAuthUser(): AuthUser =
        AuthUser(
            id = userID ?: "",
            email = profile?.email ?: "",
            displayName = profile?.name,
            photoUrl = profile?.imageURLWithDimension(320u)?.absoluteString,
            idToken = idToken?.tokenString ?: "",
        )
}
