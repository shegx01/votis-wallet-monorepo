package finance.votis.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import finance.votis.wallet.core.ui.theme.AppTheme
import finance.votis.wallet.feature.onboarding.presentation.OnboardingFlow
import finance.votis.wallet.feature.wallet.WalletScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    AppTheme {
        // Temporary simplified version to test Koin initialization
        val appViewModel: AppViewModel = koinInject()
        val appState by appViewModel.state.collectAsState()

        // Initialize the app on first composition
        LaunchedEffect(Unit) {
            appViewModel.handleIntent(AppIntent.Initialize)
        }

        when (appState) {
            is AppState.Loading -> {
                LoadingScreen()
            }
            is AppState.Unauthenticated -> {
                // Show full onboarding flow starting with LandingScreen
                OnboardingFlow(
                    onComplete = {
                            oauthResult: finance.votis.wallet.feature.onboarding.presentation.navigation.OAuthResult,
                            username: String?,
                        ->
                        // TODO: Handle completion of onboarding
                        // For now, just simulate navigation to authenticated state
                        println("Onboarding completed with provider: ${oauthResult.provider}, username: $username")
                        // Simulate authentication state change
                        // appViewModel.handleIntent(AppIntent.MockAuthentication(oauthResult.userInfo.email))
                    },
                )
            }
            is AppState.Authenticated -> {
                // Show the main wallet screen
                WalletScreen(username = "shegx01")
            }
            is AppState.Error -> {
                ErrorScreen(message = (appState as AppState.Error).message)
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
