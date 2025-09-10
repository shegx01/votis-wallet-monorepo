package finance.votis.wallet.feature.wallet.presentation.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import finance.votis.wallet.core.domain.model.ApprovalsByService
import kotlinx.datetime.Clock
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.approval_count
import mobilevotiswallet.features.feature_wallet.generated.resources.approval_item_content_description
import mobilevotiswallet.features.feature_wallet.generated.resources.approvals_empty_subtitle
import mobilevotiswallet.features.feature_wallet.generated.resources.no_approvals_message
import org.jetbrains.compose.resources.stringResource

/**
 * Component displaying token approvals grouped by service/protocol.
 * Shows service name, chain, approval count, and allows navigation to detailed view.
 * Follows the same design patterns as TokenList and NftGrid.
 */
@Composable
fun ApprovalsList(
    approvals: List<ApprovalsByService>,
    onApprovalServiceClick: (ApprovalsByService) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (approvals.isEmpty()) {
        EmptyApprovalsState(modifier = modifier)
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            approvals.forEach { approvalsByService ->
                ApprovalServiceItem(
                    approvalsByService = approvalsByService,
                    onClick = { onApprovalServiceClick(approvalsByService) },
                )
            }
        }
    }
}

@Composable
private fun ApprovalServiceItem(
    approvalsByService: ApprovalsByService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription =
        stringResource(
            Res.string.approval_item_content_description,
            approvalsByService.serviceName,
            approvalsByService.chainName,
            approvalsByService.totalCount,
        )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(vertical = 12.dp)
                .semantics {
                    this.contentDescription = contentDescription
                },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Service icon/avatar
        ServiceAvatar(
            serviceName = approvalsByService.serviceName,
            modifier = Modifier.size(44.dp),
        )

        // Service info (name and chain)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = approvalsByService.serviceName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = approvalsByService.chainName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Approval count and arrow
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Count badge
            Box(
                modifier =
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(12.dp),
                        ).padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.approval_count, approvalsByService.totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Arrow icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ServiceAvatar(
    serviceName: String,
    modifier: Modifier = Modifier,
) {
    // Generate service-specific colors based on name
    val serviceColor = getServiceColor(serviceName)

    Box(
        modifier =
            modifier
                .background(
                    color = serviceColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Show first two characters of service name as fallback
        Text(
            text = serviceName.take(2).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = serviceColor,
        )
    }
}

@Composable
private fun EmptyApprovalsState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Placeholder icon
        Box(
            modifier =
                Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(16.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🔐",
                fontSize = 32.sp,
            )
        }

        Text(
            text = stringResource(Res.string.no_approvals_message),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.approvals_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

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

/**
 * Creates mock approval data for UI development and testing
 */
fun getMockApprovals(): List<ApprovalsByService> {
    val now = Clock.System.now()

    return listOf(
        ApprovalsByService(
            serviceName = "OKX Web3",
            chainName = "BNB Chain",
            approvals = emptyList(), // Mock individual approvals if needed for detail view
            totalCount = 4,
        ),
        ApprovalsByService(
            serviceName = "Jumper Exchange",
            chainName = "BNB Chain",
            approvals = emptyList(),
            totalCount = 2,
        ),
        ApprovalsByService(
            serviceName = "MetaMask Swaps",
            chainName = "ETH Chain",
            approvals = emptyList(),
            totalCount = 1,
        ),
    )
}
