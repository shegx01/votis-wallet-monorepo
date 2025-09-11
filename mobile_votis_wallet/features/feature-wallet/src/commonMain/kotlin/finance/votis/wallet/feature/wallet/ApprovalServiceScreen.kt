package finance.votis.wallet.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import finance.votis.wallet.core.domain.model.TokenApproval
import finance.votis.wallet.core.ui.components.ServiceTopBar
import finance.votis.wallet.core.ui.components.TabCard
import finance.votis.wallet.feature.wallet.presentation.components.ApprovalServiceHeader
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.approved_amount_title
import mobilevotiswallet.features.feature_wallet.generated.resources.contract_address_content_description
import mobilevotiswallet.features.feature_wallet.generated.resources.contract_address_label
import mobilevotiswallet.features.feature_wallet.generated.resources.copy_address_description
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_copy
import mobilevotiswallet.features.feature_wallet.generated.resources.revoke_approval_content_description
import mobilevotiswallet.features.feature_wallet.generated.resources.revoke_button_text
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Screen displaying detailed token approvals for a specific service.
 * Shows contract address, approved amounts, and individual approval items with revoke functionality.
 * Uses TabCard components to wrap content sections as per design requirements.
 */
@Composable
fun ApprovalServiceScreen(
    approvalsByService: ApprovalsByService,
    onBackClick: () -> Unit,
    onCopyAddress: (String) -> Unit = {},
    onRevokeApproval: (TokenApproval) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // State to track which approvals are checked
    var checkedApprovals by remember { mutableStateOf(setOf<String>()) }
    val checkedCount = checkedApprovals.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ServiceTopBar(
                content = {
                    ApprovalServiceHeader(
                        serviceName = approvalsByService.serviceName,
                        chainName = approvalsByService.chainName,
                    )
                },
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            if (checkedCount > 0) {
                RevokeBottomBar(
                    checkedCount = checkedCount,
                    onRevokeSelected = {
                        // TODO: Implement bulk revoke functionality
                        val selectedApprovals =
                            approvalsByService.approvals.filter {
                                checkedApprovals.contains(it.id)
                            }
                        selectedApprovals.forEach { onRevokeApproval(it) }
                        checkedApprovals = emptySet() // Clear selections after revoke
                    },
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Contract Address Section wrapped in TabCard
            item {
                ContractAddressSection(
                    contractAddress = getContractAddress(approvalsByService),
                    onCopyAddress = onCopyAddress,
                )
            }

            // Approved Amount Section wrapped in TabCard
            item {
                ApprovedAmountSection(
                    approvals = approvalsByService.approvals,
                    checkedApprovals = checkedApprovals,
                    onCheckChanged = { approvalId, isChecked ->
                        checkedApprovals =
                            if (isChecked) {
                                checkedApprovals + approvalId
                            } else {
                                checkedApprovals - approvalId
                            }
                    },
                    onRevokeApproval = onRevokeApproval,
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ContractAddressSection(
    contractAddress: String,
    onCopyAddress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription =
        stringResource(
            Res.string.contract_address_content_description,
            contractAddress,
        )

    TabCard(
        content = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            this.contentDescription = contentDescription
                        },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(Res.string.contract_address_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = contractAddress,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                            ).clickable { onCopyAddress(contractAddress) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_copy),
                        contentDescription = stringResource(Res.string.copy_address_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        },
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@Composable
private fun ApprovedAmountSection(
    approvals: List<TokenApproval>,
    checkedApprovals: Set<String>,
    onCheckChanged: (String, Boolean) -> Unit,
    onRevokeApproval: (TokenApproval) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabCard(
        header = {
            Text(
                text = stringResource(Res.string.approved_amount_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                approvals.forEach { approval ->
                    ApprovalItem(
                        approval = approval,
                        isChecked = checkedApprovals.contains(approval.id),
                        onCheckChanged = { isChecked -> onCheckChanged(approval.id, isChecked) },
                        onRevoke = { onRevokeApproval(approval) },
                    )
                }
            }
        },
        contentDescription = "Token approvals with revoke options",
        modifier = modifier,
    )
}

@Composable
private fun ApprovalItem(
    approval: TokenApproval,
    isChecked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription =
        stringResource(
            Res.string.revoke_approval_content_description,
            approval.tokenName,
            approval.approvedAmount,
        )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics {
                    this.contentDescription = contentDescription
                },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Functional checkbox
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .background(
                        color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp),
                    ).border(
                        width = 1.dp,
                        color =
                            if (isChecked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        shape = RoundedCornerShape(4.dp),
                    ).clip(RoundedCornerShape(4.dp))
                    .clickable { onCheckChanged(!isChecked) },
            contentAlignment = Alignment.Center,
        ) {
            if (isChecked) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Token avatar
        TokenApprovalAvatar(
            tokenSymbol = approval.tokenSymbol,
            modifier = Modifier.size(44.dp),
        )

        // Token info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = approval.approvedAmount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = approval.tokenName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Revoke button - styled as text button with primary color
        Text(
            text = stringResource(Res.string.revoke_button_text),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .clickable { onRevoke() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun TokenApprovalAvatar(
    tokenSymbol: String,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = getTokenColor(tokenSymbol)

    Box(
        modifier =
            modifier
                .background(
                    color = backgroundColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = tokenSymbol,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = backgroundColor,
        )
    }
}

/**
 * Gets a representative contract address from the approvals list.
 * In a real implementation, this would be more sophisticated.
 */
private fun getContractAddress(approvalsByService: ApprovalsByService): String =
    approvalsByService.approvals.firstOrNull()?.contractAddress ?: "0x6b1...e618d"

/**
 * Generates consistent colors for token symbols.
 * Matches the color scheme used in TokenList component.
 */
private fun getTokenColor(tokenSymbol: String): Color =
    when (tokenSymbol) {
        "BTC", "BTCB" -> Color(0xFFFF9500) // Bitcoin orange
        "ETH" -> Color(0xFF627EEA) // Ethereum blue
        "SOL" -> Color(0xFF9945FF) // Solana purple
        "USDT", "USDC" -> Color(0xFF26A17B) // Stablecoin green
        else -> {
            // Generate consistent color based on symbol hash
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
            val hash = tokenSymbol.hashCode()
            colors[kotlin.math.abs(hash) % colors.size]
        }
    }

@Composable
private fun RevokeBottomBar(
    checkedCount: Int,
    onRevokeSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Button(
            onClick = onRevokeSelected,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "Revoke ($checkedCount)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}
