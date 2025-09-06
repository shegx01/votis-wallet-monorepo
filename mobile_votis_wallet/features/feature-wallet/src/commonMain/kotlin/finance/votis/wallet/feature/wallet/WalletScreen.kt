package finance.votis.wallet.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.AssetType
import finance.votis.wallet.core.domain.model.ContactUser
import finance.votis.wallet.core.domain.model.TimePeriod
import finance.votis.wallet.feature.wallet.presentation.components.ActionButtonRow
import finance.votis.wallet.feature.wallet.presentation.components.AssetTabs
import finance.votis.wallet.feature.wallet.presentation.components.BalanceCard
import finance.votis.wallet.feature.wallet.presentation.components.FrequentSendCarousel
import finance.votis.wallet.feature.wallet.presentation.components.TokenList
import finance.votis.wallet.feature.wallet.presentation.components.WalletHeader
import finance.votis.wallet.feature.wallet.presentation.components.getMockTokenBalances
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_total_balance
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
    // State management for interactive elements
    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.TWENTY_FOUR_HOURS) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedAssetType by remember { mutableStateOf(AssetType.TOKENS) }

    // Mock data for development
    val mockContacts = getMockFrequentContacts()
    val mockTokenBalances = getMockTokenBalances()
    val totalBalanceValue = stringResource(Res.string.mock_total_balance)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        // Header with user handle and action icons
        item {
            WalletHeader(
                username = username ?: "user",
                onQrScanClick = { /* TODO: Implement QR scan */ },
                onSearchClick = { /* TODO: Implement search */ },
            )
        }

        // Balance Card
        item {
            BalanceCard(
                balanceAmount = totalBalanceValue,
                priceChangeText = "+$1,234.56 +2.41%",
                isPriceChangePositive = true,
                selectedTimePeriod = selectedTimePeriod,
                isDropdownExpanded = isDropdownExpanded,
                onToggleDropdown = { isDropdownExpanded = !isDropdownExpanded },
                onTimePeriodSelected = {
                    selectedTimePeriod = it
                    isDropdownExpanded = false
                },
            )
        }

        // Action buttons
        item {
            ActionButtonRow(
                onReceiveClicked = { /* TODO: Navigate to receive */ },
                onSendClicked = { /* TODO: Navigate to send */ },
                onSwapClicked = { /* TODO: Navigate to swap */ },
                onBuySellClicked = { /* TODO: Navigate to buy/sell */ },
            )
        }

        // Frequent Send Carousel
        item {
            FrequentSendCarousel(
                contacts = mockContacts,
                onContactClick = { contact ->
                    // TODO: Navigate to send with pre-filled contact
                },
            )
        }

        // Asset Tabs
        item {
            AssetTabs(
                selectedTab = selectedAssetType,
                onTabSelected = { selectedAssetType = it },
                tokenCount = mockTokenBalances.size,
                nftCount = 0, // Mock data
                approvalsCount = 0, // Mock data
            )
        }

        // Token List (only shown when TOKENS tab is selected)
        if (selectedAssetType == AssetType.TOKENS) {
            item {
                TokenList(
                    tokenBalances = mockTokenBalances,
                    totalAssetsValue = totalBalanceValue,
                    onTokenClick = { tokenBalance ->
                        // TODO: Navigate to token details
                    },
                )
            }
        }
    }
}

/**
 * Creates mock frequent contact data for UI development
 */
@Composable
private fun getMockFrequentContacts(): List<ContactUser> =
    listOf(
        ContactUser(
            id = "1",
            username = "alice",
            walletAddress = "0x1234...5678",
            displayName = "Alice",
        ),
        ContactUser(
            id = "2",
            username = "bob",
            walletAddress = "0x2345...6789",
            displayName = "Bob",
        ),
        ContactUser(
            id = "3",
            username = "charlie",
            walletAddress = "0x3456...7890",
            displayName = "Charlie",
        ),
        ContactUser(
            id = "4",
            username = "diana",
            walletAddress = "0x4567...8901",
            displayName = "Diana",
        ),
    )
