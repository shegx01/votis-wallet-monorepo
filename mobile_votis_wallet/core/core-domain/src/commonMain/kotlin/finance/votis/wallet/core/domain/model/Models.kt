package finance.votis.wallet.core.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

/**
 * Domain models for the wallet application.
 * These represent the core business entities.
 */

@Serializable
data class User(
    val id: String,
    val organizationId: String,
    val createdAt: Instant,
    val isActive: Boolean = true,
)

@Serializable
data class Wallet(
    val id: String,
    val address: String,
    val name: String,
    val userId: String,
    val createdAt: Instant,
)

@Serializable
data class Balance(
    val totalValue: String, // USD value
    val tokens: List<TokenBalance>,
    val lastUpdated: Instant,
) {
    companion object {
        val ZERO =
            Balance(
                totalValue = "$0.00",
                tokens = emptyList(),
                lastUpdated = Instant.DISTANT_PAST,
            )
    }

    fun isStale(): Boolean {
        val now = Clock.System.now()
        val fiveMinutesAgo = now.minus(5.minutes)
        return lastUpdated < fiveMinutesAgo
    }
}

@Serializable
data class TokenBalance(
    val token: Token,
    val amount: String,
    val usdValue: String,
)

@Serializable
data class Token(
    val symbol: String,
    val name: String,
    val decimals: Int,
    val contractAddress: String? = null,
    val logoUrl: String? = null,
)

@Serializable
data class Nft(
    val id: String,
    val name: String,
    val imageUrl: String,
    val collection: NftCollection,
    val description: String? = null,
    val contractAddress: String,
    val tokenId: String,
    val tokenStandard: NftTokenStandard = NftTokenStandard.ERC721,
    val traits: List<NftTrait> = emptyList(),
    val lastSale: NftSale? = null,
)

@Serializable
data class NftCollection(
    val id: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val floorPrice: String? = null,
    val totalSupply: Long? = null,
    val creatorAddress: String? = null,
)

@Serializable
data class NftTrait(
    val traitType: String,
    val value: String,
    val displayType: String? = null,
)

@Serializable
data class NftSale(
    val price: String,
    val currency: String,
    val timestamp: Instant,
    val marketplace: String? = null,
)

@Serializable
enum class NftTokenStandard {
    ERC721,
    ERC1155,
}

@Serializable
enum class Network(
    val displayName: String,
    val chainId: Long,
    val symbol: String,
    val explorerUrl: String,
    val rpcUrl: String,
) {
    EVM_CHAINS(
        displayName = "EVM Chains",
        chainId = 1L,
        symbol = "ETH",
        explorerUrl = "https://etherscan.io",
        rpcUrl = "https://eth-mainnet.g.alchemy.com/v2",
    ),
    BITCOIN(
        displayName = "Bitcoin",
        chainId = 137L,
        symbol = "MATIC",
        explorerUrl = "https://polygonscan.com",
        rpcUrl = "https://polygon-rpc.com",
    ),
    SOLANA(
        displayName = "Solana",
        chainId = 56L,
        symbol = "BNB",
        explorerUrl = "https://bscscan.com",
        rpcUrl = "https://bsc-dataseed.binance.org",
    ),
    TRON(
        displayName = "Tron",
        chainId = 42L,
        symbol = "TRX",
        explorerUrl = "https://tronscan.io",
        rpcUrl = "https://api.trongrid.io",
    ),
    LITECOIN(
        displayName = "Litecoin",
        chainId = 2020L,
        symbol = "LTC",
        explorerUrl = "https://blockchair.com/litecoin",
        rpcUrl = "https://litecoin-rpc.vercel.app",
    ),
}

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val amount: String,
    val token: Token,
    val fromAddress: String,
    val toAddress: String,
    val timestamp: Instant,
    val txHash: String? = null,
    val gasUsed: String? = null,
    val gasFee: String? = null,
)

@Serializable
enum class TransactionType {
    SEND,
    RECEIVE,
    SWAP,
}

@Serializable
enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED,
}

@Serializable
data class TransactionResult(
    val transactionId: String,
    val txHash: String?,
    val status: TransactionStatus,
    val estimatedConfirmationTime: String? = null,
)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
    val userId: String,
    val walletId: String,
)

@Serializable
data class ContactUser(
    val id: String,
    val username: String,
    val walletAddress: String,
    val avatarUrl: String? = null,
    val displayName: String? = null,
    val lastTransactionAt: Instant? = null,
    val transactionCount: Int = 0,
)

@Serializable
enum class TimePeriod(
    val displayName: String,
    val value: String,
) {
    ONE_HOUR("1H", "1h"),
    TWENTY_FOUR_HOURS("24H", "24h"),
    SEVEN_DAYS("7D", "7d"),
    THIRTY_DAYS("30D", "30d"),
    ONE_YEAR("1Y", "1y"),
}

@Serializable
data class PriceChange(
    val amount: String,
    val percentage: String,
    val isPositive: Boolean,
    val period: TimePeriod,
)

@Serializable
data class WalletOverview(
    val balance: Balance,
    val priceChange: PriceChange?,
    val recentTransactions: List<Transaction>,
    val frequentContacts: List<ContactUser>,
)

@Serializable
data class AssetTab(
    val type: AssetType,
    val count: Int,
)

@Serializable
data class TokenApproval(
    val id: String,
    val serviceName: String,
    val serviceIcon: String? = null,
    val chainName: String,
    val chainId: String,
    val contractAddress: String,
    val spenderAddress: String,
    val tokenSymbol: String,
    val tokenName: String,
    val approvedAmount: String, // Could be "Unlimited" or specific amount
    val createdAt: Instant,
    val lastUsedAt: Instant? = null,
    val riskLevel: ApprovalRiskLevel = ApprovalRiskLevel.LOW,
)

@Serializable
enum class ApprovalRiskLevel {
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
data class ApprovalsByService(
    val serviceName: String,
    val serviceIcon: String? = null,
    val chainName: String,
    val approvals: List<TokenApproval>,
    val totalCount: Int = approvals.size,
)

@Serializable
enum class AssetType(
    val displayName: String,
) {
    TOKENS("Assets"),
    NFTS("NFTs"),
    APPROVALS("Approvals"),
}

/**
 * OHLCV (Open, High, Low, Close, Volume) data for candlestick charts
 */
@Serializable
data class OHLCVData(
    val timestamp: Instant,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
) {
    init {
        require(high >= open && high >= close) {
            "High price must be >= open and close"
        }
        require(low <= open && low <= close) {
            "Low price must be <= open and close"
        }
        require(volume >= 0) {
            "Volume must be non-negative"
        }
    }

    /**
     * True if the candle is bullish (close > open)
     */
    val isBullish: Boolean get() = close > open

    /**
     * True if the candle is bearish (close < open)
     */
    val isBearish: Boolean get() = close < open

    /**
     * The body size (absolute difference between open and close)
     */
    val bodySize: Double get() = kotlin.math.abs(close - open)

    /**
     * The wick size (high - low)
     */
    val wickSize: Double get() = high - low

    companion object {
        /**
         * Calculate the price range (min to max) for a series of OHLCV data
         */
        fun List<OHLCVData>.priceRange(): Pair<Double, Double> {
            if (isEmpty()) return 0.0 to 0.0
            val min = minOf { it.low }
            val max = maxOf { it.high }
            return min to max
        }

        /**
         * Calculate the volume range for a series of OHLCV data
         */
        fun List<OHLCVData>.volumeRange(): Pair<Double, Double> {
            if (isEmpty()) return 0.0 to 0.0
            val min = minOf { it.volume }
            val max = maxOf { it.volume }
            return min to max
        }

        /**
         * Filter OHLCV data by time period
         */
        fun List<OHLCVData>.filterByPeriod(
            period: TimePeriod,
            currentTime: Instant = Clock.System.now(),
        ): List<OHLCVData> {
            val cutoffTime =
                when (period) {
                    TimePeriod.ONE_HOUR -> currentTime.minus(kotlin.time.Duration.parse("1h"))
                    TimePeriod.TWENTY_FOUR_HOURS -> currentTime.minus(kotlin.time.Duration.parse("1d"))
                    TimePeriod.SEVEN_DAYS -> currentTime.minus(kotlin.time.Duration.parse("7d"))
                    TimePeriod.THIRTY_DAYS -> currentTime.minus(kotlin.time.Duration.parse("30d"))
                    TimePeriod.ONE_YEAR -> currentTime.minus(kotlin.time.Duration.parse("365d"))
                }
            return filter { it.timestamp >= cutoffTime }
        }
    }
}
