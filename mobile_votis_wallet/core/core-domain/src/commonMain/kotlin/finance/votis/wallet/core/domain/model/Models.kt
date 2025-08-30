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
