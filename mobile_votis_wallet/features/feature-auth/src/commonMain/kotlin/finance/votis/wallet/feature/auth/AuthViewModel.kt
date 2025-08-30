package finance.votis.wallet.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import finance.votis.wallet.core.auth.AuthResult
import finance.votis.wallet.core.auth.GoogleAuthClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication state and actions
 * Following the rule that backend selects provider, this ViewModel
 * handles UI state but the backend will determine the authentication provider
 */
class AuthViewModel(
    private val googleAuthClient: GoogleAuthClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    /**
     * Initiate Google Sign-In process
     * Note: The backend is responsible for provider selection as per rule AVoSGjpM4lSabYxjdpdekO
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    error = null,
                )

            when (val result = googleAuthClient.signIn()) {
                is AuthResult.Success -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            user = result.user,
                            isSignedIn = true,
                            error = null,
                        )
                }

                is AuthResult.Cancelled -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = null, // Don't show error for user cancellation
                        )
                }

                is AuthResult.Error -> {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = result.message,
                        )
                }
            }
        }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                googleAuthClient.signOut()
                _uiState.value = AuthUiState() // Reset to initial state
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        error = "Sign out failed: ${e.message}",
                    )
            }
        }
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Check if user is currently signed in
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val user = googleAuthClient.getCurrentUser()
                _uiState.value =
                    _uiState.value.copy(
                        user = user,
                        isSignedIn = user != null,
                    )
            } catch (e: Exception) {
                // Silently fail - user is not signed in
                _uiState.value =
                    _uiState.value.copy(
                        user = null,
                        isSignedIn = false,
                    )
            }
        }
    }
}
