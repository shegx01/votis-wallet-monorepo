package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import finance.votis.wallet.core.domain.model.Nft
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_tune
import mobilevotiswallet.features.feature_wallet.generated.resources.nft_item_content_description
import mobilevotiswallet.features.feature_wallet.generated.resources.no_nfts_message
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * NFT grid component displaying NFTs in a 2-column grid layout.
 * Features rounded corners, proper aspect ratios, and semantic accessibility.
 */
@Composable
fun NftGrid(
    nfts: List<Nft>,
    onNftClick: (Nft) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (nfts.isEmpty()) {
        EmptyNftState(modifier = modifier)
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // NFT Header with filter
            NftHeader()

            // NFT Grid
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "NFT collection grid"
                        },
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Group NFTs into pairs for 2-column layout
                nfts.chunked(2).forEach { rowNfts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowNfts.forEach { nft ->
                            NftCard(
                                nft = nft,
                                onClick = { onNftClick(nft) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // Add spacer if odd number of items in the last row
                        if (rowNfts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } // Close NFT Grid Column
        } // Close main Column
    }
}

/**
 * Individual NFT card component with image, name, and collection info.
 */
@Composable
private fun NftCard(
    nft: Nft,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription =
        stringResource(
            Res.string.nft_item_content_description,
            nft.name,
            nft.collection.name,
        )

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).semantics {
                    this.contentDescription = contentDescription
                },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            ),
    ) {
        Column {
            // NFT Image - slightly less than square to accommodate content below
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f) // More square image for better proportions
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                contentAlignment = Alignment.Center,
            ) {
                // NFT Image Placeholder (TODO: Replace with actual image loading)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🖼️",
                        fontSize = 48.sp,
                    )

                    // For demo purposes, show the first letter of the NFT name
                    Text(
                        text = nft.name.take(1),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.padding(top = 60.dp),
                    )
                }
            }

            // NFT Info
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(6.dp), // Very compact padding
                verticalArrangement = Arrangement.spacedBy(1.dp), // Minimal spacing between text elements
            ) {
                // NFT Name
                Text(
                    text = nft.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Collection Name
                Text(
                    text = nft.collection.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Floor Price (if available) - Floor on left, price on right
                nft.collection.floorPrice?.let { floorPrice ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Floor",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = floorPrice,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/**
 * NFT header component with filter button, similar to TokenList header.
 */
@Composable
private fun NftHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Filter NFTs",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(
            onClick = { /* TODO: Implement NFT filter */ },
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_tune),
                contentDescription = "Filter NFTs",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Empty state component when no NFTs are available.
 */
@Composable
private fun EmptyNftState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Placeholder icon or illustration could go here
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
                text = "🖼️",
                fontSize = 32.sp,
            )
        }

        Text(
            text = stringResource(Res.string.no_nfts_message),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Your NFTs will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
