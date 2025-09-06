package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.TimePeriod

/**
 * Card component displaying the user's total balance with price change information
 * and time period selection dropdown.
 */
@Composable
fun BalanceCard(
    balanceAmount: String,
    priceChangeText: String?,
    isPriceChangePositive: Boolean,
    selectedTimePeriod: TimePeriod,
    isDropdownExpanded: Boolean,
    onToggleDropdown: () -> Unit,
    onTimePeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Balance amount
            Text(
                text = balanceAmount,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price change and time period selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Price change
                priceChangeText?.let { changeText ->
                    Text(
                        text = changeText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color =
                            if (isPriceChangePositive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                    )
                }

                // Time period dropdown
                TimeDropdown(
                    selectedPeriod = selectedTimePeriod,
                    isExpanded = isDropdownExpanded,
                    onToggle = onToggleDropdown,
                    onPeriodSelected = onTimePeriodSelected,
                )
            }
        }
    }
}

/*
@Composable
fun BalanceCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Positive change
            BalanceCard(
                balanceAmount = "$17,200",
                priceChangeText = "+$233 +3%",
                isPriceChangePositive = true,
                selectedTimePeriod = TimePeriod.TWENTY_FOUR_HOURS,
                isDropdownExpanded = false,
                onToggleDropdown = { },
                onTimePeriodSelected = { },
            )

            // Negative change
            BalanceCard(
                balanceAmount = "$12,850",
                priceChangeText = "-$150 -1.2%",
                isPriceChangePositive = false,
                selectedTimePeriod = TimePeriod.SEVEN_DAYS,
                isDropdownExpanded = false,
                onToggleDropdown = { },
                onTimePeriodSelected = { },
            )

            // With dropdown expanded
            BalanceCard(
                balanceAmount = "$17,200",
                priceChangeText = "+$233 +3%",
                isPriceChangePositive = true,
                selectedTimePeriod = TimePeriod.TWENTY_FOUR_HOURS,
                isDropdownExpanded = true,
                onToggleDropdown = { },
                onTimePeriodSelected = { },
            )
        }
    }
}
*/
