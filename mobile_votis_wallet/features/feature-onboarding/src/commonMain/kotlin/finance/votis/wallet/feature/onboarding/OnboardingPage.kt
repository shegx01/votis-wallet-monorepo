package finance.votis.wallet.feature.onboarding

import androidx.compose.ui.graphics.Color
import finance.votis.wallet.core.ui.theme.VotisBrand
import finance.votis.wallet.core.ui.theme.VotisBrandDark
import finance.votis.wallet.core.ui.theme.primaryLight

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
    fun getPages(): List<OnboardingPage> =
        listOf(
            OnboardingPage(
                headline = "Your Digital Wallet",
                subtitle = "Store, manage and secure your digital assets with enterprise-grade protection",
                backgroundColor = VotisBrand,
                animationAsset = "WALLET.json",
            ),
            OnboardingPage(
                headline = "On-Chain Operations",
                subtitle = "Execute transactions directly on the blockchain with full transparency and control",
                backgroundColor = VotisBrandDark,
                animationAsset = "ONCHAIN1.json",
            ),
            OnboardingPage(
                headline = "Seamless Swaps",
                subtitle = "Exchange digital assets instantly with the best rates and minimal fees",
                backgroundColor = primaryLight,
                animationAsset = "SWAP1.json",
            ),
        )
}
