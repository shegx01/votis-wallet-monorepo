package finance.votis.wallet.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.theme.dimensions

/**
 * A reusable card component designed to wrap tab headers and content.
 *
 * Features:
 * - Consistent visual style with subtle border and no elevation
 * - Proper spacing between header and content sections
 * - Accessibility support with semantic roles
 * - Follows the design system established in wallet components
 *
 * @param content Composable content for the main tab content (e.g., lists, grids)
 * @param modifier Optional modifier for the card container
 * @param header Optional composable content for the tab header section (e.g., title, filters)
 * @param contentDescription Optional content description for accessibility
 */
@Composable
fun TabCard(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Shadow layer
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset(x = 2.dp, y = 2.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                    ),
        )

        // Main card
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Tab
                        contentDescription?.let { this.contentDescription = it }
                    },
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 0.dp,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.dimensions.spacingMedium),
            ) {
                // Optional header section (tabs, titles, filters, etc.)
                header?.let {
                    it()
                    // Spacer between header and content
                    Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingMedium))
                }

                // Content section (lists, grids, etc.)
                content()
            }
        }
    }
}

/*
Usage Examples:

1. Simple content wrapping (most common - tabs separate from card):

```kotlin
// Asset tabs outside the card
AssetTabs(
    selectedTab = selectedAssetType,
    onTabSelected = { selectedAssetType = it }
)

// Content wrapped in card
TabCard(
    content = {
        when (selectedAssetType) {
            AssetType.TOKENS -> TokenList(tokens, onTokenClick = { /* handle */ })
            AssetType.NFTS -> NftGrid(nfts, onNftClick = { /* handle */ })
            AssetType.APPROVALS -> ApprovalsList(approvals)
        }
    },
    contentDescription = "Asset content based on selected tab"
)
```

2. With optional header inside the card:

```kotlin
TabCard(
    header = {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    },
    content = {
        LazyColumn {
            items(activities) { activity ->
                ActivityItem(activity = activity)
            }
            }
        }
    },
    contentDescription = "Recent activity list"
)
```
*/
