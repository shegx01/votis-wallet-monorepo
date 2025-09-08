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
enum class AssetType(
    val displayName: String,
) {
    TOKENS("Assets"),
    NFTS("NFTs"),
    APPROVALS("Approvals"),
}
