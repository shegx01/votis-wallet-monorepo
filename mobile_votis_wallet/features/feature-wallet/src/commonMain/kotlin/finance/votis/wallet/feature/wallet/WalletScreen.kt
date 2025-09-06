package finance.votis.wallet.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.feature.wallet.presentation.components.ActionButtonRow
import finance.votis.wallet.feature.wallet.presentation.components.BalanceCard
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.home_screen_title
import org.jetbrains.compose.resources.stringResource

/**
 * Main wallet home screen showing balance, actions, and transactions.
 * This is a simplified version for initial navigation testing.
 * TODO: Integrate with full MVI architecture and ViewModel.
 */
@Composable
fun WalletScreen(
    username: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
    ) {
        WalletContent(username = username)
    }
}

@Composable
private fun WalletContent(username: String?) {
    LazyColumn {
        item {
            // Header with username if provided
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp),
            ) {
                Text(
                    text = stringResource(Res.string.home_screen_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                username?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "@$it",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            // Balance Card (simplified)
            BalanceCard(
                balanceAmount = "$17,200",
                priceChangeText = "+$233 +3%",
                isPriceChangePositive = true,
                selectedTimePeriod = finance.votis.wallet.core.domain.model.TimePeriod.TWENTY_FOUR_HOURS,
                isDropdownExpanded = false,
                onToggleDropdown = { /* TODO: Implement */ },
                onTimePeriodSelected = { /* TODO: Implement */ },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }

        item {
            // Action buttons
            ActionButtonRow(
                onReceiveClicked = { /* TODO: Implement */ },
                onSendClicked = { /* TODO: Implement */ },
                onSwapClicked = { /* TODO: Implement */ },
                onBuySellClicked = { /* TODO: Implement */ },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }

        item {
            // Coming soon message
            Text(
                text = "More features coming soon!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp),
            )
        }
    }
}
