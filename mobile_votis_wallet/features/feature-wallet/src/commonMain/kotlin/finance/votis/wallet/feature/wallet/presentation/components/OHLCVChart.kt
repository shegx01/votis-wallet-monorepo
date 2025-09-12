package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.TimePeriod

/**
 * OHLCV Chart component for displaying token price data
 * Simplified implementation for now - TODO: Add proper chart library
 */
@Composable
fun OHLCVChart(
    tokenSymbol: String,
    timePeriod: TimePeriod,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Simplified chart display for now
        SimplePriceDisplay(
            tokenSymbol = tokenSymbol,
            timePeriod = timePeriod,
        )
    }
}

/**
 * Simplified chart placeholder
 */
@Composable
private fun SimplePriceDisplay(
    tokenSymbol: String,
    timePeriod: TimePeriod,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$tokenSymbol Chart (${timePeriod.displayName})\nPrice visualization area",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
