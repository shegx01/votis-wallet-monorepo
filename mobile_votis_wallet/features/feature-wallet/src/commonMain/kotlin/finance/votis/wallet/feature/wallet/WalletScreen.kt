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
import finance.votis.wallet.core.domain.model.Nft
import finance.votis.wallet.core.domain.model.NftCollection
import finance.votis.wallet.core.domain.model.NftSale
import finance.votis.wallet.core.domain.model.NftTokenStandard
import finance.votis.wallet.core.domain.model.TimePeriod
import finance.votis.wallet.core.ui.components.TabCard
import finance.votis.wallet.feature.wallet.presentation.components.ActionButtonRow
import finance.votis.wallet.feature.wallet.presentation.components.AssetTabs
import finance.votis.wallet.feature.wallet.presentation.components.BottomNavTab
import finance.votis.wallet.feature.wallet.presentation.components.FrequentSendCarousel
import finance.votis.wallet.feature.wallet.presentation.components.NftGrid
import finance.votis.wallet.feature.wallet.presentation.components.TimeDropdown
import finance.votis.wallet.feature.wallet.presentation.components.TokenList
import finance.votis.wallet.feature.wallet.presentation.components.WalletBottomNavigation
import finance.votis.wallet.feature.wallet.presentation.components.WalletHeader
import finance.votis.wallet.feature.wallet.presentation.components.getMockTokenBalances
import kotlinx.datetime.Clock
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
    val mockNfts = getMockNfts()
    val totalBalanceValue = stringResource(Res.string.mock_total_balance)

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Sticky Header
        WalletHeader(
            username = username ?: "shegx01",
            onQrScanClick = { /* TODO: Implement QR scan */ },
            onSearchClick = { /* TODO: Implement search */ },
            onCopyUsernameClick = { /* TODO: Implement copy username */ },
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Balance display - large centered layout like in design
            item {
                BalanceDisplaySection(
                    balanceAmount = "$1,170,200.03",
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
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 64.dp),
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
                    modifier = Modifier.padding(top = 0.dp, bottom = 16.dp),
                )
            }

            // Asset Tabs (separate from card)
            item {
                AssetTabs(
                    selectedTab = selectedAssetType,
                    onTabSelected = { selectedAssetType = it },
                    tokenCount = mockTokenBalances.size,
                    nftCount = mockNfts.size,
                    approvalsCount = 0, // Mock data
                    modifier = Modifier.padding(top = 0.dp, bottom = 16.dp),
                )
            }

            // Asset content wrapped in TabCard
            item {
                TabCard(
                    content = {
                        // Asset content based on selected tab
                        when (selectedAssetType) {
                            AssetType.TOKENS -> {
                                TokenList(
                                    tokenBalances = mockTokenBalances,
                                    totalAssetsValue = totalBalanceValue,
                                    onTokenClick = { tokenBalance ->
                                        // TODO: Navigate to token details
                                    },
                                )
                            }
                            AssetType.NFTS -> {
                                NftGrid(
                                    nfts = mockNfts,
                                    onNftClick = { nft ->
                                        // TODO: Navigate to NFT details
                                    },
                                )
                            }
                            AssetType.APPROVALS -> {
                                // TODO: Implement approvals list
                                PlaceholderContent(
                                    message = "Approvals coming soon",
                                    modifier = Modifier.padding(40.dp),
                                )
                            }
                        }
                    },
                    contentDescription =
                        when (selectedAssetType) {
                            AssetType.TOKENS -> "Token list with balances and values"
                            AssetType.NFTS -> "NFT collection grid"
                            AssetType.APPROVALS -> "Token approvals management"
                        },
                    modifier = Modifier.padding(horizontal = 20.dp),
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
 * Creates mock NFT data for UI development matching the design specifications.
 */
@Composable
private fun getMockNfts(): List<Nft> {
    val now = Clock.System.now()

    return listOf(
        Nft(
            id = "nft_1",
            name = "Distant Galaxy",
            imageUrl = "https://picsum.photos/400/400?random=1",
            collection =
                NftCollection(
                    id = "collection_1",
                    name = "Space Collection",
                    description = "Futuristic space-themed digital art",
                    imageUrl = "https://picsum.photos/100/100?random=10",
                    floorPrice = "1 BNB",
                    totalSupply = 10000,
                    creatorAddress = "0x1234...5678",
                ),
            description = "A mesmerizing view of distant galaxies and cosmic phenomena",
            contractAddress = "0xabc...def",
            tokenId = "1001",
            tokenStandard = NftTokenStandard.ERC721,
            lastSale =
                NftSale(
                    price = "1 BNB",
                    currency = "BNB",
                    timestamp = now,
                    marketplace = "OpenSea",
                ),
        ),
        Nft(
            id = "nft_2",
            name = "Thy Art",
            imageUrl = "https://picsum.photos/400/400?random=2",
            collection =
                NftCollection(
                    id = "collection_2",
                    name = "Renaissance Digital",
                    description = "Classic art meets digital innovation",
                    imageUrl = "https://picsum.photos/100/100?random=11",
                    floorPrice = "1.63 ETH",
                    totalSupply = 5000,
                    creatorAddress = "0x2345...6789",
                ),
            description = "A digital masterpiece inspired by classical art",
            contractAddress = "0xbcd...efa",
            tokenId = "2001",
            tokenStandard = NftTokenStandard.ERC721,
            lastSale =
                NftSale(
                    price = "1.63 ETH",
                    currency = "ETH",
                    timestamp = now,
                    marketplace = "OpenSea",
                ),
        ),
        Nft(
            id = "nft_3",
            name = "Access",
            imageUrl = "https://picsum.photos/400/400?random=3",
            collection =
                NftCollection(
                    id = "collection_3",
                    name = "Urban Stories",
                    description = "Street photography meets digital art",
                    imageUrl = "https://picsum.photos/100/100?random=12",
                    floorPrice = "3 SOL",
                    totalSupply = 777,
                    creatorAddress = "0x3456...7890",
                ),
            description = "A powerful representation of access and opportunity",
            contractAddress = "0xcde...fab",
            tokenId = "4",
            tokenStandard = NftTokenStandard.ERC721,
            lastSale =
                NftSale(
                    price = "3 SOL",
                    currency = "SOL",
                    timestamp = now,
                    marketplace = "Magic Eden",
                ),
        ),
        Nft(
            id = "nft_4",
            name = "Cyberpunk Vision",
            imageUrl = "https://picsum.photos/400/400?random=4",
            collection =
                NftCollection(
                    id = "collection_4",
                    name = "Future Tech",
                    description = "Cyberpunk-inspired digital artworks",
                    imageUrl = "https://picsum.photos/100/100?random=13",
                    floorPrice = "0.5 ETH",
                    totalSupply = 2500,
                    creatorAddress = "0x4567...8901",
                ),
            description = "A glimpse into a cyberpunk future",
            contractAddress = "0xdef...abc",
            tokenId = "3001",
            tokenStandard = NftTokenStandard.ERC721,
        ),
        Nft(
            id = "nft_5",
            name = "Ocean Depths",
            imageUrl = "https://picsum.photos/400/400?random=5",
            collection =
                NftCollection(
                    id = "collection_5",
                    name = "Aquatic Dreams",
                    description = "Underwater worlds and marine life",
                    imageUrl = "https://picsum.photos/100/100?random=14",
                    floorPrice = "0.8 ETH",
                    totalSupply = 1500,
                    creatorAddress = "0x5678...9012",
                ),
            description = "Deep ocean mysteries captured in digital form",
            contractAddress = "0xefa...bcd",
            tokenId = "4001",
            tokenStandard = NftTokenStandard.ERC721,
        ),
        Nft(
            id = "nft_6",
            name = "Mountain Peak",
            imageUrl = "https://picsum.photos/400/400?random=6",
            collection =
                NftCollection(
                    id = "collection_6",
                    name = "Nature's Majesty",
                    description = "Breathtaking landscapes and natural wonders",
                    imageUrl = "https://picsum.photos/100/100?random=15",
                    floorPrice = "0.3 ETH",
                    totalSupply = 3333,
                    creatorAddress = "0x6789...0123",
                ),
            description = "Majestic mountain peaks reaching for the sky",
            contractAddress = "0xfab...cde",
            tokenId = "5001",
            tokenStandard = NftTokenStandard.ERC721,
        ),
    )
}

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
            lineHeight = 50.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Price change and time period in a centered row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
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

/**
 * Simple placeholder content for unimplemented features
 */
@Composable
private fun PlaceholderContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
