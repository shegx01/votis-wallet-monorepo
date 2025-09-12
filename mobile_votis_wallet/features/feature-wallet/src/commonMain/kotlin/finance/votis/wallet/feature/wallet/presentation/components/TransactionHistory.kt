package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.TokenBalance
import finance.votis.wallet.core.domain.model.TransactionType

/**
 * Data class representing a transaction for display
 */
data class TransactionDisplayData(
    val type: TransactionType,
    val serviceName: String,
    val amount: String,
    val usdValue: String,
    val fromAddress: String,
    val isPositive: Boolean,
    val tokenSymbol: String, // Add token symbol for the overlay icon
)

/**
 * Transaction history component showing recent transactions
 * Matches the design with transaction icons, amounts, and details
 */
@Composable
fun TransactionHistory(
    tokenBalance: TokenBalance,
    modifier: Modifier = Modifier,
) {
    val transactions = getMockTransactionHistory(tokenBalance)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        transactions.forEach { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

/**
 * Individual transaction item component
 */
@Composable
private fun TransactionItem(
    transaction: TransactionDisplayData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Transaction icon
        TransactionIcon(
            type = transaction.type,
            isPositive = transaction.isPositive,
            tokenSymbol = transaction.tokenSymbol,
        )

        // Transaction details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        transaction.type.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "${if (transaction.isPositive) "+" else ""}${transaction.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "From ${formatAddress(transaction.fromAddress)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = transaction.usdValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Transaction icon component with proper colors and icons based on transaction type
 * Includes token asset overlay at bottom-right corner
 */
@Composable
private fun TransactionIcon(
    type: TransactionType,
    isPositive: Boolean,
    tokenSymbol: String,
    modifier: Modifier = Modifier,
) {
    val (icon, backgroundColor) = getTransactionIconAndColor(type, isPositive)

    androidx.compose.foundation.layout.Box(
        modifier = modifier.size(40.dp),
    ) {
        // Main transaction icon
        androidx.compose.foundation.layout.Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .background(
                        color = backgroundColor.copy(alpha = 0.1f),
                        shape = CircleShape,
                    ).clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                tint = backgroundColor,
                modifier = Modifier.size(20.dp),
            )
        }

        // Token asset overlay at bottom-right
        TokenAssetOverlay(
            tokenSymbol = tokenSymbol,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp),
        )
    }
}

/**
 * Token asset overlay component showing token symbol at bottom-right corner
 */
@Composable
private fun TokenAssetOverlay(
    tokenSymbol: String,
    modifier: Modifier = Modifier,
) {
    val tokenColor = getTokenColor(tokenSymbol)

    androidx.compose.foundation.layout.Box(
        modifier =
            modifier
                .background(
                    color = tokenColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = tokenSymbol,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = tokenColor,
        )
    }
}

/**
 * Get color for different tokens
 */
private fun getTokenColor(tokenSymbol: String): Color =
    when (tokenSymbol) {
        "BTC" -> Color(0xFFFF9500) // Bitcoin orange
        "ETH" -> Color(0xFF627EEA) // Ethereum blue
        "SOL" -> Color(0xFF9945FF) // Solana purple
        "USDT" -> Color(0xFF26A17B) // USDT green
        "USDC" -> Color(0xFF2775CA) // USDC blue
        else -> Color(0xFF6200EA) // Default purple
    }

/**
 * Get appropriate icon and color for transaction type
 */
private fun getTransactionIconAndColor(
    type: TransactionType,
    isPositive: Boolean,
): Pair<ImageVector, Color> =
    when (type) {
        TransactionType.RECEIVE -> Pair(Icons.Default.Add, Color(0xFF4CAF50))
        TransactionType.SEND -> Pair(Icons.Default.Close, Color(0xFFFF9500))
        TransactionType.SWAP -> Pair(Icons.Default.Refresh, Color(0xFF2196F3))
    }

/**
 * Format address for display (first 6 and last 4 characters)
 */
private fun formatAddress(address: String): String =
    if (address.length > 10) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }

/**
 * Generate mock transaction history based on token
 * In a real app, this would come from an API
 */
private fun getMockTransactionHistory(tokenBalance: TokenBalance): List<TransactionDisplayData> {
    val symbol = tokenBalance.token.symbol

    return listOf(
        TransactionDisplayData(
            type = TransactionType.RECEIVE,
            serviceName = "DeFi Protocol",
            amount = "0.1 $symbol",
            usdValue = "$30,200.50",
            fromAddress = "0x923B...8301",
            isPositive = true,
            tokenSymbol = symbol,
        ),
        TransactionDisplayData(
            type = TransactionType.SEND,
            serviceName = "Exchange",
            amount = "0.1 $symbol",
            usdValue = "$30,200.50",
            fromAddress = "0x923B...8301",
            isPositive = false,
            tokenSymbol = symbol,
        ),
        TransactionDisplayData(
            type = TransactionType.SEND,
            serviceName = "USDT Transfer",
            amount = "100 USDT",
            usdValue = "$30,200.50",
            fromAddress = "0x923B...8301",
            isPositive = false,
            tokenSymbol = "USDT",
        ),
        TransactionDisplayData(
            type = TransactionType.SEND,
            serviceName = "Exchange",
            amount = "0.1 $symbol",
            usdValue = "$30,200.50",
            fromAddress = "0x923B...8301",
            isPositive = false,
            tokenSymbol = symbol,
        ),
        TransactionDisplayData(
            type = TransactionType.SWAP,
            serviceName = "DeFi Swap",
            amount = "+ 0.1 ETH",
            usdValue = "- 320.50 USDC",
            fromAddress = "pump.fun",
            isPositive = true,
            tokenSymbol = "ETH",
        ),
        TransactionDisplayData(
            type = TransactionType.RECEIVE,
            serviceName = "Revolut",
            amount = "+ 0.1 $symbol",
            usdValue = "$30,200.50",
            fromAddress = "0x923B...8301",
            isPositive = true,
            tokenSymbol = symbol,
        ),
        TransactionDisplayData(
            type = TransactionType.SEND,
            serviceName = "MoonPay",
            amount = "- 0.1 $symbol",
            usdValue = "$30,200.50",
            fromAddress = "0x923B...8301",
            isPositive = false,
            tokenSymbol = symbol,
        ),
    )
}
