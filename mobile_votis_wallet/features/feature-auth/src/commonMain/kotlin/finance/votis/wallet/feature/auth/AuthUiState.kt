package finance.votis.wallet.feature.auth

import finance.votis.wallet.core.auth.AuthUser

/**
 * UI state for authentication screens
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val user: AuthUser? = null,
    val error: String? = null,
    val isSignedIn: Boolean = false,
) {
    val showError: Boolean = error != null
    val canSignIn: Boolean = !isLoading && !isSignedIn
}
