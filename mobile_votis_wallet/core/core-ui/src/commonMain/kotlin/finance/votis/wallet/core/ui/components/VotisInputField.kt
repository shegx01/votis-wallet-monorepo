package finance.votis.wallet.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.theme.AppTheme
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A reusable input field component with Votis branding and theme support.
 *
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param modifier Optional modifier for the field
 * @param label Optional label text
 * @param placeholder Optional placeholder text
 * @param leadingText Optional text to show before the input (e.g., "@")
 * @param error Optional error message to display
 * @param enabled Whether the field is enabled
 * @param isLoading Whether to show loading state
 * @param singleLine Whether this is a single line field
 * @param keyboardType Type of keyboard to show
 * @param imeAction IME action for the keyboard
 * @param keyboardActions Actions to perform on keyboard events
 * @param visualTransformation Visual transformation for the text
 * @param onFocusChanged Callback when focus changes
 */
@Composable
fun VotisInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingText: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onFocusChanged: (FocusState) -> Unit = {},
) {
    val hasError = error != null
    var isFocused by remember { mutableStateOf(false) }

    val borderColor =
        when {
            hasError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.votisColors.brand
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        }

    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor =
        if (enabled) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }

    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = MaterialTheme.dimensions.spacingSmall),
            )
        }

        // Input field container
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(MaterialTheme.dimensions.buttonCornerRadius),
                    ).border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(MaterialTheme.dimensions.buttonCornerRadius),
                    ).padding(MaterialTheme.dimensions.spacingMedium)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        onFocusChanged(focusState)
                    },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Leading text (e.g., "@")
            if (leadingText != null) {
                Text(
                    text = leadingText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.votisColors.brand,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Text field
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled && !isLoading,
                    singleLine = singleLine,
                    textStyle =
                        TextStyle(
                            color = textColor,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        ),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = imeAction,
                        ),
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Placeholder
                if (value.isEmpty() && placeholder != null && !isLoading) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.votisColors.brand,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Error message
        if (hasError) {
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = MaterialTheme.dimensions.spacingSmall),
            )
        }
    }
}

@Preview
@Composable
fun VotisInputFieldPreview() {
    AppTheme {
        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VotisInputField(
                value = "",
                onValueChange = {},
                label = "Username",
                placeholder = "Enter your username",
                leadingText = "@",
            )

            VotisInputField(
                value = "john_doe",
                onValueChange = {},
                label = "Username",
                placeholder = "Enter your username",
                leadingText = "@",
            )

            VotisInputField(
                value = "john_doe",
                onValueChange = {},
                label = "Username",
                placeholder = "Enter your username",
                leadingText = "@",
                error = "This username is already taken",
            )

            VotisInputField(
                value = "checking",
                onValueChange = {},
                label = "Username",
                placeholder = "Enter your username",
                leadingText = "@",
                isLoading = true,
            )
        }
    }

    @Preview
    @Composable
    fun VotisInputFieldDarkPreview() {
        AppTheme(darkTheme = true) {
            Column(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                VotisInputField(
                    value = "",
                    onValueChange = {},
                    label = "Username",
                    placeholder = "Enter your username",
                    leadingText = "@",
                )

                VotisInputField(
                    value = "john_doe",
                    onValueChange = {},
                    label = "Username",
                    placeholder = "Enter your username",
                    leadingText = "@",
                )

                VotisInputField(
                    value = "john_doe",
                    onValueChange = {},
                    label = "Username",
                    placeholder = "Enter your username",
                    leadingText = "@",
                    error = "This username is already taken",
                )
            }
        }
    }
}
