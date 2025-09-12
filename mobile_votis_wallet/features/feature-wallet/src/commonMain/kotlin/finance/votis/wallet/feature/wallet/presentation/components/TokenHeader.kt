package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.TokenBalance
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_copy
import org.jetbrains.compose.resources.painterResource

/**
 * Enhanced token header component for the token detail screen.
 * Shows token icon, name, and contract address with copy functionality.
 * Matches the design shown in the mockup with proper spacing and styling.
 */
@Composable
fun TokenHeader(
    tokenBalance: TokenBalance,
    onCopyAddress: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Token avatar
        TokenAvatar(
            symbol = tokenBalance.token.symbol,
            modifier = Modifier.size(32.dp),
        )

        // Token name and contract address
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = tokenBalance.token.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Contract address with copy icon
            tokenBalance.token.contractAddress?.let { address ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatContractAddress(address),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    Icon(
                        painter = painterResource(Res.drawable.ic_copy),
                        contentDescription = "Copy contract address",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .size(16.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onCopyAddress(address) },
                                ),
                    )
                }
            }
        }
    }
}

/**
 * Token avatar component showing token symbol with branded colors
 */
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

    Box(
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

/**
 * Format contract address to show first 6 and last 4 characters
 * e.g., "0x6b11...618d"
 */
private fun formatContractAddress(address: String): String =
    if (address.length > 10) {
        "${address.take(6)}...${address.takeLast(4)}"
    } else {
        address
    }
