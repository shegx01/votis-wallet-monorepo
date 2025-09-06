package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.Token
import finance.votis.wallet.core.domain.model.TokenBalance
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_btc_amount
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_btc_change
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_btc_value
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_eth_amount
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_eth_change
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_eth_value
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_sol_amount
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_sol_change
import mobilevotiswallet.features.feature_wallet.generated.resources.mock_sol_value
import mobilevotiswallet.features.feature_wallet.generated.resources.token_bitcoin
import mobilevotiswallet.features.feature_wallet.generated.resources.token_ethereum
import mobilevotiswallet.features.feature_wallet.generated.resources.token_solana
import mobilevotiswallet.features.feature_wallet.generated.resources.token_symbol_btc
import mobilevotiswallet.features.feature_wallet.generated.resources.token_symbol_eth
import mobilevotiswallet.features.feature_wallet.generated.resources.token_symbol_sol
import mobilevotiswallet.features.feature_wallet.generated.resources.total_assets_amount
import mobilevotiswallet.features.feature_wallet.generated.resources.total_assets_label
import org.jetbrains.compose.resources.stringResource

/**
 * Component showing the token list with total assets and individual token balances.
 * Matches the original design with proper spacing and styling.
 */
@Composable
fun TokenList(
    tokenBalances: List<TokenBalance>,
    totalAssetsValue: String,
    onTokenClick: (TokenBalance) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Total assets header
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.total_assets_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = stringResource(Res.string.total_assets_amount, totalAssetsValue),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Token list
        Column {
            tokenBalances.forEach { tokenBalance ->
                TokenListItem(
                    tokenBalance = tokenBalance,
                    onClick = { onTokenClick(tokenBalance) },
                )
            }
        }
    }
}

@Composable
private fun TokenListItem(
    tokenBalance: TokenBalance,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Token icon/avatar
        TokenAvatar(
            symbol = tokenBalance.token.symbol,
            modifier = Modifier.size(44.dp),
        )

        // Token info (name and amount)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = tokenBalance.token.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = tokenBalance.amount,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Value and change percentage
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = tokenBalance.usdValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Price change (mock data for now)
            Text(
                text = getMockPriceChange(tokenBalance.token.symbol),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary, // Assuming positive change
            )
        }
    }
}

@Composable
private fun TokenAvatar(
    symbol: String,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        when (symbol) {
            "BTC" ->
                androidx.compose.ui.graphics
                    .Color(0xFFFF9500) // Bitcoin orange
            "ETH" ->
                androidx.compose.ui.graphics
                    .Color(0xFF627EEA) // Ethereum blue
            "SOL" ->
                androidx.compose.ui.graphics
                    .Color(0xFF9945FF) // Solana purple
            else -> MaterialTheme.colorScheme.primary
        }

    androidx.compose.foundation.layout.Box(
        modifier =
            modifier
                .background(
                    color = backgroundColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = backgroundColor,
        )
    }
}

@Composable
private fun getMockPriceChange(symbol: String): String =
    when (symbol) {
        "BTC" -> stringResource(Res.string.mock_btc_change)
        "ETH" -> stringResource(Res.string.mock_eth_change)
        "SOL" -> stringResource(Res.string.mock_sol_change)
        else -> "+0.0%"
    }

/**
 * Creates mock token balance data matching the original design
 */
@Composable
fun getMockTokenBalances(): List<TokenBalance> =
    listOf(
        TokenBalance(
            token =
                Token(
                    symbol = stringResource(Res.string.token_symbol_btc),
                    name = stringResource(Res.string.token_bitcoin),
                    decimals = 8,
                ),
            amount = stringResource(Res.string.mock_btc_amount),
            usdValue = stringResource(Res.string.mock_btc_value),
        ),
        TokenBalance(
            token =
                Token(
                    symbol = stringResource(Res.string.token_symbol_eth),
                    name = stringResource(Res.string.token_ethereum),
                    decimals = 18,
                ),
            amount = stringResource(Res.string.mock_eth_amount),
            usdValue = stringResource(Res.string.mock_eth_value),
        ),
        TokenBalance(
            token =
                Token(
                    symbol = stringResource(Res.string.token_symbol_sol),
                    name = stringResource(Res.string.token_solana),
                    decimals = 9,
                ),
            amount = stringResource(Res.string.mock_sol_amount),
            usdValue = stringResource(Res.string.mock_sol_value),
        ),
    )
