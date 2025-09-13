package finance.votis.wallet.feature.wallet.presentation.send

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for SendViewModel following project best practices.
 */
class SendViewModelTest {
    private lateinit var viewModel: SendViewModel

    private fun setupViewModel() {
        viewModel = SendViewModel()
    }

    @Test
    fun `initial state should have correct defaults`() {
        setupViewModel()

        val state = viewModel.uiState.value

        assertEquals("", state.amount)
        assertEquals("SOL", state.selectedToken.symbol)
        assertEquals("", state.recipientAddress)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isAmountValid)
        assertFalse(state.isAddressValid)
        assertFalse(state.isSendEnabled)
    }

    @Test
    fun `updateAmount should validate numeric input correctly`() {
        setupViewModel()

        // Valid amount
        viewModel.handleIntent(SendIntent.UpdateAmount("10.5"))
        assertTrue(viewModel.uiState.value.isAmountValid)
        assertEquals("10.5", viewModel.uiState.value.amount)

        // Invalid amount - empty
        viewModel.handleIntent(SendIntent.UpdateAmount(""))
        assertFalse(viewModel.uiState.value.isAmountValid)

        // Invalid amount - zero
        viewModel.handleIntent(SendIntent.UpdateAmount("0"))
        assertFalse(viewModel.uiState.value.isAmountValid)
        assertEquals("0", viewModel.uiState.value.amount)

        // Negative input becomes positive (filter removes minus sign)
        viewModel.handleIntent(SendIntent.UpdateAmount("-5"))
        assertTrue(viewModel.uiState.value.isAmountValid)
        assertEquals("5", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount should filter non-numeric characters`() {
        setupViewModel()

        viewModel.handleIntent(SendIntent.UpdateAmount("abc123.45def"))

        // Should filter out non-numeric characters
        assertEquals("123.45", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateAmount should prevent multiple decimal points`() {
        setupViewModel()

        viewModel.handleIntent(SendIntent.UpdateAmount("12.34.56"))

        // Should only keep first decimal point
        assertEquals("12.3456", viewModel.uiState.value.amount)
    }

    @Test
    fun `updateRecipientAddress should validate Solana addresses correctly`() {
        setupViewModel()

        // Valid Solana address (base58, 32-44 chars)
        val validAddress = "7vfCXTUXx5WJV5JADk17DUJ4ksgau7utNKj4b963voxs"
        viewModel.handleIntent(SendIntent.UpdateRecipientAddress(validAddress))

        assertTrue(viewModel.uiState.value.isAddressValid)
        assertEquals(validAddress, viewModel.uiState.value.recipientAddress)

        // Invalid address - too short
        viewModel.handleIntent(SendIntent.UpdateRecipientAddress("123"))
        assertFalse(viewModel.uiState.value.isAddressValid)

        // Invalid address - contains invalid characters
        viewModel.handleIntent(SendIntent.UpdateRecipientAddress("0OIl" + "1".repeat(40)))
        assertFalse(viewModel.uiState.value.isAddressValid)

        // Empty address should be invalid
        viewModel.handleIntent(SendIntent.UpdateRecipientAddress(""))
        assertFalse(viewModel.uiState.value.isAddressValid)
    }

    @Test
    fun `isSendEnabled should be true when both amount and address are valid`() {
        setupViewModel()

        // Set valid amount
        viewModel.handleIntent(SendIntent.UpdateAmount("10"))
        // Set valid address
        viewModel.handleIntent(SendIntent.UpdateRecipientAddress("7vfCXTUXx5WJV5JADk17DUJ4ksgau7utNKj4b963voxs"))

        val state = viewModel.uiState.value
        assertTrue(state.isAmountValid)
        assertTrue(state.isAddressValid)
        assertTrue(state.isSendEnabled)
    }

    @Test
    fun `isSendEnabled should be true when both amount and address are valid and not loading`() {
        setupViewModel()

        // Set valid amount and address
        viewModel.handleIntent(SendIntent.UpdateAmount("10"))
        viewModel.handleIntent(SendIntent.UpdateRecipientAddress("7vfCXTUXx5WJV5JADk17DUJ4ksgau7utNKj4b963voxs"))

        val state = viewModel.uiState.value
        assertTrue(state.isAmountValid)
        assertTrue(state.isAddressValid)
        assertFalse(state.isLoading)
        assertTrue(state.isSendEnabled)
    }

    @Test
    fun `clearError should remove error from state`() {
        setupViewModel()

        // Set an invalid address to trigger error
        viewModel.handleIntent(SendIntent.UpdateRecipientAddress("invalid"))
        assertTrue(viewModel.uiState.value.error != null)

        // Clear error
        viewModel.handleIntent(SendIntent.ClearError)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `amountAsDouble should convert string amount to double correctly`() {
        setupViewModel()

        viewModel.handleIntent(SendIntent.UpdateAmount("10.5"))
        assertEquals(10.5, viewModel.uiState.value.amountAsDouble)

        viewModel.handleIntent(SendIntent.UpdateAmount(""))
        assertNull(viewModel.uiState.value.amountAsDouble)

        viewModel.handleIntent(SendIntent.UpdateAmount("invalid"))
        assertNull(viewModel.uiState.value.amountAsDouble)
    }

    @Test
    fun `sendTransaction should not proceed if send is not enabled`() {
        setupViewModel()

        // Don't set valid amount/address
        viewModel.handleIntent(SendIntent.SendTransaction)

        // State should remain unchanged
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSendEnabled)
    }
}
