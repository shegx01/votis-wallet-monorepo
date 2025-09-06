package finance.votis.wallet.core.domain.usecase

import finance.votis.wallet.core.domain.Result
import finance.votis.wallet.core.domain.UseCase
import finance.votis.wallet.core.domain.model.ContactUser
import finance.votis.wallet.core.domain.model.PriceChange
import finance.votis.wallet.core.domain.model.TimePeriod
import finance.votis.wallet.core.domain.model.WalletOverview
import finance.votis.wallet.core.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case to get comprehensive wallet overview data for the home screen.
 */
class GetWalletOverviewUseCase(
    private val walletRepository: WalletRepository,
) : UseCase<TimePeriod, WalletOverview>() {
    /**
     * Gets the wallet overview data including balance, price changes, and recent activity.
     */
    override suspend fun execute(parameters: TimePeriod): WalletOverview {
        val balance = walletRepository.getWalletBalance()
        val transactions = walletRepository.getTransactionHistory()

        when {
            balance is Result.Failure -> throw balance.error
            transactions is Result.Failure -> throw transactions.error
            else -> {
                val balanceData = (balance as Result.Success).data
                val transactionData = (transactions as Result.Success).data

                // Get price change data (mock for now)
                val priceChange = getPriceChangeForPeriod(parameters)

                // Get frequent contacts from recent transactions
                val frequentContacts = getFrequentContactsFromTransactions(transactionData)

                return WalletOverview(
                    balance = balanceData,
                    priceChange = priceChange,
                    recentTransactions = transactionData.take(5), // Show latest 5
                    frequentContacts = frequentContacts,
                )
            }
        }
    }

    /**
     * Observes wallet overview changes.
     */
    fun observe(timePeriod: TimePeriod = TimePeriod.TWENTY_FOUR_HOURS): Flow<WalletOverview> =
        combine(
            walletRepository.observeBalance(),
            walletRepository.observeTransactions(),
        ) { balance, transactions ->
            val priceChange = getPriceChangeForPeriod(timePeriod)
            val frequentContacts = getFrequentContactsFromTransactions(transactions)

            WalletOverview(
                balance = balance,
                priceChange = priceChange,
                recentTransactions = transactions.take(5),
                frequentContacts = frequentContacts,
            )
        }

    private fun getPriceChangeForPeriod(timePeriod: TimePeriod): PriceChange? {
        // TODO: Implement actual price change calculation from backend
        // For now, return mock data based on the UI mockup
        return PriceChange(
            amount = "+$233",
            percentage = "+3%",
            isPositive = true,
            period = timePeriod,
        )
    }

    private fun getFrequentContactsFromTransactions(
        transactions: List<finance.votis.wallet.core.domain.model.Transaction>,
    ): List<ContactUser> {
        // TODO: Implement actual frequent contacts logic
        // For now, return mock data based on the UI mockup
        return listOf(
            ContactUser(
                id = "1",
                username = "angel_lubin",
                walletAddress = "0x1234...5678",
                displayName = "Angel Lubin",
                avatarUrl = null,
                transactionCount = 5,
            ),
            ContactUser(
                id = "2",
                username = "ann_baptista",
                walletAddress = "0x2345...6789",
                displayName = "Ann Baptista",
                avatarUrl = null,
                transactionCount = 3,
            ),
            ContactUser(
                id = "3",
                username = "makenna_t",
                walletAddress = "0x3456...7890",
                displayName = "Makenna T",
                avatarUrl = null,
                transactionCount = 2,
            ),
            ContactUser(
                id = "4",
                username = "craig_septimus",
                walletAddress = "0x4567...8901",
                displayName = "Craig Septimus",
                avatarUrl = null,
                transactionCount = 1,
            ),
        )
    }
}
