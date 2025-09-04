package finance.votis.wallet.feature.onboarding.presentation.screen.username.usernameentry

import finance.votis.wallet.core.ui.mvi.BaseUiEffect

/**
 * One-shot UI effects for the username entry screen.
 */
sealed class UsernameEffect : BaseUiEffect {
    /**
     * Navigate to the next screen after successful username creation
     */
    data class NavigateToNextScreen(
        val username: String,
    ) : UsernameEffect()

    /**
     * Show a toast or snackbar message
     */
    data class ShowMessage(
        val message: String,
    ) : UsernameEffect()

    /**
     * Navigate back to the previous screen
     */
    data object NavigateBack : UsernameEffect()
}
