package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.AssetType
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.approvals_tab
import mobilevotiswallet.features.feature_wallet.generated.resources.nfts_tab
import mobilevotiswallet.features.feature_wallet.generated.resources.tokens_tab
import org.jetbrains.compose.resources.stringResource

/**
 * Tab component for switching between Tokens, NFTs, and Approvals.
 * Features translucent background with subtle selected state styling for modern appearance.
 */
@Composable
fun AssetTabs(
    selectedTab: AssetType,
    onTabSelected: (AssetType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                ).padding(4.dp)
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AssetType.values().forEach { tab ->
            AssetTab(
                assetType = tab,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Overloaded version with count parameters for development.
 * Currently maintains backward compatibility by not showing counts.
 * TODO: Implement Badge components when design system is finalized.
 */
@Composable
fun AssetTabs(
    selectedTab: AssetType,
    onTabSelected: (AssetType) -> Unit,
    tokenCount: Int,
    nftCount: Int,
    approvalsCount: Int,
    modifier: Modifier = Modifier,
) {
    // For now, maintain backward compatibility and use the regular version
    // In a future iteration, we can add Badge components to show counts
    AssetTabs(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}

@Composable
private fun AssetTab(
    assetType: AssetType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animate color transitions for smooth visual feedback
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
            } else {
                Color.Transparent
            },
        animationSpec = tween(durationMillis = 250),
        label = "containerColor",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(durationMillis = 250),
        label = "contentColor",
    )

    Box(
        modifier =
            modifier
                .height(40.dp)
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(12.dp),
                ).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Custom visual feedback through color animation
                    role = Role.Tab,
                    onClick = onClick,
                ).semantics {
                    selected = isSelected
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = getAssetTypeDisplayName(assetType),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
        )
    }
}

@Composable
private fun getAssetTypeDisplayName(assetType: AssetType): String =
    when (assetType) {
        AssetType.TOKENS -> stringResource(Res.string.tokens_tab)
        AssetType.NFTS -> stringResource(Res.string.nfts_tab)
        AssetType.APPROVALS -> stringResource(Res.string.approvals_tab)
    }
