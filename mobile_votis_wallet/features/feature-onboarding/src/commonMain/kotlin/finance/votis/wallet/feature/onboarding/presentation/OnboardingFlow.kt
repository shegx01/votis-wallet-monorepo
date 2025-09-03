package finance.votis.wallet.feature.onboarding.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import finance.votis.wallet.feature.onboarding.presentation.navigation.*
import finance.votis.wallet.feature.onboarding.presentation.screen.account.AccountSelectionScreen
import finance.votis.wallet.feature.onboarding.presentation.screen.username.UsernameLandingScreen

/**
 * Main onboarding flow coordinator that manages the entire onboarding process
 * from animated onboarding carousel through account selection and username setup.
 */
@Composable
fun OnboardingFlow(
    onComplete: (OAuthResult, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember {
        mutableStateOf(
            OnboardingState(
                currentRoute = OnboardingRoute.AccountSelection,
            ),
        )
    }

    when (state.currentRoute) {
        OnboardingRoute.AccountSelection -> {
            AccountSelectionScreen(
                onGoogleSignIn = {
                    // TODO: Implement Google Sign-In integration
                    // For now, simulate successful authentication
                    val mockOAuthResult =
                        OAuthResult(
                            provider = AuthProvider.GOOGLE,
                            accessToken = "mock_google_token",
                            userInfo =
                                UserInfo(
                                    id = "google_123",
                                    email = "user@gmail.com",
                                    name = "John Doe",
                                    profilePictureUrl = null,
                                ),
                            isNewUser = true,
                        )

                    state =
                        state.copy(
                            oauthResult = mockOAuthResult,
                            hasCompletedAuth = true,
                            currentRoute = OnboardingRoute.UsernameLanding,
                        )
                },
                onAppleSignIn = {
                    // TODO: Implement Apple Sign-In integration
                    // For now, simulate successful authentication
                    val mockOAuthResult =
                        OAuthResult(
                            provider = AuthProvider.APPLE,
                            accessToken = "mock_apple_token",
                            userInfo =
                                UserInfo(
                                    id = "apple_456",
                                    email = "user@privaterelay.appleid.com",
                                    name = "Jane Smith",
                                    profilePictureUrl = null,
                                ),
                            isNewUser = true,
                        )

                    state =
                        state.copy(
                            oauthResult = mockOAuthResult,
                            hasCompletedAuth = true,
                            currentRoute = OnboardingRoute.UsernameLanding,
                        )
                },
            )
        }

        OnboardingRoute.UsernameLanding -> {
            UsernameLandingScreen(
                userInfo = state.oauthResult?.userInfo,
                onCreateUsername = {
                    // TODO: Navigate to actual username creation flow
                    // For now, simulate username creation
                    val username = "user_${System.currentTimeMillis()}"
                    state =
                        state.copy(
                            selectedUsername = username,
                            isCompleted = true,
                        )

                    // Complete onboarding with the created username
                    state.oauthResult?.let { oauthResult ->
                        onComplete(oauthResult, username)
                    }
                },
                onSkip = {
                    state = state.copy(isCompleted = true)

                    // Complete onboarding without username
                    state.oauthResult?.let { oauthResult ->
                        onComplete(oauthResult, null)
                    }
                },
            )
        }
    }
}

/**
 * Simplified onboarding flow that starts directly with account selection
 * (for cases where landing animation is not needed)
 */
@Composable
fun SimpleOnboardingFlow(
    onComplete: (OAuthResult, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember {
        mutableStateOf(
            OnboardingState(
                currentRoute = OnboardingRoute.AccountSelection,
            ),
        )
    }

    when (state.currentRoute) {
        OnboardingRoute.AccountSelection -> {
            AccountSelectionScreen(
                onGoogleSignIn = {
                    // TODO: Implement Google Sign-In integration
                    val mockOAuthResult =
                        OAuthResult(
                            provider = AuthProvider.GOOGLE,
                            accessToken = "mock_google_token",
                            userInfo =
                                UserInfo(
                                    id = "google_123",
                                    email = "user@gmail.com",
                                    name = "John Doe",
                                ),
                            isNewUser = false, // Existing user - skip username selection
                        )

                    if (mockOAuthResult.isNewUser) {
                        state =
                            state.copy(
                                oauthResult = mockOAuthResult,
                                hasCompletedAuth = true,
                                currentRoute = OnboardingRoute.UsernameLanding,
                            )
                    } else {
                        // Existing user - complete immediately
                        onComplete(mockOAuthResult, null)
                    }
                },
                onAppleSignIn = {
                    // TODO: Implement Apple Sign-In integration
                    val mockOAuthResult =
                        OAuthResult(
                            provider = AuthProvider.APPLE,
                            accessToken = "mock_apple_token",
                            userInfo =
                                UserInfo(
                                    id = "apple_456",
                                    email = "user@privaterelay.appleid.com",
                                    name = "Jane Smith",
                                ),
                            isNewUser = false, // Existing user - skip username selection
                        )

                    if (mockOAuthResult.isNewUser) {
                        state =
                            state.copy(
                                oauthResult = mockOAuthResult,
                                hasCompletedAuth = true,
                                currentRoute = OnboardingRoute.UsernameLanding,
                            )
                    } else {
                        // Existing user - complete immediately
                        onComplete(mockOAuthResult, null)
                    }
                },
            )
        }

        OnboardingRoute.UsernameLanding -> {
            UsernameLandingScreen(
                userInfo = state.oauthResult?.userInfo,
                onCreateUsername = {
                    // TODO: Navigate to actual username creation flow
                    // For now, simulate username creation
                    val username = "user_${System.currentTimeMillis()}"
                    state =
                        state.copy(
                            selectedUsername = username,
                            isCompleted = true,
                        )

                    state.oauthResult?.let { oauthResult ->
                        onComplete(oauthResult, username)
                    }
                },
                onSkip = {
                    state = state.copy(isCompleted = true)

                    state.oauthResult?.let { oauthResult ->
                        onComplete(oauthResult, null)
                    }
                },
            )
        }
    }
}
