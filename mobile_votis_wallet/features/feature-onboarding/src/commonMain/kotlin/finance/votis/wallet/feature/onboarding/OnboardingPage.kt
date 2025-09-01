package finance.votis.wallet.feature.onboarding

import androidx.compose.ui.graphics.Color

/**
 * Represents a single page in the onboarding carousel
 */
data class OnboardingPage(
    val headline: String,
    val subtitle: String,
    val backgroundColor: Color,
)

/**
 * Default onboarding pages for the animated carousel
 */
object OnboardingPages {
    fun getPages(): List<OnboardingPage> =
        listOf(
            OnboardingPage(
                headline = "Welcome to Votis",
                subtitle = "Your secure digital wallet for the future of finance",
                backgroundColor = Color(0xFF6366F1), // Indigo-500
            ),
            OnboardingPage(
                headline = "Safe & Secure",
                subtitle = "Bank-level security with biometric authentication and encrypted storage",
                backgroundColor = Color(0xFF8B5CF6), // Violet-500
            ),
            OnboardingPage(
                headline = "Easy to Use",
                subtitle = "Send, receive, and manage your digital assets with just a few taps",
                backgroundColor = Color(0xFF06B6D4), // Cyan-500
            ),
        )
}
