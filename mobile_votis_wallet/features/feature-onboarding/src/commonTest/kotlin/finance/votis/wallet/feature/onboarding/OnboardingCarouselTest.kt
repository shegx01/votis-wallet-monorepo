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
            )

        assertEquals("Test Headline", page.headline)
        assertEquals("Test Subtitle", page.subtitle)
        assertEquals(Color.Blue, page.backgroundColor)
    }

    @Test
    fun `OnboardingPages provides correct number of pages`() {
        val pages = OnboardingPages.getPages()

        assertEquals(3, pages.size)
    }

    @Test
    fun `OnboardingPages contains expected content`() {
        val pages = OnboardingPages.getPages()

        // First page
        assertEquals("Welcome to Votis", pages[0].headline)
        assertTrue(pages[0].subtitle.contains("secure digital wallet"))

        // Second page
        assertEquals("Safe & Secure", pages[1].headline)
        assertTrue(pages[1].subtitle.contains("Bank-level security"))

        // Third page
        assertEquals("Easy to Use", pages[2].headline)
        assertTrue(pages[2].subtitle.contains("Send, receive, and manage"))
    }

    @Test
    fun `OnboardingPages have different background colors`() {
        val pages = OnboardingPages.getPages()

        // All pages should have unique background colors
        val colors = pages.map { it.backgroundColor }
        val uniqueColors = colors.distinct()

        assertEquals(colors.size, uniqueColors.size, "All pages should have unique background colors")
    }
}
