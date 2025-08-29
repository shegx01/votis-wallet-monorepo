package finance.votis.wallet.utils

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlatformUtilsTest {
    @Test
    fun platformFlagsAreExclusive() {
        // Platform flags should be mutually exclusive
        assertNotEquals(
            PlatformUtils.isAndroid,
            PlatformUtils.isIos,
            "Platform flags should not both be true or both be false",
        )
    }

    @Test
    fun platformFlagsAreConsistent() {
        // Exactly one platform flag should be true
        val platformFlags = listOf(PlatformUtils.isAndroid, PlatformUtils.isIos)
        val trueFlagsCount = platformFlags.count { it }
        assertTrue(
            trueFlagsCount == 1,
            "Exactly one platform flag should be true, but $trueFlagsCount were true",
        )
    }
}
