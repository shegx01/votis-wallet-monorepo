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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import finance.votis.wallet.core.ui.components.TabCard
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ApprovalServiceTopBar(
                serviceName = approvalsByService.serviceName,
                chainName = approvalsByService.chainName,
                onBackClick = onBackClick,
            )
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
                    onRevokeApproval = onRevokeApproval,
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApprovalServiceTopBar(
    serviceName: String,
    chainName: String,
    onBackClick: () -> Unit,
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
                // Service icon (using dummy colored square for now)
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .background(
                                color = getServiceColor(serviceName),
                                shape = RoundedCornerShape(8.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = serviceName.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface,
                    )
                }

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
                    Text(
                        text = chainName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription =
        stringResource(
            Res.string.revoke_approval_content_description,
            approval.tokenName,
            approval.approvedAmount,
        )

    var isChecked by remember { mutableStateOf(false) }

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
                                    .copy(
                                        alpha = 0.3f,
                                    )
                            },
                        shape = RoundedCornerShape(4.dp),
                    ).clip(RoundedCornerShape(4.dp))
                    .clickable { isChecked = !isChecked },
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

        // Revoke button - styled as text button with cyan color
        Text(
            text = stringResource(Res.string.revoke_button_text),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF00BCD4), // Cyan color like in design
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

/**
 * Generates consistent colors for service names.
 * Matches the color scheme used in ApprovalsList component.
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
