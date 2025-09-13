package finance.votis.wallet.feature.wallet.presentation.send

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import finance.votis.wallet.core.ui.components.PrimaryButton
import finance.votis.wallet.core.ui.components.ServiceTopBar
import finance.votis.wallet.core.ui.components.VotisInputField
import finance.votis.wallet.core.ui.theme.dimensions
import mobilevotiswallet.features.feature_wallet.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Send screen that matches the provided design.
 * Features amount input, token selection, recipient address field, and send button.
 */
@Composable
fun SendScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = viewModel { SendViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val effect by viewModel.uiEffect.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }

    // Handle UI effects
    LaunchedEffect(effect) {
        when (effect) {
            is SendUiEffect.NavigateBack -> {
                onBackClick()
                viewModel.clearEffect()
            }
            is SendUiEffect.ShowTransactionSuccess -> {
                // Could show a snackbar here
                viewModel.clearEffect()
            }
            is SendUiEffect.ShowError -> {
                // Could show a snackbar here
                viewModel.clearEffect()
            }
            null -> { /* No effect */ }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        // Top bar
        ServiceTopBar(
            content = {
                Text(
                    text = stringResource(Res.string.send_screen_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            onBackClick = onBackClick,
        )

        // Main content
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.dimensions.screenHorizontalPadding)
                    .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Amount section - large centered input
            AmountSection(
                amount = state.amount,
                selectedToken = state.selectedToken,
                onAmountChange = { viewModel.handleIntent(SendIntent.UpdateAmount(it)) },
                onTokenClick = { /* TODO: Show token selector */ },
                focusRequester = amountFocusRequester,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Recipient address field
            VotisInputField(
                value = state.recipientAddress,
                onValueChange = { viewModel.handleIntent(SendIntent.UpdateRecipientAddress(it)) },
                label = stringResource(Res.string.send_recipient_label),
                placeholder = stringResource(Res.string.send_recipient_placeholder),
                singleLine = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                keyboardActions =
                    KeyboardActions(
                        onDone = { keyboardController?.hide() },
                    ),
                error =
                    if (!state.isAddressValid && state.recipientAddress.isNotEmpty()) {
                        stringResource(Res.string.send_address_error)
                    } else {
                        null
                    },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Send button
            PrimaryButton(
                text = stringResource(Res.string.send_cta_button),
                onClick = { viewModel.handleIntent(SendIntent.SendTransaction) },
                enabled = state.isSendEnabled,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.dimensions.footerVerticalPadding),
            )
        }
    }

    // Auto-focus amount field when screen opens
    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }
}

/**
 * Amount input section with vertical layout to match the expected design.
 */
@Composable
private fun AmountSection(
    amount: String,
    selectedToken: finance.votis.wallet.core.domain.model.Token,
    onAmountChange: (String) -> Unit,
    onTokenClick: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Large amount and token in horizontal layout with underline
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            // Amount input with underline
            BasicTextField(
                value = amount.ifEmpty { "0" },
                onValueChange = onAmountChange,
                textStyle =
                    TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    ),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                singleLine = true,
                modifier = Modifier.focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        innerTextField()

                        Spacer(modifier = Modifier.height(4.dp))

                        // Underline under the amount
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.width(60.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Token symbol
            Text(
                text = selectedToken.symbol,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // USD value placeholder (0 USD)
        Text(
            text = "0 ${stringResource(Res.string.send_usd_suffix)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Token selector button
        TokenSelectorButton(
            selectedToken = selectedToken,
            balance = "11", // Mock balance
            onClick = onTokenClick,
        )
    }
}

/**
 * Token selector button showing selected token and balance.
 */
@Composable
private fun TokenSelectorButton(
    selectedToken: finance.votis.wallet.core.domain.model.Token,
    balance: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Token symbol
            Text(
                text = selectedToken.symbol,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            // Separator dot
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Balance
            Text(
                text = balance,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Dropdown arrow
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
