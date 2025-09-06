package finance.votis.wallet.feature.wallet.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the BottomNavTab enum and navigation logic
 */
class BottomNavigationTest {
    @Test
    fun `test BottomNavTab enum values`() {
        val tabs = BottomNavTab.values()
        assertEquals(5, tabs.size, "Should have exactly 5 navigation tabs")

        assertEquals(BottomNavTab.HOME, tabs[0])
        assertEquals(BottomNavTab.EXCHANGE, tabs[1])
        assertEquals(BottomNavTab.TRANSACTIONS, tabs[2])
        assertEquals(BottomNavTab.BROWSER, tabs[3])
        assertEquals(BottomNavTab.SETTINGS, tabs[4])
    }

    @Test
    fun `test BottomNavTab enum order matches expected navigation order`() {
        // Ensure the enum values are in the expected order for UI consistency
        val expectedOrder =
            listOf(
                BottomNavTab.HOME,
                BottomNavTab.EXCHANGE,
                BottomNavTab.TRANSACTIONS,
                BottomNavTab.BROWSER,
                BottomNavTab.SETTINGS,
            )

        val actualOrder = BottomNavTab.values().toList()
        assertEquals(expectedOrder, actualOrder, "Tab order should match expected navigation sequence")
    }
}
