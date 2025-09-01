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

        // First page - Digital Wallet
        assertEquals("Your Digital Wallet", pages[0].headline)
        assertTrue(pages[0].subtitle.contains("digital assets"))
        assertEquals("WALLET.json", pages[0].animationAsset)

        // Second page - On-Chain Operations
        assertEquals("On-Chain Operations", pages[1].headline)
        assertTrue(pages[1].subtitle.contains("blockchain"))
        assertEquals("ONCHAIN1.json", pages[1].animationAsset)

        // Third page - Seamless Swaps
        assertEquals("Seamless Swaps", pages[2].headline)
        assertTrue(pages[2].subtitle.contains("Exchange"))
        assertEquals("SWAP1.json", pages[2].animationAsset)
    }

    @Test
    fun `OnboardingPages have different background colors`() {
        val pages = OnboardingPages.getPages()

        // All pages should have unique background colors
        val colors = pages.map { it.backgroundColor }
        val uniqueColors = colors.distinct()

        assertEquals(colors.size, uniqueColors.size, "All pages should have unique background colors")
    }

    @Test
    fun `OnboardingPages use correct Lottie animation assets`() {
        val pages = OnboardingPages.getPages()

        val expectedAnimations = listOf("WALLET.json", "ONCHAIN1.json", "SWAP1.json")
        val actualAnimations = pages.map { it.animationAsset }

        assertEquals(expectedAnimations, actualAnimations)
    }
}
