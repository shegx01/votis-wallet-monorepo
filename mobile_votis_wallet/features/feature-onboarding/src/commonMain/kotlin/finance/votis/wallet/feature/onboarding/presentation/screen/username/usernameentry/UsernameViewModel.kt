package finance.votis.wallet.feature.onboarding.presentation.screen.username.usernameentry

import androidx.lifecycle.viewModelScope
import finance.votis.wallet.core.domain.Result
import finance.votis.wallet.core.domain.usecase.UsernameValidationError
import finance.votis.wallet.core.domain.usecase.UsernameValidationResult
import finance.votis.wallet.core.domain.usecase.ValidateUsernameUseCase
import finance.votis.wallet.core.ui.mvi.BaseViewModelWithEffects
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the username entry screen following MVI pattern.
 * Handles username validation, state management, and navigation effects.
 */
class UsernameViewModel(
    private val validateUsernameUseCase: ValidateUsernameUseCase,
) : BaseViewModelWithEffects<UsernameState, UsernameAction, UsernameEffect>(
        initialState = UsernameState(),
    ) {
    private var validationJob: Job? = null

    override fun handleIntent(intent: UsernameAction) {
        when (intent) {
            is UsernameAction.OnUsernameChanged -> handleUsernameChanged(intent.username)
            is UsernameAction.OnContinueClicked -> handleContinueClicked()
            is UsernameAction.ClearError -> clearError()
            is UsernameAction.RetryValidation -> retryValidation()
        }
    }

    private fun handleUsernameChanged(username: String) {
        // Cancel previous validation
        validationJob?.cancel()

        updateState {
            copy(
                username = username,
                error = null,
                isUsernameValid = false,
                canContinue = false,
            )
        }

        // Only validate if username is not empty
        if (username.isNotBlank()) {
            // Debounce validation to avoid excessive API calls
            validationJob =
                viewModelScope.launch {
                    delay(500) // Wait for user to stop typing
                    validateUsername(username)
                }
        }
    }

    private suspend fun validateUsername(username: String) {
        updateState { copy(isValidating = true) }

        when (val result = validateUsernameUseCase(username)) {
            is Result.Success -> {
                when (val validationResult = result.data) {
                    is UsernameValidationResult.Valid -> {
                        updateState {
                            copy(
                                isValidating = false,
                                error = null,
                                isUsernameValid = true,
                                canContinue = true,
                            )
                        }
                    }
                    is UsernameValidationResult.Invalid -> {
                        val error = mapValidationErrorToUsernameError(validationResult.error)
                        updateState {
                            copy(
                                isValidating = false,
                                error = error,
                                isUsernameValid = false,
                                canContinue = false,
                            )
                        }
                    }
                }
            }
            is Result.Failure -> {
                updateState {
                    copy(
                        isValidating = false,
                        error = UsernameError.NetworkError,
                        isUsernameValid = false,
                        canContinue = false,
                    )
                }
            }
        }
    }

    private fun mapValidationErrorToUsernameError(error: UsernameValidationError): UsernameError =
        when (error) {
            is UsernameValidationError.TooShort -> UsernameError.TooShort
            is UsernameValidationError.TooLong -> UsernameError.TooLong
            is UsernameValidationError.InvalidCharacters -> UsernameError.InvalidCharacters
            is UsernameValidationError.StartsWithUnderscore -> UsernameError.StartsWithUnderscore
            is UsernameValidationError.AlreadyTaken -> UsernameError.AlreadyTaken
            is UsernameValidationError.NetworkError -> UsernameError.NetworkError
        }

    private fun handleContinueClicked() {
        val state = currentState
        if (state.canContinue && state.isUsernameValid && !state.hasError) {
            sendEffect(UsernameEffect.NavigateToNextScreen(state.username))
        }
    }

    private fun clearError() {
        updateState { copy(error = null) }
    }

    private fun retryValidation() {
        val username = currentState.username
        if (username.isNotBlank()) {
            viewModelScope.launch {
                validateUsername(username)
            }
        }
    }
}
