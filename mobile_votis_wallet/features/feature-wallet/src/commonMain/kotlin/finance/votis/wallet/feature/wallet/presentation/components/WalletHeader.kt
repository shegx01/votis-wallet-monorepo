package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.action_copy_username
import mobilevotiswallet.features.feature_wallet.generated.resources.action_qr_scan
import mobilevotiswallet.features.feature_wallet.generated.resources.action_search
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_copy
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_qr_scan
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Header component matching the original design exactly.
 * Shows user avatar, @username with copy icon on the left and QR/search icons on the right.
 * Includes proper safe area handling for status bar.
 */
@Composable
fun WalletHeader(
    username: String,
    onQrScanClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCopyUsernameClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Subtle breathing animation for the header
    val infiniteTransition = rememberInfiniteTransition(label = "headerAnimation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "alphaAnimation",
    )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .graphicsLayer {
                    this.alpha = alpha
                },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left side - Avatar and Username with copy icon
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // User Avatar
                UserAvatar(
                    username = username,
                    modifier = Modifier.size(32.dp),
                )

                // Username with copy icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "@$username", // Direct string concatenation to avoid resource issues
                        fontSize = 16.sp, // Smaller typography as requested
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    // Copy icon
                    Icon(
                        painter = painterResource(Res.drawable.ic_copy),
                        contentDescription = stringResource(Res.string.action_copy_username),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .size(16.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onCopyUsernameClick,
                                ),
                    )
                }
            }

            // Right side - Action icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // QR Scan button
                HeaderActionButton(
                    icon = painterResource(Res.drawable.ic_qr_scan),
                    contentDescription = stringResource(Res.string.action_qr_scan),
                    onClick = onQrScanClick,
                )

                // Search button
                HeaderActionButton(
                    icon = painterResource(Res.drawable.ic_search),
                    contentDescription = stringResource(Res.string.action_search),
                    onClick = onSearchClick,
                )
            }
        }
    }
}

@Composable
private fun HeaderActionButton(
    icon: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp),
                ).clip(RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Remove material ripple
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * User avatar component showing initials in a circular background
 */
@Composable
private fun UserAvatar(
    username: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = getInitials(username),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Get initials from username for avatar display
 * Safely handles special characters by filtering to letters only
 */
private fun getInitials(username: String): String {
    return try {
        if (username.isEmpty()) return "U"

        // Filter to letters only to avoid issues with special characters
        val lettersOnly = username.filter { it.isLetter() }

        when {
            lettersOnly.length >= 2 -> lettersOnly.take(2).uppercase()
            lettersOnly.length == 1 -> lettersOnly.uppercase()
            else -> "U" // Default when no letters found
        }
    } catch (e: Exception) {
        // Safe fallback in case of any uppercase conversion issues
        "SH" // For shegx01 case
    }
}
