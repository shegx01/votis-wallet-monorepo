package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import finance.votis.wallet.core.ui.theme.primaryDark
import finance.votis.wallet.core.ui.theme.primaryLight
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
 * Clean bottom navigation bar matching the exact design from the image.
 * Features 5 tabs with minimal styling: Home, Exchange, Transactions, Browser, Settings
 */
@Composable
fun WalletBottomNavigation(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkMode = isSystemInDarkTheme()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
    ) {
        Column {
            // Top border
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )

            // Navigation items
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BottomNavTab.entries.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        isSelected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        isDarkMode = isDarkMode,
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
) {
    // Theme-aware colors for light and dark mode using Votis primary colors
    val selectedColor =
        if (isDarkMode) {
            primaryDark // Votis primary color for dark mode
        } else {
            primaryLight // Votis primary color for light mode
        }

    val unselectedColor =
        if (isDarkMode) {
            Color(0xFF8E8E93) // Same gray works in both modes
        } else {
            Color(0xFF8E8E93) // Light gray for light mode
        }

    Column(
        modifier =
            modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(getTabIcon(tab)),
            contentDescription = stringResource(getTabLabel(tab)),
            modifier = Modifier.size(22.dp), // Slightly larger for better visibility
            tint = if (isSelected) selectedColor else unselectedColor,
        )

        Text(
            text = stringResource(getTabLabel(tab)),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) selectedColor else unselectedColor,
            maxLines = 1,
        )
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
