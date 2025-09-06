package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.TimePeriod
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_dropdown_arrow
import mobilevotiswallet.features.feature_wallet.generated.resources.time_period_1h
import mobilevotiswallet.features.feature_wallet.generated.resources.time_period_1y
import mobilevotiswallet.features.feature_wallet.generated.resources.time_period_24h
import mobilevotiswallet.features.feature_wallet.generated.resources.time_period_30d
import mobilevotiswallet.features.feature_wallet.generated.resources.time_period_7d
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Dropdown component for selecting time periods.
 * Shows the selected period and allows selection of different time periods.
 */
@Composable
fun TimeDropdown(
    selectedPeriod: TimePeriod,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Selected period button
        TimePeriodButton(
            period = selectedPeriod,
            isExpanded = isExpanded,
            onClick = onToggle,
        )

        // Dropdown menu
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onToggle,
            modifier =
                Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                    ).clip(RoundedCornerShape(12.dp)),
        ) {
            TimePeriod.values().forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(getTimePeriodStringResource(period)),
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                if (period == selectedPeriod) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    },
                    onClick = {
                        onPeriodSelected(period)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TimePeriodButton(
    period: TimePeriod,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp),
                ).padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(getTimePeriodStringResource(period)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Icon(
            painter = painterResource(Res.drawable.ic_dropdown_arrow),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun getTimePeriodStringResource(period: TimePeriod) =
    when (period) {
        TimePeriod.ONE_HOUR -> Res.string.time_period_1h
        TimePeriod.TWENTY_FOUR_HOURS -> Res.string.time_period_24h
        TimePeriod.SEVEN_DAYS -> Res.string.time_period_7d
        TimePeriod.THIRTY_DAYS -> Res.string.time_period_30d
        TimePeriod.ONE_YEAR -> Res.string.time_period_1y
    }

/*
@Composable
fun TimeDropdownPreview() {
    MaterialTheme {
        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TimeDropdown(
                selectedPeriod = TimePeriod.TWENTY_FOUR_HOURS,
                isExpanded = false,
                onToggle = { },
                onPeriodSelected = { },
            )

            TimeDropdown(
                selectedPeriod = TimePeriod.SEVEN_DAYS,
                isExpanded = true,
                onToggle = { },
                onPeriodSelected = { },
            )
        }
    }
}
*/
