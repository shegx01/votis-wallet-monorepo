package finance.votis.wallet.feature.onboarding.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.theme.AppTheme
import mobilevotiswallet.features.feature_onboarding.generated.resources.Res
import mobilevotiswallet.features.feature_onboarding.generated.resources.username
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_landing_image_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UsernameLandingImage(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.username),
            contentDescription = stringResource(Res.string.username_landing_image_description),
            modifier = Modifier.size(120.dp),
        )
    }
}

@Preview
@Composable
private fun UsernameLandingImagePreview() {
    AppTheme {
        UsernameLandingImage()
    }
}
