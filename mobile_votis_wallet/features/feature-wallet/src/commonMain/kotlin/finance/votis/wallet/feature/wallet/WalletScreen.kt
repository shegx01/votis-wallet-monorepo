package finance.votis.wallet.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import finance.votis.wallet.core.domain.model.AssetType
import finance.votis.wallet.core.domain.model.ContactUser
import finance.votis.wallet.core.domain.model.TimePeriod
import finance.votis.wallet.feature.wallet.presentation.components.ActionButtonRow
import finance.votis.wallet.feature.wallet.presentation.components.AssetTabs
import finance.votis.wallet.feature.wallet.presentation.components.BottomNavTab
import finance.votis.wallet.feature.wallet.presentation.components.FrequentSendCarousel
import finance.votis.wallet.feature.wallet.presentation.components.TimeDropdown
import finance.votis.wallet.feature.wallet.presentation.components.TokenList
import finance.votis.wallet.feature.wallet.presentation.components.WalletBottomNavigation
import finance.votis.wallet.feature.wallet.presentation.components.WalletHeader
import finance.votis.wallet.feature.wallet.presentation.components.getMockTokenBalances
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_total_balance
import org.jetbrains.compose.resources.stringResource

/**
 * Main wallet home screen showing balance, actions, and transactions.
 * Features complete bottom navigation with 5 tabs: Home, Exchange, Transactions, Browser, Settings.
 * TODO: Integrate with full MVI architecture and ViewModel.
 */
@Composable
fun WalletScreen(
    username: String? = null,
    modifier: Modifier = Modifier,
) {
    // Bottom navigation state
    var selectedBottomNavTab by remember { mutableStateOf(BottomNavTab.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            WalletBottomNavigation(
                selectedTab = selectedBottomNavTab,
                onTabSelected = { tab ->
                    selectedBottomNavTab = tab
                    // TODO: Handle navigation based on selected tab
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
        ) {
            when (selectedBottomNavTab) {
                BottomNavTab.HOME -> WalletContent(username = username)
                BottomNavTab.EXCHANGE -> PlaceholderScreen("Exchange", "Exchange features coming soon")
                BottomNavTab.TRANSACTIONS -> PlaceholderScreen("Transactions", "Transaction history coming soon")
                BottomNavTab.BROWSER -> PlaceholderScreen("Browser", "DApp browser coming soon")
                BottomNavTab.SETTINGS -> PlaceholderScreen("Settings", "Settings panel coming soon")
            }
        }
    }
}

@Composable
private fun WalletContent(username: String?) {
    // State management for interactive elements
    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.TWENTY_FOUR_HOURS) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedAssetType by remember { mutableStateOf(AssetType.TOKENS) }

    // Mock data for development
    val mockContacts = getMockFrequentContactsLocal() // Use local function without underscores
    val mockTokenBalances = getMockTokenBalances()
    val totalBalanceValue = stringResource(Res.string.mock_total_balance)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier,
    ) {
        // Header with user handle and action icons - matching exact design spacing
        item {
            WalletHeader(
                username = username ?: "shegx01",
                onQrScanClick = { /* TODO: Implement QR scan */ },
                onSearchClick = { /* TODO: Implement search */ },
                onCopyUsernameClick = { /* TODO: Implement copy username */ },
            )
        }

        // Balance display - large centered layout like in design
        item {
            BalanceDisplaySection(
                balanceAmount = "$17,200",
                priceChangeText = "+$233",
                priceChangePercent = "+3%",
                isPriceChangePositive = true,
                selectedTimePeriod = selectedTimePeriod,
                isDropdownExpanded = isDropdownExpanded,
                onToggleDropdown = { isDropdownExpanded = !isDropdownExpanded },
                onTimePeriodSelected = {
                    selectedTimePeriod = it
                    isDropdownExpanded = false
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
            )
        }

        // Action buttons
        item {
            ActionButtonRow(
                onReceiveClicked = { /* TODO: Navigate to receive */ },
                onSendClicked = { /* TODO: Navigate to send */ },
                onSwapClicked = { /* TODO: Navigate to swap */ },
                onBuySellClicked = { /* TODO: Navigate to buy/sell */ },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }

        // Frequent Send Carousel - now in card format
        item {
            FrequentSendCarousel(
                contacts = mockContacts,
                onContactClick = { contact ->
                    // TODO: Navigate to send with pre-filled contact
                },
                modifier = Modifier.padding(vertical = 16.dp),
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
                modifier = Modifier.padding(vertical = 16.dp),
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
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * Creates mock frequent contact data for UI development without underscores
 */
@Composable
private fun getMockFrequentContactsLocal(): List<ContactUser> =
    listOf(
        ContactUser(
            id = "1",
            username = "angel.lubin",
            walletAddress = "0x1234...5678",
            displayName = "Angel Lubin",
        ),
        ContactUser(
            id = "2",
            username = "ann.baptist",
            walletAddress = "0x2345...6789",
            displayName = "Ann Baptist",
        ),
        ContactUser(
            id = "3",
            username = "makenna.turner",
            walletAddress = "0x3456...7890",
            displayName = "Makenna Turner",
        ),
        ContactUser(
            id = "4",
            username = "craig.septimus",
            walletAddress = "0x4567...8901",
            displayName = "Craig Septimus",
        ),
        ContactUser(
            id = "5",
            username = "carter.galileo",
            walletAddress = "0x5678...9012",
            displayName = "Carter Galileo",
        ),
        ContactUser(
            id = "6",
            username = "emilie.lawson",
            walletAddress = "0x6789...0123",
            displayName = "Emilie Lawson",
        ),
        ContactUser(
            id = "7",
            username = "marisa.blake",
            walletAddress = "0x7890...1234",
            displayName = "Marisa Blake",
        ),
        ContactUser(
            id = "8",
            username = "jason.keller",
            walletAddress = "0x8901...2345",
            displayName = "Jason Keller",
        ),
    )

/**
 * Balance display section that matches the exact design from the image.
 * Shows large centered balance with price change and time period selector.
 */
@Composable
private fun BalanceDisplaySection(
    balanceAmount: String,
    priceChangeText: String,
    priceChangePercent: String,
    isPriceChangePositive: Boolean,
    selectedTimePeriod: TimePeriod,
    isDropdownExpanded: Boolean,
    onToggleDropdown: () -> Unit,
    onTimePeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Large balance amount - matching design typography
        Text(
            text = balanceAmount,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 56.sp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Price change and time period in a row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Price change amount
            Text(
                text = priceChangeText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color =
                    if (isPriceChangePositive) {
                        androidx.compose.ui.graphics
                            .Color(0xFF00C851) // Green color like in design
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )

            // Price change percentage with light green background
            Text(
                text = priceChangePercent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color =
                    if (isPriceChangePositive) {
                        androidx.compose.ui.graphics
                            .Color(0xFF00C851) // Green color like in design
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                modifier =
                    Modifier
                        .background(
                            color =
                                if (isPriceChangePositive) {
                                    androidx.compose.ui.graphics
                                        .Color(0xFF00C851)
                                        .copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                },
                            shape = RoundedCornerShape(8.dp),
                        ).padding(horizontal = 8.dp, vertical = 4.dp),
            )

            // Time period dropdown
            TimeDropdown(
                selectedPeriod = selectedTimePeriod,
                isExpanded = isDropdownExpanded,
                onToggle = onToggleDropdown,
                onPeriodSelected = onTimePeriodSelected,
            )
        }
    }
}

/**
 * Placeholder screen for tabs that are not yet implemented
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
