package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.TokenBalance

/**
 * Data class representing token information
 */
data class TokenInfoData(
    val label: String,
    val value: String,
)

/**
 * Token information component showing detailed token stats
 * Matches the design with Symbol, Network, MCap, Total supply, Circulating supply
 */
@Composable
fun TokenInfo(
    tokenBalance: TokenBalance,
    modifier: Modifier = Modifier,
) {
    val tokenInfoData = getTokenInfoData(tokenBalance)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tokenInfoData.forEach { data ->
            TokenInfoRow(data = data)
        }
    }
}

/**
 * Individual token info row component
 */
@Composable
private fun TokenInfoRow(
    data: TokenInfoData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = data.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = data.value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Generate token information data based on token
 * In a real app, this would come from an API
 */
private fun getTokenInfoData(tokenBalance: TokenBalance): List<TokenInfoData> {
    val token = tokenBalance.token

    return when (token.symbol) {
        "BTC" ->
            listOf(
                TokenInfoData("Symbol", "BTC"),
                TokenInfoData("Network", "Bitcoin"),
                TokenInfoData("MCap", "$2.1T"),
                TokenInfoData("Total supply", "19.89 BTC"),
                TokenInfoData("Circulating supply", "19.89 BTC"),
            )
        "ETH" ->
            listOf(
                TokenInfoData("Symbol", "ETH"),
                TokenInfoData("Network", "Ethereum"),
                TokenInfoData("MCap", "$337.2B"),
                TokenInfoData("Total supply", "120.43M ETH"),
                TokenInfoData("Circulating supply", "120.43M ETH"),
            )
        "SOL" ->
            listOf(
                TokenInfoData("Symbol", "SOL"),
                TokenInfoData("Network", "Solana"),
                TokenInfoData("MCap", "$84.3B"),
                TokenInfoData("Total supply", "467.32M SOL"),
                TokenInfoData("Circulating supply", "467.32M SOL"),
            )
        else ->
            listOf(
                TokenInfoData("Symbol", token.symbol),
                TokenInfoData("Network", "Unknown"),
                TokenInfoData("MCap", "N/A"),
                TokenInfoData("Total supply", "N/A"),
                TokenInfoData("Circulating supply", "N/A"),
            )
    }
}
