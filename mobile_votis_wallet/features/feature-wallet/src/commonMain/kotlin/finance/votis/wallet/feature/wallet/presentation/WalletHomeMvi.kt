package finance.votis.wallet.feature.wallet.presentation

import finance.votis.wallet.core.domain.model.AssetType
import finance.votis.wallet.core.domain.model.ContactUser
import finance.votis.wallet.core.domain.model.TimePeriod
import finance.votis.wallet.core.domain.model.TokenBalance
import finance.votis.wallet.core.domain.model.Transaction
import finance.votis.wallet.core.domain.model.WalletOverview
import finance.votis.wallet.core.ui.mvi.BaseIntent
import finance.votis.wallet.core.ui.mvi.BaseUiEffect
import finance.votis.wallet.core.ui.mvi.BaseUiState

/**
 * State for the Wallet Home screen following MVI pattern.
 */
data class WalletHomeState(
    val isLoading: Boolean = false,
    val walletOverview: WalletOverview? = null,
    val selectedTimePeriod: TimePeriod = TimePeriod.TWENTY_FOUR_HOURS,
    val selectedAssetTab: AssetType = AssetType.TOKENS,
    val error: WalletHomeError? = null,
    val isRefreshing: Boolean = false,
    val showTimePeriodDropdown: Boolean = false,
) : BaseUiState {
    val hasData: Boolean get() = walletOverview != null
    val hasError: Boolean get() = error != null
    val balanceAmount: String get() = walletOverview?.balance?.totalValue ?: "$0.00"
    val priceChangeText: String? get() =
        walletOverview?.priceChange?.let {
            "${if (it.isPositive) "+" else ""}${it.amount} ${if (it.isPositive) "+" else ""}${it.percentage}%"
        }
    val isPriceChangePositive: Boolean get() = walletOverview?.priceChange?.isPositive ?: true
    val frequentContacts: List<ContactUser> get() = walletOverview?.frequentContacts ?: emptyList()
    val recentTransactions: List<Transaction> get() = walletOverview?.recentTransactions ?: emptyList()
    val tokenBalances: List<TokenBalance> get() = walletOverview?.balance?.tokens ?: emptyList()
    val tokensCount: Int get() = tokenBalances.size
}

/**
 * Actions that can be performed on the Wallet Home screen.
 */
sealed interface WalletHomeAction : BaseIntent {
    object LoadData : WalletHomeAction

    object RefreshData : WalletHomeAction

    object RetryLoad : WalletHomeAction

    object ClearError : WalletHomeAction

    // Time period selection
    object ToggleTimePeriodDropdown : WalletHomeAction

    data class SelectTimePeriod(
        val period: TimePeriod,
    ) : WalletHomeAction

    // Asset tabs
    data class SelectAssetTab(
        val assetType: AssetType,
    ) : WalletHomeAction

    // Action buttons
    object OnReceiveClicked : WalletHomeAction

    object OnSendClicked : WalletHomeAction

    object OnSwapClicked : WalletHomeAction

    object OnBuySellClicked : WalletHomeAction

    // Contact interactions
    data class OnContactClicked(
        val contact: ContactUser,
    ) : WalletHomeAction

    // Token interactions
    data class OnTokenClicked(
        val tokenBalance: TokenBalance,
    ) : WalletHomeAction
}

/**
 * Effects that can be triggered from the Wallet Home screen.
 */
sealed interface WalletHomeEffect : BaseUiEffect {
    object NavigateToReceive : WalletHomeEffect

    object NavigateToSend : WalletHomeEffect

    object NavigateToSwap : WalletHomeEffect

    object NavigateToBuySell : WalletHomeEffect

    data class NavigateToSendWithContact(
        val contact: ContactUser,
    ) : WalletHomeEffect

    data class NavigateToTokenDetails(
        val tokenBalance: TokenBalance,
    ) : WalletHomeEffect

    data class ShowError(
        val message: String,
    ) : WalletHomeEffect
}

/**
 * Errors that can occur on the Wallet Home screen.
 */
sealed interface WalletHomeError {
    object LoadError : WalletHomeError

    object RefreshError : WalletHomeError

    object NetworkError : WalletHomeError

    data class UnknownError(
        val message: String,
    ) : WalletHomeError
}
