package finance.votis.wallet.feature.wallet.presentation.send

import finance.votis.wallet.core.ui.mvi.BaseViewModelWithEffects

/**
 * ViewModel for the Send screen following MVI pattern.
 */
class SendViewModel :
    BaseViewModelWithEffects<SendUiState, SendIntent, SendUiEffect>(
        initialState = SendUiState(),
    ) {
    override fun handleIntent(intent: SendIntent) {
        when (intent) {
            is SendIntent.UpdateAmount -> updateAmount(intent.amount)
            is SendIntent.UpdateRecipientAddress -> updateRecipientAddress(intent.address)
            is SendIntent.SelectToken -> selectToken(intent.token)
            is SendIntent.SendTransaction -> sendTransaction()
            is SendIntent.ClearError -> clearError()
        }
    }

    private fun updateAmount(amount: String) {
        // Only allow numeric input with optional decimal point (no negative)
        val filteredAmount = amount.filter { it.isDigit() || it == '.' }

        // Prevent multiple decimal points
        val finalAmount =
            if (filteredAmount.count { it == '.' } > 1) {
                filteredAmount.take(filteredAmount.indexOf('.') + 1) +
                    filteredAmount.drop(filteredAmount.indexOf('.') + 1).filter { it != '.' }
            } else {
                filteredAmount
            }

        val isValid = validateAmount(finalAmount)

        updateState {
            copy(
                amount = finalAmount,
                isAmountValid = isValid,
                error = if (isValid) null else error,
            )
        }
    }

    private fun updateRecipientAddress(address: String) {
        val isValid = validateAddress(address.trim())

        updateState {
            copy(
                recipientAddress = address.trim(),
                isAddressValid = isValid,
                error = if (isValid || address.trim().isEmpty()) null else "Invalid wallet address",
            )
        }
    }

    private fun selectToken(token: finance.votis.wallet.core.domain.model.Token) {
        updateState {
            copy(selectedToken = token)
        }
    }

    private fun sendTransaction() {
        if (!currentState.isSendEnabled) return

        updateState { copy(isLoading = true) }

        // TODO: Implement actual transaction logic
        // For now, just simulate immediate success for testing
        updateState { copy(isLoading = false) }
        sendEffect(SendUiEffect.ShowTransactionSuccess)
    }

    private fun clearError() {
        updateState { copy(error = null) }
    }

    /**
     * Validates the amount input.
     * Returns true if the amount is a valid positive number.
     */
    private fun validateAmount(amount: String): Boolean {
        if (amount.isEmpty()) return false
        val numericAmount = amount.toDoubleOrNull() ?: return false
        return numericAmount > 0.0
    }

    /**
     * Validates the wallet address.
     * For Solana addresses: base58 encoded, 32-44 characters.
     */
    private fun validateAddress(address: String): Boolean {
        if (address.isEmpty()) return false

        // Basic Solana address validation
        // Real implementation should use proper base58 validation
        val base58Regex = Regex("^[1-9A-HJ-NP-Za-km-z]{32,44}$")
        return base58Regex.matches(address)
    }
}
