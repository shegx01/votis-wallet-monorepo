package finance.votis.wallet.feature.onboarding.presentation.screen.username.usernameentry

import finance.votis.wallet.core.ui.mvi.BaseUiState

/**
 * UI state for the username entry screen.
 */
data class UsernameState(
    val username: String = "",
    val isLoading: Boolean = false,
    val isValidating: Boolean = false,
    val error: UsernameError? = null,
    val isUsernameValid: Boolean = false,
    val canContinue: Boolean = false,
) : BaseUiState {
    val hasError: Boolean get() = error != null
    val showLoadingIndicator: Boolean get() = isLoading || isValidating
}

/**
 * Different types of username validation errors.
 */
sealed class UsernameError {
    data object TooShort : UsernameError()

    data object TooLong : UsernameError()

    data object InvalidCharacters : UsernameError()

    data object StartsWithUnderscore : UsernameError()

    data object AlreadyTaken : UsernameError()

    data object NetworkError : UsernameError()

    data class Unknown(
        val message: String,
    ) : UsernameError()
}
