package finance.votis.wallet.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Wallet address display component matching the design spec.
 * Shows a clean address display with copy button on the right, without background.
 */
@Composable
fun WalletAddressCard(
    address: String,
    onCopyAddress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Address display
        Text(
            text = formatAddressForSpec(address),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Copy button with background
        IconButton(
            onClick = { onCopyAddress(address) },
            modifier =
                Modifier
                    .size(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ),
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy address",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Format the wallet address according to the design spec.
 * Shows address in format: 0x6b11465731404...e618d
 */
private fun formatAddressForSpec(address: String): String =
    if (address.length > 20) {
        val start = address.take(14) // First 14 characters including 0x
        val end = address.takeLast(5) // Last 5 characters
        "$start...$end"
    } else {
        address
    }
