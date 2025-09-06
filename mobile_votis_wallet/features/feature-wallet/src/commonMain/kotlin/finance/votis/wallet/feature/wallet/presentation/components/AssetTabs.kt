package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Matches the original design with proper styling and selection states.
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                ).padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
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
 * Overloaded version with count parameters for development
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
    // For now, just ignore the counts and use the regular version
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
    Box(
        modifier =
            modifier
                .height(44.dp)
                .background(
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        },
                    shape = RoundedCornerShape(8.dp),
                ).clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = getAssetTypeDisplayName(assetType),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
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
