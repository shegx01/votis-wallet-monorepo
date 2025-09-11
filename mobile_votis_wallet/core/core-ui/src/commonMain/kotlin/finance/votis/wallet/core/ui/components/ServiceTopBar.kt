package finance.votis.wallet.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Reusable top app bar component for service-related screens.
 * Features a centered service icon with name and subtitle, plus back navigation.
 *
 * @param serviceName The name of the service (e.g., "OKX Web3")
 * @param serviceSubtitle Optional subtitle (e.g., "BNB Chain")
 * @param onBackClick Callback when back button is pressed
 * @param serviceColor Optional custom color for the service icon, defaults to dynamic color based on service name
 * @param modifier Optional modifier for the top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTopBar(
    serviceName: String,
    serviceSubtitle: String? = null,
    onBackClick: () -> Unit,
    serviceColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset(x = (-24).dp),
                // Compensate for navigation icon space to center content
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Service icon
                ServiceIcon(
                    serviceName = serviceName,
                    serviceColor = serviceColor,
                    modifier = Modifier.size(32.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    serviceSubtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        modifier = modifier,
    )
}

/**
 * Service icon component that displays a colored square with service initials.
 *
 * @param serviceName The service name to generate initials and color from
 * @param serviceColor Optional custom color, defaults to generated color based on service name
 * @param modifier Optional modifier for the icon
 */
@Composable
private fun ServiceIcon(
    serviceName: String,
    serviceColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = serviceColor ?: getServiceColor(serviceName)

    Box(
        modifier =
            modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = serviceName.take(2).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

/**
 * Generates consistent colors for service names.
 * Uses predefined colors for known services, otherwise generates from hash.
 */
private fun getServiceColor(serviceName: String): Color =
    when (serviceName.lowercase()) {
        "okx web3", "okx" -> Color(0xFF000000) // Black
        "jumper exchange", "jumper" -> Color(0xFF6C5CE7) // Purple
        "metamask swaps", "metamask" -> Color(0xFFFF6B35) // Orange
        "uniswap" -> Color(0xFFFF007A) // Pink
        "1inch" -> Color(0xFF1F2937) // Dark gray
        "pancakeswap" -> Color(0xFF1FC7D4) // Cyan
        else -> {
            // Generate consistent color based on service name hash
            val colors =
                listOf(
                    Color(0xFF6366F1), // Indigo
                    Color(0xFF8B5CF6), // Violet
                    Color(0xFF06B6D4), // Cyan
                    Color(0xFF10B981), // Emerald
                    Color(0xFFF59E0B), // Amber
                    Color(0xFFEF4444), // Red
                    Color(0xFF3B82F6), // Blue
                    Color(0xFF84CC16), // Lime
                )
            val hash = serviceName.hashCode()
            colors[kotlin.math.abs(hash) % colors.size]
        }
    }
