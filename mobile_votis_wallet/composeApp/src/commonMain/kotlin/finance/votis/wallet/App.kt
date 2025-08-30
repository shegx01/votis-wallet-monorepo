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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import finance.votis.wallet.feature.onboarding.AccountSelectionScreen
import finance.votis.wallet.feature.wallet.WalletScreen
import finance.votis.wallet.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

sealed class Route(
    val path: String,
) {
    data object Onboarding : Route("onboarding")

    data object WalletHome : Route("wallet_home")
}

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
                // Temporarily show AccountSelectionScreen directly without Navigation
                AccountSelectionScreen(
                    onGoogleSignIn = {
                        // TODO: Handle Google Sign-In
                        println("Google Sign-In clicked - would navigate to wallet")
                    },
                    onAppleSignIn = {
                        // TODO: Handle Apple Sign-In
                        println("Apple Sign-In clicked - would navigate to wallet")
                    },
                )
            }
            is AppState.Authenticated -> {
                // Temporarily show success message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Authenticated - Wallet would show here",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
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

@Composable
private fun AppNavHost(startDestination: String) {
    val navController = rememberNavController()

    key(startDestination) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Route.Onboarding.path) {
                AccountSelectionScreen(
                    onGoogleSignIn = {
                        navController.navigate(Route.WalletHome.path) {
                            popUpTo(Route.Onboarding.path) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onAppleSignIn = {
                        navController.navigate(Route.WalletHome.path) {
                            popUpTo(Route.Onboarding.path) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Route.WalletHome.path) {
                WalletScreen()
            }
        }
    }
}
