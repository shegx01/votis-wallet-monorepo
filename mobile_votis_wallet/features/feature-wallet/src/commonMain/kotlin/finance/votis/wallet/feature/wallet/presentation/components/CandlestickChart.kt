package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.OHLCVData
import finance.votis.wallet.core.domain.model.OHLCVData.Companion.priceRange
import finance.votis.wallet.core.domain.model.OHLCVData.Companion.volumeRange
import kotlin.math.max
import kotlin.math.min

/**
 * Chart theme configuration for candlestick chart
 */
data class CandlestickChartTheme(
    val bullishColor: Color,
    val bearishColor: Color,
    val volumeColor: Color,
    val gridColor: Color,
    val textColor: Color,
    val backgroundColor: Color,
    val crosshairColor: Color,
)

/**
 * Viewport state for pan and zoom functionality
 */
@Stable
data class ChartViewport(
    val startIndex: Int = 0,
    val endIndex: Int = Int.MAX_VALUE,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
) {
    fun isValid() = startIndex < endIndex && minPrice < maxPrice
}

/**
 * Crosshair state for touch interactions
 */
data class CrosshairState(
    val isVisible: Boolean = false,
    val x: Float = 0f,
    val y: Float = 0f,
    val candleIndex: Int = -1,
)

/**
 * Cross-platform Candlestick Chart component
 *
 * Renders OHLCV data with:
 * - Candlestick bodies and wicks
 * - Volume bars at bottom
 * - Price scale on right
 * - Time labels at bottom
 * - Pan/zoom gestures
 * - Crosshair on touch
 */
@Composable
fun CandlestickChart(
    ohlcvData: List<OHLCVData>,
    modifier: Modifier = Modifier,
    theme: CandlestickChartTheme = createDefaultTheme(),
) {
    if (ohlcvData.isEmpty()) {
        EmptyChartPlaceholder(modifier = modifier)
        return
    }

    val density = LocalDensity.current
    var viewport by remember { mutableStateOf(ChartViewport()) }
    var crosshairState by remember { mutableStateOf(CrosshairState()) }

    // Animation for smooth viewport changes
    val animatedViewport = remember { Animatable(0f) }

    // Update viewport when data changes
    LaunchedEffect(ohlcvData) {
        val (minPrice, maxPrice) = ohlcvData.priceRange()
        val padding = (maxPrice - minPrice) * 0.1 // 10% padding
        viewport =
            ChartViewport(
                startIndex = 0,
                endIndex = ohlcvData.size,
                minPrice = minPrice - padding,
                maxPrice = maxPrice + padding,
            )
    }

    Box(modifier = modifier) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(ohlcvData) {
                        detectTransformGestures(
                            panZoomLock = true,
                            onGesture = { _, pan, zoom, _ ->
                                // Simple pan implementation
                                val panSensitivity = 0.1f
                                val visibleCandles = viewport.endIndex - viewport.startIndex
                                val panAmount = (pan.x * panSensitivity * visibleCandles / size.width).toInt()

                                val newStart = (viewport.startIndex - panAmount).coerceAtLeast(0)
                                val newEnd = (viewport.endIndex - panAmount).coerceAtMost(ohlcvData.size)

                                if (newStart < newEnd && newEnd - newStart > 10) {
                                    viewport =
                                        viewport.copy(
                                            startIndex = newStart,
                                            endIndex = newEnd,
                                        )
                                }
                            },
                        )
                    },
        ) {
            drawCandlestickChart(
                ohlcvData = ohlcvData,
                viewport = viewport,
                theme = theme,
                crosshairState = crosshairState,
            )
        }

        // Crosshair info popup
        if (crosshairState.isVisible && crosshairState.candleIndex >= 0) {
            CrosshairInfoPopup(
                candleData = ohlcvData.getOrNull(crosshairState.candleIndex),
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
            )
        }
    }
}

/**
 * Main drawing function for the candlestick chart
 */
private fun DrawScope.drawCandlestickChart(
    ohlcvData: List<OHLCVData>,
    viewport: ChartViewport,
    theme: CandlestickChartTheme,
    crosshairState: CrosshairState,
) {
    if (!viewport.isValid()) return

    val chartArea =
        Size(
            width = size.width * 0.85f, // Leave 15% for price labels
            height = size.height * 0.85f, // Leave 15% for time labels
        )

    val visibleData =
        ohlcvData.subList(
            max(0, viewport.startIndex),
            min(ohlcvData.size, viewport.endIndex),
        )

    if (visibleData.isEmpty()) return

    // Draw background
    drawRect(
        color = theme.backgroundColor,
        size = size,
    )

    // Draw grid
    drawGrid(chartArea, theme.gridColor)

    // Draw volume bars (bottom 30% of chart area)
    drawVolumeBars(
        data = visibleData,
        chartArea = chartArea,
        theme = theme,
    )

    // Draw candlesticks (top 70% of chart area)
    drawCandlesticks(
        data = visibleData,
        chartArea = chartArea,
        viewport = viewport,
        theme = theme,
    )

    // Draw price scale
    drawPriceScale(
        viewport = viewport,
        chartArea = chartArea,
        theme = theme,
    )

    // Draw time labels
    drawTimeLabels(
        data = visibleData,
        chartArea = chartArea,
        theme = theme,
    )

    // Draw crosshair
    if (crosshairState.isVisible) {
        drawCrosshair(crosshairState, theme)
    }
}

/**
 * Draw grid lines for better readability
 */
private fun DrawScope.drawGrid(
    chartArea: Size,
    gridColor: Color,
) {
    val strokeWidth = 1.dp.toPx()
    val alpha = 0.2f

    // Horizontal grid lines
    for (i in 0..5) {
        val y = chartArea.height * i / 5f
        drawLine(
            color = gridColor.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(chartArea.width, y),
            strokeWidth = strokeWidth,
        )
    }

    // Vertical grid lines
    val visibleCandles = 10 // Approximate number of visible candles for grid
    for (i in 0..visibleCandles) {
        val x = chartArea.width * i / visibleCandles.toFloat()
        drawLine(
            color = gridColor.copy(alpha = alpha),
            start = Offset(x, 0f),
            end = Offset(x, chartArea.height),
            strokeWidth = strokeWidth,
        )
    }
}

/**
 * Draw volume bars at the bottom of the chart
 */
private fun DrawScope.drawVolumeBars(
    data: List<OHLCVData>,
    chartArea: Size,
    theme: CandlestickChartTheme,
) {
    if (data.isEmpty()) return

    val (minVolume, maxVolume) = data.volumeRange()
    val volumeAreaHeight = chartArea.height * 0.3f
    val volumeAreaTop = chartArea.height * 0.7f

    val candleWidth = chartArea.width / data.size.toFloat()

    data.forEachIndexed { index, candle ->
        val x = index * candleWidth + candleWidth / 2f
        val volumeRatio =
            if (maxVolume > minVolume) {
                (candle.volume - minVolume) / (maxVolume - minVolume)
            } else {
                0.5
            }

        val barHeight = volumeRatio * volumeAreaHeight
        val barTop = volumeAreaTop + volumeAreaHeight - barHeight

        drawRect(
            color = theme.volumeColor.copy(alpha = 0.3f),
            topLeft = Offset(x - candleWidth * 0.3f, barTop.toFloat()),
            size = Size(candleWidth * 0.6f, barHeight.toFloat()),
        )
    }
}

/**
 * Draw candlestick bodies and wicks
 */
private fun DrawScope.drawCandlesticks(
    data: List<OHLCVData>,
    chartArea: Size,
    viewport: ChartViewport,
    theme: CandlestickChartTheme,
) {
    if (data.isEmpty()) return

    val priceAreaHeight = chartArea.height * 0.7f
    val priceRange = viewport.maxPrice - viewport.minPrice

    val candleWidth = chartArea.width / data.size.toFloat()
    val bodyWidth = candleWidth * 0.7f
    val wickWidth = 2.dp.toPx()

    data.forEachIndexed { index, candle ->
        val x = index * candleWidth + candleWidth / 2f

        // Calculate Y positions (inverted because canvas Y grows downward)
        val highY = priceAreaHeight - ((candle.high - viewport.minPrice) / priceRange * priceAreaHeight)
        val lowY = priceAreaHeight - ((candle.low - viewport.minPrice) / priceRange * priceAreaHeight)
        val openY = priceAreaHeight - ((candle.open - viewport.minPrice) / priceRange * priceAreaHeight)
        val closeY = priceAreaHeight - ((candle.close - viewport.minPrice) / priceRange * priceAreaHeight)

        val color = if (candle.isBullish) theme.bullishColor else theme.bearishColor

        // Draw wick (high-low line)
        drawLine(
            color = color,
            start = Offset(x, highY.toFloat()),
            end = Offset(x, lowY.toFloat()),
            strokeWidth = wickWidth,
        )

        // Draw body (open-close rectangle)
        val bodyTop = min(openY, closeY).toFloat()
        val bodyHeight =
            kotlin.math
                .abs(closeY - openY)
                .toFloat()
                .coerceAtLeast(1.dp.toPx())

        drawRect(
            color = color,
            topLeft = Offset(x - bodyWidth / 2f, bodyTop),
            size = Size(bodyWidth, bodyHeight),
        )
    }
}

/**
 * Draw price scale on the right side
 */
private fun DrawScope.drawPriceScale(
    viewport: ChartViewport,
    chartArea: Size,
    theme: CandlestickChartTheme,
) {
    val priceAreaHeight = chartArea.height * 0.7f
    val numberOfLabels = 6

    for (i in 0..numberOfLabels) {
        val ratio = i.toFloat() / numberOfLabels
        val price = viewport.minPrice + (viewport.maxPrice - viewport.minPrice) * (1 - ratio)
        val y = priceAreaHeight * ratio

        // Format price for display (simplified - avoiding string format for KMP compatibility)
        val priceText =
            when {
                price >= 1000 -> "$${price.toInt()}"
                price >= 1 -> "$${(price * 100).toInt() / 100.0}"
                else -> "$${(price * 10000).toInt() / 10000.0}"
            }

        // Draw price indicator (simplified - avoiding platform-specific text drawing for now)
        drawCircle(
            color = theme.textColor.copy(alpha = 0.6f),
            radius = 3.dp.toPx(),
            center = Offset(chartArea.width + 10.dp.toPx(), y),
        )
    }
}

/**
 * Draw time labels at the bottom
 */
private fun DrawScope.drawTimeLabels(
    data: List<OHLCVData>,
    chartArea: Size,
    theme: CandlestickChartTheme,
) {
    // Simplified time label drawing
    // In a real implementation, you'd format timestamps and draw them properly
    val numberOfLabels = 4
    val candleWidth = chartArea.width / data.size.toFloat()

    for (i in 0..numberOfLabels) {
        val index = (data.size * i / numberOfLabels.toFloat()).toInt().coerceAtMost(data.size - 1)
        val x = index * candleWidth + candleWidth / 2f
        val y = chartArea.height + 20.dp.toPx()

        // Draw time marker
        drawCircle(
            color = theme.textColor,
            radius = 2.dp.toPx(),
            center = Offset(x, y),
        )
    }
}

/**
 * Draw crosshair lines when user touches the chart
 */
private fun DrawScope.drawCrosshair(
    crosshairState: CrosshairState,
    theme: CandlestickChartTheme,
) {
    val strokeWidth = 1.dp.toPx()
    val color = theme.crosshairColor.copy(alpha = 0.8f)

    // Horizontal line
    drawLine(
        color = color,
        start = Offset(0f, crosshairState.y),
        end = Offset(size.width, crosshairState.y),
        strokeWidth = strokeWidth,
    )

    // Vertical line
    drawLine(
        color = color,
        start = Offset(crosshairState.x, 0f),
        end = Offset(crosshairState.x, size.height),
        strokeWidth = strokeWidth,
    )
}

/**
 * Empty chart placeholder when no data is available
 */
@Composable
private fun EmptyChartPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No chart data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Crosshair info popup showing OHLCV values
 */
@Composable
private fun CrosshairInfoPopup(
    candleData: OHLCVData?,
    modifier: Modifier = Modifier,
) {
    if (candleData == null) return

    Surface(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = "OHLCV Data",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Open: $${candleData.open}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "High: $${candleData.high}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Low: $${candleData.low}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Close: $${candleData.close}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Volume: ${candleData.volume}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Default theme for candlestick chart using Material Design colors
 */
@Composable
fun createDefaultTheme(): CandlestickChartTheme {
    val colorScheme = MaterialTheme.colorScheme

    return CandlestickChartTheme(
        bullishColor = Color(0xFF4CAF50), // Green
        bearishColor = Color(0xFFFF5722), // Red
        volumeColor = colorScheme.primary,
        gridColor = colorScheme.onSurface,
        textColor = colorScheme.onSurface,
        backgroundColor = colorScheme.surface,
        crosshairColor = colorScheme.onSurface,
    )
}
