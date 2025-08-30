package finance.votis.wallet.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors

/**
 * A reusable button component for social sign-in providers.
 *
 * @param text The text to display on the button
 * @param icon The icon painter for the social provider
 * @param onClick Callback when the button is clicked
 * @param iconTint The tint color for the icon. Use Color.Unspecified to keep original colors
 * @param iconSize The size of the icon
 * @param modifier Optional modifier for the button
 */
@Composable
fun SocialSignInButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.votisColors.onSurface,
    iconSize: Dp = MaterialTheme.dimensions.iconSize,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(MaterialTheme.dimensions.buttonHeight),
        shape = RoundedCornerShape(MaterialTheme.dimensions.buttonCornerRadius),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.votisColors.surface,
                contentColor = MaterialTheme.votisColors.onSurface,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = icon,
                contentDescription = "$text Logo",
                modifier = Modifier.size(iconSize),
                tint = iconTint,
            )
            Spacer(
                modifier =
                    Modifier.width(
                        MaterialTheme.dimensions.spacingSmall + MaterialTheme.dimensions.spacingXSmall,
                    ),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
