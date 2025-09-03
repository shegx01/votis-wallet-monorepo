package finance.votis.wallet.feature.onboarding.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors
import mobilevotiswallet.features.feature_onboarding.generated.resources.Res
import mobilevotiswallet.features.feature_onboarding.generated.resources.ic_check
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_check_icon_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun BulletPoint(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_check),
            contentDescription = stringResource(Res.string.username_check_icon_description),
            tint = MaterialTheme.votisColors.brand,
            modifier = Modifier.size(MaterialTheme.dimensions.iconSize),
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun BulletPointPreview() {
    MaterialTheme {
        BulletPoint(text = "Use your @username instead of long wallet addresses.")
    }
}
