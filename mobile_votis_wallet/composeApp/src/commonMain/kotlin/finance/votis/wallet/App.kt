package finance.votis.wallet

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import finance.votis.wallet.feature.wallet.WalletScreen
import finance.votis.wallet.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

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
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Onboarding.path,
    ) {
        composable(Route.Onboarding.path) {
            OnboardingScreen(
                onContinue = {
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
