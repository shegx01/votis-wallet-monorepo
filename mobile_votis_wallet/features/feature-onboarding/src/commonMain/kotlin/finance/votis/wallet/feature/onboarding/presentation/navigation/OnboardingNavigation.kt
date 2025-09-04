package finance.votis.wallet.feature.onboarding.presentation.navigation

/**
 * Represents the different screens in the onboarding flow
 */
sealed class OnboardingRoute(
    val path: String,
) {
    data object AccountSelection : OnboardingRoute("onboarding_account_selection")

    data object UsernameLanding : OnboardingRoute("onboarding_username_landing")

    data object UsernameEntry : OnboardingRoute("onboarding_username_entry")
}

/**
 * Represents the state of the onboarding flow
 */
data class OnboardingState(
    val currentRoute: OnboardingRoute = OnboardingRoute.AccountSelection,
    val isLoading: Boolean = false,
    val oauthResult: OAuthResult? = null,
    val selectedUsername: String? = null,
    val hasCompletedAuth: Boolean = false,
    val isCompleted: Boolean = false,
)

/**
 * OAuth authentication result from Google/Apple
 */
data class OAuthResult(
    val provider: AuthProvider,
    val accessToken: String,
    val userInfo: UserInfo,
    val isNewUser: Boolean = false,
)

data class UserInfo(
    val id: String,
    val email: String,
    val name: String,
    val profilePictureUrl: String? = null,
)

enum class AuthProvider {
    GOOGLE,
    APPLE,
}

/**
 * Actions that can be performed during onboarding
 */
sealed class OnboardingAction {
    data object NavigateToAccountSelection : OnboardingAction()

    data object NavigateToUsernameLanding : OnboardingAction()

    data object NavigateToUsernameEntry : OnboardingAction()

    data object NavigateToHome : OnboardingAction()

    data class CompleteOAuth(
        val result: OAuthResult,
    ) : OnboardingAction()

    data class SetUsername(
        val username: String,
    ) : OnboardingAction()

    data object SkipUsernameSelection : OnboardingAction()

    data object NavigateBack : OnboardingAction()

    data object RetryAuthentication : OnboardingAction()
}
