package finance.votis.wallet.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors

/**
 * Validation states for input fields
 */
enum class ValidationState {
    IDLE,
    VALIDATING,
    VALID,
    ERROR,
}

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
 * @param validationState Current validation state
 * @param statusMessage Optional status message to display
 * @param validatingIcon Optional icon for validating state
 * @param validIcon Optional icon for valid state
 * @param errorIcon Optional icon for error state
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
    validationState: ValidationState = ValidationState.IDLE,
    statusMessage: String? = null,
    validatingIcon: Painter? = null,
    validIcon: Painter? = null,
    errorIcon: Painter? = null,
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

            // No validation indicator inside the input field
            // Icons will be shown below with status messages
        }

        // Status message or error message with icons below the input field
        when {
            hasError -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = MaterialTheme.dimensions.spacingSmall),
                ) {
                    if (errorIcon != null) {
                        Icon(
                            painter = errorIcon,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            statusMessage != null -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = MaterialTheme.dimensions.spacingSmall),
                ) {
                    val icon =
                        when (validationState) {
                            ValidationState.VALIDATING -> validatingIcon
                            ValidationState.VALID -> validIcon
                            else -> null
                        }

                    if (icon != null) {
                        // Rotation animation for loading state
                        val infiniteTransition = rememberInfiniteTransition(label = "loadingRotation")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec =
                                infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart,
                                ),
                            label = "rotation",
                        )

                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier =
                                Modifier
                                    .size(16.dp)
                                    .then(
                                        if (validationState == ValidationState.VALIDATING) {
                                            Modifier.rotate(rotation)
                                        } else {
                                            Modifier
                                        },
                                    ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            when (validationState) {
                                ValidationState.VALIDATING -> MaterialTheme.votisColors.greyText
                                ValidationState.VALID -> Color(0xFF34C759) // Green
                                else -> MaterialTheme.votisColors.greyText
                            },
                    )
                }
            }
        }
    }
}

// Preview functions removed to avoid theme dependency issues
// Will be added back in the feature module where AppTheme is available
