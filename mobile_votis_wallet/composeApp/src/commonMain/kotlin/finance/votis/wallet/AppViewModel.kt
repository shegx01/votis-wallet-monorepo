package finance.votis.wallet

import finance.votis.wallet.core.domain.repository.AuthRepository
import finance.votis.wallet.core.ui.mvi.BaseIntent
import finance.votis.wallet.core.ui.mvi.BaseUiState
import finance.votis.wallet.core.ui.mvi.BaseViewModel

/**
 * ViewModel for managing app-level authentication state and navigation.
 */
class AppViewModel(
    private val authRepository: AuthRepository,
) : BaseViewModel<AppState, AppIntent>(AppState.Loading) {
    val state = uiState

    override fun handleIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.Initialize -> initialize()
            is AppIntent.Logout -> logout()
        }
    }

    private fun initialize() {
        launchViewModelScope {
            try {
                // Initialize the auth repository to load persisted session
                if (authRepository is finance.votis.wallet.core.data.repository.AuthRepositoryImpl) {
                    authRepository.initialize()
                }

                val isAuthenticated = authRepository.isAuthenticated()

                updateState {
                    if (isAuthenticated) {
                        AppState.Authenticated
                    } else {
                        AppState.Unauthenticated
                    }
                }
            } catch (e: Exception) {
                updateState { AppState.Error(e.message ?: "Unknown error occurred") }
            }
        }
    }

    private fun logout() {
        launchViewModelScope {
            try {
                authRepository.logout()
                updateState { AppState.Unauthenticated }
            } catch (e: Exception) {
                // For now, just update state with error
                updateState { AppState.Error(e.message ?: "Logout failed") }
            }
        }
    }
}

/**
 * App-level state for authentication and navigation.
 */
sealed class AppState : BaseUiState {
    object Loading : AppState()

    object Unauthenticated : AppState()

    object Authenticated : AppState()

    data class Error(
        val message: String,
    ) : AppState()
}

/**
 * App-level intents.
 */
sealed class AppIntent : BaseIntent {
    object Initialize : AppIntent()

    object Logout : AppIntent()
}

/**
 * App-level side effects.
 */
sealed class AppSideEffect {
    data class ShowError(
        val message: String,
    ) : AppSideEffect()
}
