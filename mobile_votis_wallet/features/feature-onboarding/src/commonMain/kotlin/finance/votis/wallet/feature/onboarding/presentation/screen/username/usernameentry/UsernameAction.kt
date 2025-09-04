package finance.votis.wallet.feature.onboarding.presentation.screen.username.usernameentry

import finance.votis.wallet.core.ui.mvi.BaseIntent

/**
 * User intents/actions for the username entry screen.
 */
sealed class UsernameAction : BaseIntent {
    /**
     * User changed the username input
     */
    data class OnUsernameChanged(
        val username: String,
    ) : UsernameAction()

    /**
     * User clicked the continue button
     */
    data object OnContinueClicked : UsernameAction()

    /**
     * Clear any current error state
     */
    data object ClearError : UsernameAction()

    /**
     * Retry username validation (e.g., after network error)
     */
    data object RetryValidation : UsernameAction()
}
