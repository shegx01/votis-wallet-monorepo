package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.OHLCVData
import finance.votis.wallet.core.domain.model.OHLCVData.Companion.priceRange
import finance.votis.wallet.core.domain.model.OHLCVData.Companion.volumeRange
import kotlinx.datetime.Instant
import kotlin.math.*

/**
 * Professional chart theme with enhanced visual design
 */
data class ProfessionalChartTheme(
    val bullishColor: Color,
    val bearishColor: Color,
    val bullishGradient: List<Color>,
    val bearishGradient: List<Color>,
    val volumeBullishColor: Color,
    val volumeBearishColor: Color,
    val gridColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val textPrimaryColor: Color,
    val textSecondaryColor: Color,
    val crosshairColor: Color,
    val crosshairBackgroundColor: Color,
    val shadowColor: Color,
) {
    companion object {
        @Composable
        fun defaultTheme(): ProfessionalChartTheme {
            val colorScheme = MaterialTheme.colorScheme

            // Use Material3 system colors for automatic theme adaptation
            return ProfessionalChartTheme(
                bullishColor = Color(0xFF4CAF50),
                bearishColor = Color(0xFFFF5722),
                bullishGradient =
                    listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF81C784),
                    ),
                bearishGradient =
                    listOf(
                        Color(0xFFFF5722),
                        Color(0xFFFF8A65),
                    ),
                volumeBullishColor = Color(0xFF4CAF50).copy(alpha = 0.4f),
                volumeBearishColor = Color(0xFFFF5722).copy(alpha = 0.4f),
                gridColor = colorScheme.outline,
                backgroundColor = colorScheme.background,
                surfaceColor = colorScheme.surface,
                textPrimaryColor = colorScheme.onSurface,
                textSecondaryColor = colorScheme.onSurfaceVariant,
                crosshairColor = colorScheme.onSurface,
                crosshairBackgroundColor = colorScheme.inverseSurface,
                shadowColor = Color.Black.copy(alpha = 0.1f),
            )
        }
    }
}

/**
 * Enhanced viewport with smooth animations
 */
@Stable
data class ProfessionalViewport(
    val startIndex: Int = 0,
    val endIndex: Int = Int.MAX_VALUE,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val zoom: Float = 1.0f,
    val pan: Float = 0.0f,
) {
    fun isValid() = startIndex < endIndex && minPrice < maxPrice

    fun getVisibleCandles(totalCandles: Int): IntRange {
        val visible = (totalCandles / zoom).toInt().coerceAtLeast(10)
        val start = (pan * totalCandles).toInt().coerceAtLeast(0)
        val end = (start + visible).coerceAtMost(totalCandles)
        return start until end
    }
}

/**
 * Enhanced crosshair state with smooth animations
 */
@Stable
data class ProfessionalCrosshair(
    val isVisible: Boolean = false,
    val x: Float = 0f,
    val y: Float = 0f,
    val candleIndex: Int = -1,
    val price: Double = 0.0,
    val timestamp: Instant? = null,
    val animatedAlpha: Float = 0f,
)

/**
 * Professional CandlestickChart with advanced visual design and interactions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalCandlestickChart(
    ohlcvData: List<OHLCVData>,
    modifier: Modifier = Modifier,
    theme: ProfessionalChartTheme = ProfessionalChartTheme.defaultTheme(),
    showVolume: Boolean = true,
    showGrid: Boolean = true,
    showCrosshair: Boolean = true,
    animationDuration: Int = 500,
) {
    if (ohlcvData.isEmpty()) {
        EmptyChartState(modifier = modifier, theme = theme)
        return
    }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // State management
    var viewport by remember { mutableStateOf(ProfessionalViewport()) }
    var crosshair by remember { mutableStateOf(ProfessionalCrosshair()) }

    // Animation states
    val chartAnimationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
        label = "chartAnimation",
    )

    val crosshairAlpha by animateFloatAsState(
        targetValue = if (crosshair.isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "crosshairAlpha",
    )

    // Initialize viewport
    LaunchedEffect(ohlcvData) {
        val (minPrice, maxPrice) = ohlcvData.priceRange()
        val priceRange = maxPrice - minPrice

        // Ensure minimum price range to prevent gradient issues
        val safeRange = if (priceRange < 0.01) 0.01 else priceRange
        val padding = safeRange * 0.05 // 5% padding

        viewport =
            ProfessionalViewport(
                startIndex = 0,
                endIndex = ohlcvData.size,
                minPrice = minPrice - padding,
                maxPrice = minPrice + safeRange + padding,
            )
    }

    // Full canvas chart without borders or containers
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(ohlcvData) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (showCrosshair) {
                                    val candleIndex =
                                        calculateCandleIndex(
                                            offset.x,
                                            size.width.toFloat(),
                                            viewport,
                                            ohlcvData.size,
                                        )

                                    if (candleIndex in ohlcvData.indices) {
                                        val candle = ohlcvData[candleIndex]
                                        crosshair =
                                            ProfessionalCrosshair(
                                                isVisible = true,
                                                x = offset.x,
                                                y = offset.y,
                                                candleIndex = candleIndex,
                                                price = candle.close,
                                                timestamp = candle.timestamp,
                                            )
                                    }
                                }
                            },
                        )
                    }.pointerInput(ohlcvData) {
                        detectDragGestures(
                            onDragEnd = {
                                // Fade out crosshair after drag
                                if (crosshair.isVisible) {
                                    crosshair = crosshair.copy(isVisible = false)
                                }
                            },
                        ) { change, dragAmount ->
                            // Pan implementation
                            val panSensitivity = 0.002f
                            val newPan =
                                (viewport.pan - dragAmount.x * panSensitivity)
                                    .coerceIn(0f, 1f - 1f / viewport.zoom)

                            viewport = viewport.copy(pan = newPan)
                        }
                    },
        ) {
            drawFullCanvasChart(
                data = ohlcvData,
                viewport = viewport,
                crosshair = crosshair.copy(animatedAlpha = crosshairAlpha),
                theme = theme,
                showVolume = showVolume,
                showGrid = showGrid,
                animationProgress = chartAnimationProgress,
            )
        }

        // Crosshair info overlay
        if (crosshair.isVisible && crosshair.candleIndex >= 0 && crosshairAlpha > 0f) {
            CrosshairInfoOverlay(
                candleData = ohlcvData.getOrNull(crosshair.candleIndex),
                theme = theme,
                alpha = crosshairAlpha,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
            )
        }
    }
}

/**
 * Full canvas drawing function without axis labels or borders
 */
private fun DrawScope.drawFullCanvasChart(
    data: List<OHLCVData>,
    viewport: ProfessionalViewport,
    crosshair: ProfessionalCrosshair,
    theme: ProfessionalChartTheme,
    showVolume: Boolean,
    showGrid: Boolean,
    animationProgress: Float,
) {
    val visibleRange = viewport.getVisibleCandles(data.size)
    val visibleData =
        data.subList(
            visibleRange.first.coerceAtLeast(0),
            visibleRange.last.coerceAtMost(data.size),
        )

    if (visibleData.isEmpty()) return

    // Use full canvas area
    val chartArea = size
    val priceArea =
        Size(
            width = chartArea.width,
            height = if (showVolume) chartArea.height * 0.75f else chartArea.height,
        )

    // Draw minimal grid if enabled
    if (showGrid) {
        drawMinimalGrid(theme, chartArea)
    }

    // Draw volume bars in lower portion
    if (showVolume) {
        drawFullCanvasVolume(
            data = visibleData,
            chartArea = chartArea,
            priceArea = priceArea,
            theme = theme,
            animationProgress = animationProgress,
        )
    }

    // Draw candlesticks using full price area
    drawFullCanvasCandlesticks(
        data = visibleData,
        priceArea = priceArea,
        viewport = viewport,
        theme = theme,
        animationProgress = animationProgress,
    )

    // Draw minimal crosshair without labels
    if (crosshair.isVisible && crosshair.animatedAlpha > 0f) {
        drawMinimalCrosshair(
            crosshair = crosshair,
            theme = theme,
        )
    }
}

/**
 * Draw minimal grid for full canvas
 */
private fun DrawScope.drawMinimalGrid(
    theme: ProfessionalChartTheme,
    chartArea: Size,
) {
    val strokeWidth = 0.3.dp.toPx()
    val gridAlpha = 0.1f

    // Only draw major horizontal grid lines
    for (i in 1..4) {
        val y = chartArea.height * i / 5f
        drawLine(
            color = theme.gridColor.copy(alpha = gridAlpha),
            start = Offset(0f, y),
            end = Offset(chartArea.width, y),
            strokeWidth = strokeWidth,
            pathEffect =
                PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8.dp.toPx(), 4.dp.toPx()),
                ),
        )
    }
}

/**
 * Draw volume bars using full canvas width
 */
private fun DrawScope.drawFullCanvasVolume(
    data: List<OHLCVData>,
    chartArea: Size,
    priceArea: Size,
    theme: ProfessionalChartTheme,
    animationProgress: Float,
) {
    if (data.isEmpty()) return

    val (minVolume, maxVolume) = data.volumeRange()
    val volumeAreaHeight = chartArea.height - priceArea.height - 10.dp.toPx()
    val volumeAreaTop = priceArea.height + 5.dp.toPx()

    val candleWidth = chartArea.width / data.size.toFloat()
    val barWidth = candleWidth * 0.9f // Use more width in full canvas

    data.forEachIndexed { index, candle ->
        val x = index * candleWidth + candleWidth / 2f
        val volumeRatio =
            if (maxVolume > minVolume) {
                (candle.volume - minVolume) / (maxVolume - minVolume)
            } else {
                0.5
            }

        val barHeight = volumeRatio * volumeAreaHeight * animationProgress
        val barTop = volumeAreaTop + volumeAreaHeight - barHeight

        val barColor =
            if (candle.isBullish) {
                theme.volumeBullishColor
            } else {
                theme.volumeBearishColor
            }

        // Simple volume bar without rounded corners for performance
        drawRect(
            color = barColor,
            topLeft = Offset(x - barWidth / 2f, barTop.toFloat()),
            size = Size(barWidth, barHeight.toFloat()),
        )
    }
}

/**
 * Draw candlesticks using full canvas space
 */
private fun DrawScope.drawFullCanvasCandlesticks(
    data: List<OHLCVData>,
    priceArea: Size,
    viewport: ProfessionalViewport,
    theme: ProfessionalChartTheme,
    animationProgress: Float,
) {
    if (data.isEmpty()) return

    val priceRange = viewport.maxPrice - viewport.minPrice
    // Safety check for zero or invalid price range
    if (priceRange <= 0) return

    val candleWidth = priceArea.width / data.size.toFloat()
    val bodyWidth = candleWidth * 0.8f // Use more width in full canvas
    val wickWidth = max(1.5.dp.toPx(), candleWidth * 0.15f)

    data.forEachIndexed { index, candle ->
        val x = index * candleWidth + candleWidth / 2f

        // Calculate Y positions
        val highY = priceArea.height - ((candle.high - viewport.minPrice) / priceRange * priceArea.height)
        val lowY = priceArea.height - ((candle.low - viewport.minPrice) / priceRange * priceArea.height)
        val openY = priceArea.height - ((candle.open - viewport.minPrice) / priceRange * priceArea.height)
        val closeY = priceArea.height - ((candle.close - viewport.minPrice) / priceRange * priceArea.height)

        val isBullish = candle.isBullish
        val mainColor = if (isBullish) theme.bullishColor else theme.bearishColor

        // Draw wick (simplified without gradient for performance)
        val wickStartY = highY.toFloat()
        val wickEndY = lowY.toFloat()

        if (abs(wickEndY - wickStartY) > 1f) {
            drawLine(
                color = mainColor.copy(alpha = animationProgress * 0.8f),
                start = Offset(x, wickStartY),
                end = Offset(x, wickEndY),
                strokeWidth = wickWidth,
                cap = StrokeCap.Round,
            )
        }

        // Draw body (simplified for performance)
        val bodyTop = min(openY, closeY).toFloat()
        val bodyHeight = abs(closeY - openY).toFloat().coerceAtLeast(1.dp.toPx())
        val animatedBodyHeight = bodyHeight * animationProgress

        if (animatedBodyHeight > 1f) {
            drawRect(
                color = mainColor.copy(alpha = animationProgress),
                topLeft = Offset(x - bodyWidth / 2f, bodyTop + bodyHeight - animatedBodyHeight),
                size = Size(bodyWidth, animatedBodyHeight),
            )
        }
    }
}

/**
 * Draw minimal crosshair without labels
 */
private fun DrawScope.drawMinimalCrosshair(
    crosshair: ProfessionalCrosshair,
    theme: ProfessionalChartTheme,
) {
    val alpha = crosshair.animatedAlpha * 0.7f
    val strokeWidth = 1.dp.toPx()

    // Horizontal line
    drawLine(
        color = theme.crosshairColor.copy(alpha = alpha),
        start = Offset(0f, crosshair.y),
        end = Offset(size.width, crosshair.y),
        strokeWidth = strokeWidth,
        pathEffect =
            PathEffect.dashPathEffect(
                intervals = floatArrayOf(6.dp.toPx(), 3.dp.toPx()),
            ),
    )

    // Vertical line
    drawLine(
        color = theme.crosshairColor.copy(alpha = alpha),
        start = Offset(crosshair.x, 0f),
        end = Offset(crosshair.x, size.height),
        strokeWidth = strokeWidth,
        pathEffect =
            PathEffect.dashPathEffect(
                intervals = floatArrayOf(6.dp.toPx(), 3.dp.toPx()),
            ),
    )

    // Center dot
    drawCircle(
        color = theme.crosshairColor.copy(alpha = alpha * 1.5f),
        radius = 3.dp.toPx(),
        center = Offset(crosshair.x, crosshair.y),
    )
}

/**
 * Professional crosshair info overlay with improved theming
 */
@Composable
private fun CrosshairInfoOverlay(
    candleData: OHLCVData?,
    theme: ProfessionalChartTheme,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (candleData == null || alpha <= 0f) return

    // Use Material3 colors for better light/dark mode support
    val backgroundColor =
        MaterialTheme.colorScheme.surface.copy(
            alpha =
                if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
                    0.95f // Light mode - more opaque
                } else {
                    0.9f // Dark mode - slightly less opaque
                },
        )

    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Card(
        modifier =
            modifier
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f),
                ),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
        shape = RoundedCornerShape(8.dp), // Slightly smaller radius to match images
        border =
            BorderStroke(
                width = 1.dp,
                color = borderColor,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Remove default elevation
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // OHLCV header
            Text(
                text = "OHLCV",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            // OHLC values in 2x2 grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    ProfessionalInfoItem("O", formatPrice(candleData.open))
                    ProfessionalInfoItem("H", formatPrice(candleData.high))
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    ProfessionalInfoItem("L", formatPrice(candleData.low))
                    ProfessionalInfoItem("C", formatPrice(candleData.close))
                }
            }

            // Volume on separate line
            ProfessionalInfoItem("Vol", formatVolume(candleData.volume))
        }
    }
}

@Composable
private fun ProfessionalInfoItem(
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Normal,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Empty state for when no data is available
 */
@Composable
private fun EmptyChartState(
    modifier: Modifier = Modifier,
    theme: ProfessionalChartTheme,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "📊",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "No chart data available",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.textSecondaryColor,
            )
        }
    }
}

/**
 * Helper functions for formatting
 */
private fun formatPrice(price: Double): String =
    when {
        price >= 1_000_000 -> {
            val value = (price / 1_000_000 * 10).toInt() / 10.0
            "$${value}M"
        }
        price >= 1_000 -> {
            val value = (price / 1_000 * 10).toInt() / 10.0
            "$${value}K"
        }
        price >= 100 -> "$${price.toInt()}"
        price >= 1 -> {
            val value = (price * 100).toInt() / 100.0
            "$$value"
        }
        else -> {
            val value = (price * 10000).toInt() / 10000.0
            "$$value"
        }
    }

private fun formatVolume(volume: Double): String =
    when {
        volume >= 1_000_000_000 -> {
            val value = (volume / 1_000_000_000 * 10).toInt() / 10.0
            "${value}B"
        }
        volume >= 1_000_000 -> {
            val value = (volume / 1_000_000 * 10).toInt() / 10.0
            "${value}M"
        }
        volume >= 1_000 -> {
            val value = (volume / 1_000 * 10).toInt() / 10.0
            "${value}K"
        }
        else -> volume.toInt().toString()
    }

private fun formatTime(timestamp: Instant): String {
    // Simplified time formatting - in real app you'd use proper date formatting
    val epochSeconds = timestamp.epochSeconds
    val hours = (epochSeconds / 3600) % 24
    val hourStr = if (hours < 10) "0$hours" else hours.toString()
    return "$hourStr:00"
}

private fun calculateCandleIndex(
    touchX: Float,
    chartWidth: Float,
    viewport: ProfessionalViewport,
    totalCandles: Int,
): Int {
    val visibleRange = viewport.getVisibleCandles(totalCandles)
    val candleWidth = chartWidth / visibleRange.count()
    val index = (touchX / candleWidth).toInt() + visibleRange.first
    return index.coerceIn(0, totalCandles - 1)
}
