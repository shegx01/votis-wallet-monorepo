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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Data class representing performance metrics for a time period
 */
data class PerformanceData(
    val period: String,
    val percentage: String,
    val isPositive: Boolean,
)

/**
 * Performance metrics component showing time period performance table
 * Matches the design with proper color coding for positive/negative changes
 */
@Composable
fun PerformanceMetrics(
    tokenSymbol: String,
    modifier: Modifier = Modifier,
) {
    val performanceData = getMockPerformanceData(tokenSymbol)

    Column(
        modifier =
            modifier
                .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            performanceData.take(5).forEach { data ->
                PerformanceItem(
                    data = data,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Individual performance item component
 */
@Composable
private fun PerformanceItem(
    data: PerformanceData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = data.period,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = data.percentage,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color =
                if (data.isPositive) {
                    Color(0xFF4CAF50) // Green for positive
                } else {
                    Color(0xFFF44336) // Red for negative
                },
        )
    }
}

/**
 * Generate mock performance data for different tokens
 * In a real app, this would come from an API
 */
private fun getMockPerformanceData(tokenSymbol: String): List<PerformanceData> =
    when (tokenSymbol) {
        "BTC" ->
            listOf(
                PerformanceData("24 hours", "+3,66%", true),
                PerformanceData("7 days", "+14,35%", true),
                PerformanceData("30 days", "-13,93%", false),
                PerformanceData("1 year", "+31,04%", true),
                PerformanceData("YTD", "-87,21%", false),
            )
        "ETH" ->
            listOf(
                PerformanceData("24 hours", "+2,15%", true),
                PerformanceData("7 days", "+8,42%", true),
                PerformanceData("30 days", "-5,67%", false),
                PerformanceData("1 year", "+45,23%", true),
                PerformanceData("YTD", "-12,34%", false),
            )
        "SOL" ->
            listOf(
                PerformanceData("24 hours", "+4,21%", true),
                PerformanceData("7 days", "+18,65%", true),
                PerformanceData("30 days", "+3,45%", true),
                PerformanceData("1 year", "+125,67%", true),
                PerformanceData("YTD", "+78,90%", true),
            )
        else ->
            listOf(
                PerformanceData("24 hours", "0,00%", true),
                PerformanceData("7 days", "0,00%", true),
                PerformanceData("30 days", "0,00%", true),
                PerformanceData("1 year", "0,00%", true),
                PerformanceData("YTD", "0,00%", true),
            )
    }
