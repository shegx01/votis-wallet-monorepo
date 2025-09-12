package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.MockOhlcvGenerator
import finance.votis.wallet.core.domain.model.OHLCVData
import finance.votis.wallet.core.domain.model.TimePeriod
import kotlinx.coroutines.launch

/**
 * OHLCV Chart state for managing data loading
 */
sealed class OHLCVChartState {
    data object Loading : OHLCVChartState()

    data class Success(
        val data: List<OHLCVData>,
    ) : OHLCVChartState()

    data class Error(
        val message: String,
    ) : OHLCVChartState()
}

/**
 * OHLCV Chart component for displaying token price data
 * Now uses real candlestick chart with mock data generation
 */
@Composable
fun OHLCVChart(
    tokenSymbol: String,
    timePeriod: TimePeriod,
    modifier: Modifier = Modifier,
) {
    var chartState by remember { mutableStateOf<OHLCVChartState>(OHLCVChartState.Loading) }
    val scope = rememberCoroutineScope()

    // Generate mock data when token or period changes
    LaunchedEffect(tokenSymbol, timePeriod) {
        chartState = OHLCVChartState.Loading
        scope.launch {
            try {
                // Simulate network delay for realistic loading state
                kotlinx.coroutines.delay(500)

                val mockData =
                    MockOhlcvGenerator.generateSeries(
                        tokenSymbol = tokenSymbol,
                        period = timePeriod,
                    )

                chartState = OHLCVChartState.Success(mockData)
            } catch (e: Exception) {
                chartState = OHLCVChartState.Error("Failed to load chart data")
            }
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val state = chartState) {
            is OHLCVChartState.Loading -> {
                LoadingChart(tokenSymbol = tokenSymbol, timePeriod = timePeriod)
            }

            is OHLCVChartState.Success -> {
                ProfessionalCandlestickChart(
                    ohlcvData = state.data,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is OHLCVChartState.Error -> {
                ErrorChart(
                    message = state.message,
                    onRetry = {
                        chartState = OHLCVChartState.Loading
                        scope.launch {
                            try {
                                val mockData =
                                    MockOhlcvGenerator.generateSeries(
                                        tokenSymbol = tokenSymbol,
                                        period = timePeriod,
                                    )
                                chartState = OHLCVChartState.Success(mockData)
                            } catch (e: Exception) {
                                chartState = OHLCVChartState.Error("Failed to load chart data")
                            }
                        }
                    },
                )
            }
        }
    }
}

/**
 * Loading state for chart
 */
@Composable
private fun LoadingChart(
    tokenSymbol: String,
    timePeriod: TimePeriod,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Loading $tokenSymbol Chart (${timePeriod.displayName})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Error state for chart
 */
@Composable
private fun ErrorChart(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().height(280.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            androidx.compose.material3.TextButton(
                onClick = onRetry,
            ) {
                Text("Retry")
            }
        }
    }
}
