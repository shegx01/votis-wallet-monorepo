package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.action_buy_sell
import mobilevotiswallet.features.feature_wallet.generated.resources.action_receive
import mobilevotiswallet.features.feature_wallet.generated.resources.action_send
import mobilevotiswallet.features.feature_wallet.generated.resources.action_swap
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_dollar
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_qr_code
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_send
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_swap
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Row of action buttons for wallet operations: Receive, Send, Swap, Buy/Sell.
 */
@Composable
fun ActionButtonRow(
    onReceiveClicked: () -> Unit,
    onSendClicked: () -> Unit,
    onSwapClicked: () -> Unit,
    onBuySellClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(
            icon = painterResource(Res.drawable.ic_qr_code),
            label = stringResource(Res.string.action_receive),
            onClick = onReceiveClicked,
            modifier = Modifier.weight(1f),
        )

        ActionButton(
            icon = painterResource(Res.drawable.ic_send),
            label = stringResource(Res.string.action_send),
            onClick = onSendClicked,
            modifier = Modifier.weight(1f),
        )

        ActionButton(
            icon = painterResource(Res.drawable.ic_swap),
            label = stringResource(Res.string.action_swap),
            onClick = onSwapClicked,
            modifier = Modifier.weight(1f),
        )

        ActionButton(
            icon = painterResource(Res.drawable.ic_dollar),
            label = stringResource(Res.string.action_buy_sell),
            onClick = onBuySellClicked,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionButton(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Remove material ripple
                    onClick = onClick,
                ).padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Rounded rectangle icon background - matching the design image
        Column(
            modifier =
                Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp),
                    ).clip(RoundedCornerShape(16.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }

        // Label with proper typography
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/*
@Composable
fun ActionButtonRowPreview() {
    MaterialTheme {
        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
        ) {
            ActionButtonRow(
                onReceiveClicked = { },
                onSendClicked = { },
                onSwapClicked = { },
                onBuySellClicked = { },
            )
        }
    }
}
*/
