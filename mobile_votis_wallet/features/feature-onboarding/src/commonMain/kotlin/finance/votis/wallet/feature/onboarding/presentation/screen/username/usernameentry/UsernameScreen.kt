package finance.votis.wallet.feature.onboarding.presentation.screen.username.usernameentry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import finance.votis.wallet.core.ui.components.PrimaryButton
import finance.votis.wallet.core.ui.components.VotisInputField
import finance.votis.wallet.core.ui.theme.AppTheme
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors
import mobilevotiswallet.features.feature_onboarding.generated.resources.Res
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_continue_button
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_entry_label
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_entry_placeholder
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_entry_subtitle
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_entry_title
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_error_invalid_chars
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_error_network
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_error_starts_underscore
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_error_taken
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_error_too_long
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_error_too_short
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Username entry screen that allows users to create their unique @username.
 * Follows the MVI pattern and integrates with the existing onboarding flow.
 */
@Composable
fun UsernameScreen(
    viewModel: UsernameViewModel,
    onNavigateNext: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val effect by viewModel.uiEffect.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle effects
    LaunchedEffect(effect) {
        effect?.let { currentEffect ->
            when (currentEffect) {
                is UsernameEffect.NavigateToNextScreen -> {
                    viewModel.clearEffect()
                    onNavigateNext(currentEffect.username)
                }
                is UsernameEffect.NavigateBack -> {
                    viewModel.clearEffect()
                    onNavigateBack()
                }
                is UsernameEffect.ShowMessage -> {
                    viewModel.clearEffect()
                    // Could show a snackbar or toast here
                }
            }
        }
    }

    UsernameScreenContent(
        state = state,
        onUsernameChanged = { viewModel.handleIntent(UsernameAction.OnUsernameChanged(it)) },
        onContinueClicked = {
            keyboardController?.hide()
            viewModel.handleIntent(UsernameAction.OnContinueClicked)
        },
        onRetryClicked = { viewModel.handleIntent(UsernameAction.RetryValidation) },
    )
}

@Composable
private fun UsernameScreenContent(
    state: UsernameState,
    onUsernameChanged: (String) -> Unit,
    onContinueClicked: () -> Unit,
    onRetryClicked: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = MaterialTheme.dimensions.screenHorizontalPadding)
                .safeContentPadding()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))

        // Title and subtitle
        Text(
            text = stringResource(Res.string.username_entry_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = MaterialTheme.dimensions.spacingSmall),
        )

        Text(
            text = stringResource(Res.string.username_entry_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.votisColors.greyText,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.dimensions.spacingXLarge),
        )

        // Username input field
        VotisInputField(
            value = state.username,
            onValueChange = onUsernameChanged,
            label = stringResource(Res.string.username_entry_label),
            placeholder = stringResource(Res.string.username_entry_placeholder),
            leadingText = "@",
            error = state.error?.let { mapErrorToString(it) },
            isLoading = state.showLoadingIndicator,
            imeAction = ImeAction.Done,
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        if (state.canContinue) {
                            onContinueClicked()
                        }
                    },
                ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        PrimaryButton(
            text = stringResource(Res.string.username_continue_button),
            onClick = onContinueClicked,
            enabled = state.canContinue && !state.showLoadingIndicator,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.dimensions.spacingLarge),
        )
    }
}

@Composable
private fun mapErrorToString(error: UsernameError): String =
    when (error) {
        is UsernameError.TooShort -> stringResource(Res.string.username_error_too_short)
        is UsernameError.TooLong -> stringResource(Res.string.username_error_too_long)
        is UsernameError.InvalidCharacters -> stringResource(Res.string.username_error_invalid_chars)
        is UsernameError.StartsWithUnderscore -> stringResource(Res.string.username_error_starts_underscore)
        is UsernameError.AlreadyTaken -> stringResource(Res.string.username_error_taken)
        is UsernameError.NetworkError -> stringResource(Res.string.username_error_network)
        is UsernameError.Unknown -> error.message
    }

@Preview
@Composable
fun UsernameScreenPreview() {
    AppTheme {
        UsernameScreenContent(
            state =
                UsernameState(
                    username = "john_doe",
                    isValidating = false,
                    isUsernameValid = true,
                    canContinue = true,
                ),
            onUsernameChanged = {},
            onContinueClicked = {},
            onRetryClicked = {},
        )
    }
}

@Preview
@Composable
fun UsernameScreenWithErrorPreview() {
    AppTheme {
        UsernameScreenContent(
            state =
                UsernameState(
                    username = "jo",
                    error = UsernameError.TooShort,
                    isUsernameValid = false,
                    canContinue = false,
                ),
            onUsernameChanged = {},
            onContinueClicked = {},
            onRetryClicked = {},
        )
    }
}

@Preview
@Composable
fun UsernameScreenLoadingPreview() {
    AppTheme {
        UsernameScreenContent(
            state =
                UsernameState(
                    username = "checking",
                    isValidating = true,
                    isUsernameValid = false,
                    canContinue = false,
                ),
            onUsernameChanged = {},
            onContinueClicked = {},
            onRetryClicked = {},
        )
    }
}

@Preview
@Composable
fun UsernameScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        UsernameScreenContent(
            state =
                UsernameState(
                    username = "john_doe",
                    isValidating = false,
                    isUsernameValid = true,
                    canContinue = true,
                ),
            onUsernameChanged = {},
            onContinueClicked = {},
            onRetryClicked = {},
        )
    }
}
