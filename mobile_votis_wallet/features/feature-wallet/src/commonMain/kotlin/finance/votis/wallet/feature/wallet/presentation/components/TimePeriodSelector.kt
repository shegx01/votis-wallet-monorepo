package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.TimePeriod

/**
 * Extended TimePeriod enum to include ALL period for the token screen
 */
enum class ExtendedTimePeriod(
    val displayName: String,
    val timePeriod: TimePeriod?,
) {
    TWENTY_FOUR_HOURS("24H", TimePeriod.TWENTY_FOUR_HOURS),
    SEVEN_DAYS("7D", TimePeriod.SEVEN_DAYS),
    THIRTY_DAYS("30D", TimePeriod.THIRTY_DAYS),
    ONE_YEAR("1Y", TimePeriod.ONE_YEAR),
    ALL("ALL", null), // Special case for all-time view
}

/**
 * Horizontal time period selector component matching the design.
 * Shows tabs for 24H, 7D, 30D, 1Y, ALL with active state styling.
 */
@Composable
fun TimePeriodSelector(
    selectedPeriod: ExtendedTimePeriod,
    onPeriodSelected: (ExtendedTimePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExtendedTimePeriod.entries.forEach { period ->
            TimePeriodTab(
                period = period,
                isSelected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Individual time period tab component
 */
@Composable
private fun TimePeriodTab(
    period: ExtendedTimePeriod,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        },
                ).clickable(onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = period.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}
