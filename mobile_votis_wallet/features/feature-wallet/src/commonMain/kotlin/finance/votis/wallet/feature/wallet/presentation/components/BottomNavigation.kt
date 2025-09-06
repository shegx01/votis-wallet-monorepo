package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.bottom_nav_browser
import mobilevotiswallet.features.feature_wallet.generated.resources.bottom_nav_exchange
import mobilevotiswallet.features.feature_wallet.generated.resources.bottom_nav_home
import mobilevotiswallet.features.feature_wallet.generated.resources.bottom_nav_settings
import mobilevotiswallet.features.feature_wallet.generated.resources.bottom_nav_transactions
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_browser
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_exchange
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_home
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_settings
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_transactions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom navigation tabs for the wallet app
 */
enum class BottomNavTab {
    HOME,
    EXCHANGE,
    TRANSACTIONS,
    BROWSER,
    SETTINGS,
}

/**
 * Bottom navigation bar component matching the original design exactly.
 * Features 5 tabs: Home, Exchange, Transactions, Browser, Settings
 */
@Composable
fun WalletBottomNavigation(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        BottomNavTab.values().forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(getTabIcon(tab)),
                        contentDescription = stringResource(getTabLabel(tab)),
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(getTabLabel(tab)),
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    ),
            )
        }
    }
}

/**
 * Get the icon resource for each tab
 */
private fun getTabIcon(tab: BottomNavTab) =
    when (tab) {
        BottomNavTab.HOME -> Res.drawable.ic_home
        BottomNavTab.EXCHANGE -> Res.drawable.ic_exchange
        BottomNavTab.TRANSACTIONS -> Res.drawable.ic_transactions
        BottomNavTab.BROWSER -> Res.drawable.ic_browser
        BottomNavTab.SETTINGS -> Res.drawable.ic_settings
    }

/**
 * Get the label resource for each tab
 */
private fun getTabLabel(tab: BottomNavTab) =
    when (tab) {
        BottomNavTab.HOME -> Res.string.bottom_nav_home
        BottomNavTab.EXCHANGE -> Res.string.bottom_nav_exchange
        BottomNavTab.TRANSACTIONS -> Res.string.bottom_nav_transactions
        BottomNavTab.BROWSER -> Res.string.bottom_nav_browser
        BottomNavTab.SETTINGS -> Res.string.bottom_nav_settings
    }
