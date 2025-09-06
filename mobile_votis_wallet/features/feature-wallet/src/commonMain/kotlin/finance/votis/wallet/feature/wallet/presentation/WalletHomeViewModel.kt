package finance.votis.wallet.feature.wallet.presentation

import androidx.lifecycle.viewModelScope
import finance.votis.wallet.core.domain.Result
import finance.votis.wallet.core.domain.model.AssetType
import finance.votis.wallet.core.domain.model.TimePeriod
import finance.votis.wallet.core.domain.usecase.GetWalletOverviewUseCase
import finance.votis.wallet.core.ui.mvi.BaseViewModelWithEffects
import kotlinx.coroutines.launch

/**
 * ViewModel for the Wallet Home screen following MVI pattern.
 * Manages wallet data, user interactions, and navigation effects.
 */
class WalletHomeViewModel(
    private val getWalletOverviewUseCase: GetWalletOverviewUseCase,
) : BaseViewModelWithEffects<WalletHomeState, WalletHomeAction, WalletHomeEffect>(
        initialState = WalletHomeState(),
    ) {
    init {
        // Load data when ViewModel is initialized
        handleIntent(WalletHomeAction.LoadData)
    }

    override fun handleIntent(intent: WalletHomeAction) {
        when (intent) {
            is WalletHomeAction.LoadData -> loadData()
            is WalletHomeAction.RefreshData -> refreshData()
            is WalletHomeAction.RetryLoad -> retryLoad()
            is WalletHomeAction.ClearError -> clearError()
            is WalletHomeAction.ToggleTimePeriodDropdown -> toggleTimePeriodDropdown()
            is WalletHomeAction.SelectTimePeriod -> selectTimePeriod(intent.period)
            is WalletHomeAction.SelectAssetTab -> selectAssetTab(intent.assetType)
            is WalletHomeAction.OnReceiveClicked -> handleReceiveClicked()
            is WalletHomeAction.OnSendClicked -> handleSendClicked()
            is WalletHomeAction.OnSwapClicked -> handleSwapClicked()
            is WalletHomeAction.OnBuySellClicked -> handleBuySellClicked()
            is WalletHomeAction.OnContactClicked -> handleContactClicked(intent.contact)
            is WalletHomeAction.OnTokenClicked -> handleTokenClicked(intent.tokenBalance)
        }
    }

    private fun loadData() {
        if (currentState.hasData) return // Don't reload if we already have data

        updateState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = getWalletOverviewUseCase(currentState.selectedTimePeriod)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            walletOverview = result.data,
                            error = null,
                        )
                    }
                }
                is Result.Failure -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = WalletHomeError.LoadError,
                        )
                    }
                }
            }
        }
    }

    private fun refreshData() {
        updateState { copy(isRefreshing = true, error = null) }

        viewModelScope.launch {
            when (val result = getWalletOverviewUseCase(currentState.selectedTimePeriod)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isRefreshing = false,
                            walletOverview = result.data,
                            error = null,
                        )
                    }
                }
                is Result.Failure -> {
                    updateState {
                        copy(
                            isRefreshing = false,
                            error = WalletHomeError.RefreshError,
                        )
                    }
                    sendEffect(WalletHomeEffect.ShowError("Failed to refresh data"))
                }
            }
        }
    }

    private fun retryLoad() {
        updateState { copy(error = null) }
        loadData()
    }

    private fun clearError() {
        updateState { copy(error = null) }
    }

    private fun toggleTimePeriodDropdown() {
        updateState { copy(showTimePeriodDropdown = !showTimePeriodDropdown) }
    }

    private fun selectTimePeriod(period: TimePeriod) {
        updateState {
            copy(
                selectedTimePeriod = period,
                showTimePeriodDropdown = false,
            )
        }

        // Reload data with new time period
        refreshData()
    }

    private fun selectAssetTab(assetType: AssetType) {
        updateState { copy(selectedAssetTab = assetType) }
    }

    private fun handleReceiveClicked() {
        sendEffect(WalletHomeEffect.NavigateToReceive)
    }

    private fun handleSendClicked() {
        sendEffect(WalletHomeEffect.NavigateToSend)
    }

    private fun handleSwapClicked() {
        sendEffect(WalletHomeEffect.NavigateToSwap)
    }

    private fun handleBuySellClicked() {
        sendEffect(WalletHomeEffect.NavigateToBuySell)
    }

    private fun handleContactClicked(contact: finance.votis.wallet.core.domain.model.ContactUser) {
        sendEffect(WalletHomeEffect.NavigateToSendWithContact(contact))
    }

    private fun handleTokenClicked(tokenBalance: finance.votis.wallet.core.domain.model.TokenBalance) {
        sendEffect(WalletHomeEffect.NavigateToTokenDetails(tokenBalance))
    }
}
