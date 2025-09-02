package finance.votis.wallet.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import mobilevotiswallet.features.feature_onboarding.generated.resources.*
import mobilevotiswallet.features.feature_onboarding.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

/**
 * Represents a single page in the onboarding carousel
 */
data class OnboardingPage(
    val headline: String,
    val subtitle: String,
    val backgroundColor: Color,
    val animationAsset: String, // Lottie animation file name
)

/**
 * Default onboarding pages for the animated carousel
 * Based on WALLET.json, ONCHAIN1.json, and SWAP1.json animations
 */
object OnboardingPages {
    @Composable
    fun getPages(): List<OnboardingPage> =
        listOf(
            OnboardingPage(
                headline = stringResource(Res.string.onboarding_page1_headline),
                subtitle = stringResource(Res.string.onboarding_page1_subtitle),
                backgroundColor = Color.Transparent, // Use theme background
                animationAsset = "WALLET.json",
            ),
            OnboardingPage(
                headline = stringResource(Res.string.onboarding_page2_headline),
                subtitle = stringResource(Res.string.onboarding_page2_subtitle),
                backgroundColor = Color.Transparent, // Use theme background
                animationAsset = "ONCHAIN1.json",
            ),
            OnboardingPage(
                headline = stringResource(Res.string.onboarding_page3_headline),
                subtitle = stringResource(Res.string.onboarding_page3_subtitle),
                backgroundColor = Color.Transparent, // Use theme background
                animationAsset = "SWAP1.json",
            ),
        )
}
