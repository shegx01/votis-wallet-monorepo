package finance.votis.wallet.feature.onboarding.presentation.screen.username

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.components.PrimaryButton
import finance.votis.wallet.core.ui.theme.AppTheme
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors
import finance.votis.wallet.feature.onboarding.presentation.navigation.UserInfo
import finance.votis.wallet.feature.onboarding.ui.components.BulletPoint
import finance.votis.wallet.feature.onboarding.ui.components.UsernameLandingImage
import mobilevotiswallet.features.feature_onboarding.generated.resources.Res
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_bullet1
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_bullet2
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_bullet3
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_bullet4
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_create_button
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_landing_headline
import mobilevotiswallet.features.feature_onboarding.generated.resources.username_landing_subtitle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UsernameLandingScreen(
    userInfo: UserInfo? = null,
    onCreateUsername: () -> Unit = {},
    onSkip: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = MaterialTheme.dimensions.screenHorizontalPadding)
                .safeContentPadding()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))

            UsernameLandingImage(
                modifier = Modifier.padding(bottom = MaterialTheme.dimensions.spacingMedium),
            )

            Text(
                text = stringResource(Res.string.username_landing_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = MaterialTheme.dimensions.spacingMedium),
            )

            Text(
                text = stringResource(Res.string.username_landing_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.votisColors.greyText,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.dimensions.spacingLarge),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp),
                        ).padding(MaterialTheme.dimensions.spacingMedium),
            ) {
                BulletPoint(text = stringResource(Res.string.username_bullet1))
                BulletPoint(text = stringResource(Res.string.username_bullet2))
                BulletPoint(text = stringResource(Res.string.username_bullet3))
                BulletPoint(text = stringResource(Res.string.username_bullet4))
            }
        }

        PrimaryButton(
            text = stringResource(Res.string.username_create_button),
            onClick = onCreateUsername,
            modifier = Modifier.padding(vertical = MaterialTheme.dimensions.spacingLarge),
        )
    }
}

@Preview
@Composable
fun UsernameLandingScreenPreview() {
    AppTheme {
        UsernameLandingScreen()
    }
}
