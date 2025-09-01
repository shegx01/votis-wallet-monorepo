package finance.votis.wallet.feature.onboarding

import androidx.compose.ui.graphics.Color
import kotlin.test.*

class OnboardingCarouselTest {
    @Test
    fun `OnboardingPage creates correctly with all properties`() {
        val page =
            OnboardingPage(
                headline = "Test Headline",
                subtitle = "Test Subtitle",
                backgroundColor = Color.Blue,
                animationAsset = "TEST.json",
            )

        assertEquals("Test Headline", page.headline)
        assertEquals("Test Subtitle", page.subtitle)
        assertEquals(Color.Blue, page.backgroundColor)
        assertEquals("TEST.json", page.animationAsset)
    }

    @Test
    fun `OnboardingPages provides correct number of pages`() {
        val pages = OnboardingPages.getPages()

        assertEquals(3, pages.size)
    }

    @Test
    fun `OnboardingPages contains expected content`() {
        val pages = OnboardingPages.getPages()

        // First page - Wallet Management
        assertEquals("Own, control and manage", pages[0].headline)
        assertEquals("your asset securely", pages[0].subtitle)
        assertEquals("WALLET.json", pages[0].animationAsset)

        // Second page - On-Chain Operations
        assertEquals("Execute transactions", pages[1].headline)
        assertEquals("directly on-chain with full control", pages[1].subtitle)
        assertEquals("ONCHAIN1.json", pages[1].animationAsset)

        // Third page - Asset Swaps
        assertEquals("Swap assets instantly", pages[2].headline)
        assertEquals("with the best rates available", pages[2].subtitle)
        assertEquals("SWAP1.json", pages[2].animationAsset)
    }

    @Test
    fun `OnboardingPages use consistent transparent backgrounds`() {
        val pages = OnboardingPages.getPages()

        // All pages should use transparent background for clean theme-based design
        pages.forEach { page ->
            assertEquals(Color.Transparent, page.backgroundColor, "All pages should use transparent background")
        }
    }

    @Test
    fun `OnboardingPages use correct Lottie animation assets`() {
        val pages = OnboardingPages.getPages()

        val expectedAnimations = listOf("WALLET.json", "ONCHAIN1.json", "SWAP1.json")
        val actualAnimations = pages.map { it.animationAsset }

        assertEquals(expectedAnimations, actualAnimations)
    }
}
