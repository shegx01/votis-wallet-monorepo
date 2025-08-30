package finance.votis.wallet.core.domain.repository

import finance.votis.wallet.core.domain.Result
import finance.votis.wallet.core.domain.model.Balance
import finance.votis.wallet.core.domain.model.Transaction
import finance.votis.wallet.core.domain.model.TransactionResult
import finance.votis.wallet.core.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for wallet operations.
 * All wallet-related data operations should go through this interface.
 */
interface WalletRepository {
    /**
     * Creates a new wallet for the authenticated user.
     */
    suspend fun createWallet(): Result<Wallet>

    /**
     * Gets the current wallet balance.
     */
    suspend fun getWalletBalance(): Result<Balance>

    /**
     * Gets transaction history for the user's wallet.
     */
    suspend fun getTransactionHistory(): Result<List<Transaction>>

    /**
     * Submits a send transaction.
     */
    suspend fun sendTransaction(
        amount: String,
        recipient: String,
        token: String = "ETH",
    ): Result<TransactionResult>

    /**
     * Gets receive information (wallet address, QR code data).
     */
    suspend fun getReceiveInfo(): Result<ReceiveInfo>

    /**
     * Initiates a token swap operation.
     */
    suspend fun swapTokens(
        fromToken: String,
        toToken: String,
        amount: String,
    ): Result<TransactionResult>

    /**
     * Observes balance changes.
     */
    fun observeBalance(): Flow<Balance>

    /**
     * Observes transaction updates.
     */
    fun observeTransactions(): Flow<List<Transaction>>
}

/**
 * Information needed for receiving payments.
 */
data class ReceiveInfo(
    val walletAddress: String,
    val qrCodeData: String,
    val supportedTokens: List<String>,
)
