package finance.votis.wallet.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import finance.votis.wallet.core.domain.model.TokenBalance
import finance.votis.wallet.core.ui.components.ServiceTopBar
import finance.votis.wallet.core.ui.components.TabCard
import finance.votis.wallet.feature.wallet.presentation.components.ActionButtonRow
import finance.votis.wallet.feature.wallet.presentation.components.ExtendedTimePeriod
import finance.votis.wallet.feature.wallet.presentation.components.OHLCVChart
import finance.votis.wallet.feature.wallet.presentation.components.PerformanceMetrics
import finance.votis.wallet.feature.wallet.presentation.components.TimePeriodSelector
import finance.votis.wallet.feature.wallet.presentation.components.TokenHeader
import finance.votis.wallet.feature.wallet.presentation.components.TokenInfo
import finance.votis.wallet.feature.wallet.presentation.components.TransactionHistory

/**
 * Complete token detail screen matching the design mockup.
 * Shows token info, balance, chart, performance metrics, token details, and transaction history.
 */
@Composable
fun TokenScreen(
    tokenBalance: TokenBalance,
    onBackClick: () -> Unit,
    onReceiveClicked: () -> Unit = {},
    onSendClicked: () -> Unit = {},
    onSwapClicked: () -> Unit = {},
    onBuySellClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // State for time period selection
    var selectedTimePeriod by remember { mutableStateOf(ExtendedTimePeriod.SEVEN_DAYS) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ServiceTopBar(
                content = {
                    TokenHeader(
                        tokenBalance = tokenBalance,
                        onCopyAddress = { address ->
                            // TODO: Implement copy to clipboard
                            println("Copied address: $address")
                        },
                    )
                },
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Token Balance Display (Large centered balance)
            item {
                TokenBalanceDisplay(
                    tokenBalance = tokenBalance,
                    priceChangePercent = getMockPriceChange(tokenBalance.token.symbol),
                    isPriceChangePositive = true,
                    modifier = Modifier.padding(vertical = 32.dp),
                )
            }

            // Time Period Selector
            item {
                TimePeriodSelector(
                    selectedPeriod = selectedTimePeriod,
                    onPeriodSelected = { selectedTimePeriod = it },
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }

            // OHLCV Chart
            item {
                OHLCVChart(
                    tokenSymbol = tokenBalance.token.symbol,
                    timePeriod =
                        selectedTimePeriod.timePeriod ?: finance.votis.wallet.core.domain.model.TimePeriod.SEVEN_DAYS,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Action buttons
            item {
                ActionButtonRow(
                    onReceiveClicked = onReceiveClicked,
                    onSendClicked = onSendClicked,
                    onSwapClicked = onSwapClicked,
                    onBuySellClicked = onBuySellClicked,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }

            // Performance Metrics
            item {
                TabCard(
                    content = {
                        PerformanceMetrics(
                            tokenSymbol = tokenBalance.token.symbol,
                        )
                    },
                    contentDescription = "Performance metrics for ${tokenBalance.token.name}",
                )
            }

            // Token Information
            item {
                TabCard(
                    content = {
                        TokenInfo(
                            tokenBalance = tokenBalance,
                        )
                    },
                    contentDescription = "Token information for ${tokenBalance.token.name}",
                )
            }

            // Transaction History
            item {
                TabCard(
                    content = {
                        TransactionHistory(
                            tokenBalance = tokenBalance,
                        )
                    },
                    contentDescription = "Transaction history for ${tokenBalance.token.name}",
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Token balance display section with large centered amount and price change
 */
@Composable
private fun TokenBalanceDisplay(
    tokenBalance: TokenBalance,
    priceChangePercent: String,
    isPriceChangePositive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // USD value (large display)
        Text(
            text = tokenBalance.usdValue,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 38.sp,
        )

        // Price change with green color
        Text(
            text = "+$233 +3%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4CAF50), // Green color matching the design
        )
    }
}

/**
 * Mock function to get price change percentage for a token
 */
private fun getMockPriceChange(symbol: String): String =
    when (symbol) {
        "BTC" -> "+2.35%"
        "ETH" -> "+1.58%"
        "SOL" -> "+4.21%"
        else -> "+0.0%"
    }
