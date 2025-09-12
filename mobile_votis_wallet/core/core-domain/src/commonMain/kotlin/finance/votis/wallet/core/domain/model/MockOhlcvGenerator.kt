package finance.votis.wallet.core.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.*
import kotlin.random.Random

/**
 * Mock OHLCV data generator for testing and development purposes.
 * Generates realistic candlestick data with proper price relationships.
 */
object MockOhlcvGenerator {
    /**
     * Generate OHLCV data series for a given token and time period
     *
     * @param tokenSymbol The token symbol (affects base price and volatility)
     * @param period The time period to generate data for
     * @param seed Random seed for deterministic results in tests (optional)
     * @return List of OHLCV data points
     */
    fun generateSeries(
        tokenSymbol: String,
        period: TimePeriod,
        seed: Long? = null,
    ): List<OHLCVData> {
        val random = seed?.let { Random(it) } ?: Random.Default
        val basePrice = getBasePrice(tokenSymbol)
        val volatility = getVolatility(tokenSymbol)

        val (candleCount, intervalMinutes) =
            when (period) {
                TimePeriod.ONE_HOUR -> 60 to 1 // 60 candles, 1 minute each
                TimePeriod.TWENTY_FOUR_HOURS -> 144 to 10 // 144 candles, 10 minutes each
                TimePeriod.SEVEN_DAYS -> 168 to 60 // 168 candles, 1 hour each
                TimePeriod.THIRTY_DAYS -> 120 to 360 // 120 candles, 6 hours each
                TimePeriod.ONE_YEAR -> 365 to 1440 // 365 candles, 1 day each
            }

        val currentTime = Clock.System.now()
        val candles = mutableListOf<OHLCVData>()

        // Start with base price and generate walk
        var currentPrice = basePrice

        for (i in 0 until candleCount) {
            val timestamp =
                currentTime.minus(
                    kotlin.time.Duration.parse("${(candleCount - i) * intervalMinutes}m"),
                )

            // Generate price movement (random walk with trend)
            val trendFactor = getTrendFactor(tokenSymbol, i, candleCount)
            val priceChange = random.nextGaussian() * volatility * basePrice * 0.01 + trendFactor
            val newClose = (currentPrice + priceChange).coerceAtLeast(basePrice * 0.1)

            // Generate OHLC values around the close price
            val open = currentPrice
            val close = newClose

            // High/Low generation with realistic relationships
            val range = abs(close - open) + (random.nextDouble() * volatility * basePrice * 0.005)
            val high = max(open, close) + (random.nextDouble() * range * 0.5)
            val low = min(open, close) - (random.nextDouble() * range * 0.3)

            // Generate volume (higher volume for larger price movements)
            val baseVolume = getBaseVolume(tokenSymbol)
            val priceMovement = abs(close - open) / open
            val volumeMultiplier = 1.0 + (priceMovement * 2.0) + (random.nextGaussian() * 0.3)
            val volume = (baseVolume * volumeMultiplier.coerceAtLeast(0.1)).coerceAtLeast(0.0)

            candles.add(
                OHLCVData(
                    timestamp = timestamp,
                    open = open,
                    high = high.coerceAtLeast(max(open, close)),
                    low = low.coerceAtMost(min(open, close)),
                    close = close,
                    volume = volume,
                ),
            )

            currentPrice = close
        }

        return candles.sortedBy { it.timestamp }
    }

    /**
     * Get base price for different tokens (in USD)
     */
    private fun getBasePrice(tokenSymbol: String): Double =
        when (tokenSymbol) {
            "BTC" -> 65000.0
            "ETH" -> 2800.0
            "SOL" -> 140.0
            "USDT", "USDC" -> 1.0
            else -> 100.0
        }

    /**
     * Get volatility factor for different tokens (0.0 to 1.0, higher = more volatile)
     */
    private fun getVolatility(tokenSymbol: String): Double =
        when (tokenSymbol) {
            "BTC" -> 0.3
            "ETH" -> 0.4
            "SOL" -> 0.6
            "USDT", "USDC" -> 0.05
            else -> 0.5
        }

    /**
     * Get base trading volume for different tokens
     */
    private fun getBaseVolume(tokenSymbol: String): Double =
        when (tokenSymbol) {
            "BTC" -> 1000000.0
            "ETH" -> 2000000.0
            "SOL" -> 500000.0
            "USDT", "USDC" -> 10000000.0
            else -> 100000.0
        }

    /**
     * Generate a trend factor to add some directional bias to price movements
     * This creates more realistic looking charts with periods of growth/decline
     */
    private fun getTrendFactor(
        tokenSymbol: String,
        currentIndex: Int,
        totalCandles: Int,
    ): Double {
        val progress = currentIndex.toDouble() / totalCandles

        // Create different trend patterns based on token
        return when (tokenSymbol) {
            "BTC" -> {
                // Bitcoin: gradual upward trend with some cycles
                sin(progress * PI * 2.5) * 0.001 + progress * 0.002
            }
            "ETH" -> {
                // Ethereum: more volatile with stronger cycles
                sin(progress * PI * 3.0) * 0.0015 + cos(progress * PI * 1.5) * 0.001
            }
            "SOL" -> {
                // Solana: high growth followed by correction
                if (progress < 0.6) progress * 0.005 else (1.0 - progress) * 0.003
            }
            else -> {
                // Other tokens: modest random trend
                sin(progress * PI * 2.0) * 0.001
            }
        }
    }

    /**
     * Generate a single OHLCV candle for testing purposes
     */
    fun generateSingleCandle(
        timestamp: Instant = Clock.System.now(),
        basePrice: Double = 100.0,
        seed: Long? = null,
    ): OHLCVData {
        val random = seed?.let { Random(it) } ?: Random.Default
        val open = basePrice
        val priceChange = random.nextGaussian() * basePrice * 0.02
        val close = (open + priceChange).coerceAtLeast(basePrice * 0.5)

        val range = abs(close - open) * (1.0 + random.nextDouble())
        val high = max(open, close) + (random.nextDouble() * range * 0.3)
        val low = min(open, close) - (random.nextDouble() * range * 0.2)

        return OHLCVData(
            timestamp = timestamp,
            open = open,
            high = high.coerceAtLeast(max(open, close)),
            low = low.coerceAtMost(min(open, close)),
            close = close,
            volume = random.nextDouble() * 1000000.0,
        )
    }
}

/**
 * Extension function to generate Gaussian random numbers using Box-Muller transform
 * Since kotlin.random.Random doesn't have nextGaussian in common
 */
private fun Random.nextGaussian(): Double {
    // Box-Muller transform to generate normal distribution from uniform
    val u1 = nextDouble()
    val u2 = nextDouble()
    return sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
}
