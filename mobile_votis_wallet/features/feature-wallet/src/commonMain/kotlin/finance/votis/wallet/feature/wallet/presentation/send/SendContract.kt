package finance.votis.wallet.feature.wallet.presentation.send

import finance.votis.wallet.core.domain.model.Token
import finance.votis.wallet.core.ui.mvi.BaseIntent
import finance.votis.wallet.core.ui.mvi.BaseUiEffect
import finance.votis.wallet.core.ui.mvi.BaseUiState

/**
 * Send screen UI state following MVI pattern.
 */
data class SendUiState(
    val amount: String = "",
    val selectedToken: Token = defaultSolanaToken(),
    val recipientAddress: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAmountValid: Boolean = false,
    val isAddressValid: Boolean = false,
) : BaseUiState {
    val isSendEnabled: Boolean
        get() = isAmountValid && isAddressValid && !isLoading && error == null

    val amountAsDouble: Double?
        get() = amount.toDoubleOrNull()

    companion object {
        fun defaultSolanaToken(): Token =
            Token(
                symbol = "SOL",
                name = "Solana",
                decimals = 9,
                logoUrl = null,
            )
    }
}

/**
 * User intents for the Send screen.
 */
sealed class SendIntent : BaseIntent {
    data class UpdateAmount(
        val amount: String,
    ) : SendIntent()

    data class UpdateRecipientAddress(
        val address: String,
    ) : SendIntent()

    data class SelectToken(
        val token: Token,
    ) : SendIntent()

    data object SendTransaction : SendIntent()

    data object ClearError : SendIntent()
}

/**
 * One-time UI effects for the Send screen.
 */
sealed class SendUiEffect : BaseUiEffect {
    data object NavigateBack : SendUiEffect()

    data object ShowTransactionSuccess : SendUiEffect()

    data class ShowError(
        val message: String,
    ) : SendUiEffect()
}
