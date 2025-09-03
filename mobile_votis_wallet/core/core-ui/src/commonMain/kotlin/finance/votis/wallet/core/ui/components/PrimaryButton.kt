package finance.votis.wallet.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors

/**
 * A primary action button with brand styling.
 *
 * @param text The text to display on the button
 * @param onClick Callback when the button is clicked
 * @param modifier Optional modifier for the button
 * @param enabled Whether the button is enabled
 * @param backgroundColor Background color of the button
 * @param contentColor Content (text) color of the button
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.votisColors.brand,
    contentColor: Color = Color.White,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .fillMaxWidth()
                .height(MaterialTheme.dimensions.buttonHeight),
        shape = RoundedCornerShape(MaterialTheme.dimensions.buttonCornerRadius),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.38f),
                disabledContentColor = contentColor.copy(alpha = 0.38f),
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
