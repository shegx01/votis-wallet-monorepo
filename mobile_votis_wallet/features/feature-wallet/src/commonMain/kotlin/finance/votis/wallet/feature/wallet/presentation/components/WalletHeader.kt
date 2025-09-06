package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import mobilevotiswallet.features.feature_wallet.generated.resources.user_handle_prefix
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
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Safe area spacing for status bar
        Spacer(
            modifier =
                Modifier.height(
                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                ),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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
                        text = stringResource(Res.string.user_handle_prefix, username),
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = getInitials(username),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Get initials from username for avatar display
 */
private fun getInitials(username: String): String =
    if (username.isNotEmpty()) {
        username.take(2).uppercase()
    } else {
        "U"
    }
