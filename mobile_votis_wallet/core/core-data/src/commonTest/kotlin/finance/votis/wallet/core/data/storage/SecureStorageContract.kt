package finance.votis.wallet.core.data.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Contract tests that verify the behavior of any SecureStorage implementation.
 * This ensures consistency across Android and iOS implementations.
 */
abstract class SecureStorageContract {
    /**
     * Returns the SecureStorage implementation to test.
     * Each platform should provide its own implementation.
     */
    abstract fun createSecureStorage(): SecureStorage

    /**
     * Optional setup before each test.
     * Default implementation clears all data.
     */
    open suspend fun setUp(storage: SecureStorage) {
        storage.clear()
    }

    /**
     * Optional cleanup after each test.
     * Default implementation clears all data.
     */
    open suspend fun tearDown(storage: SecureStorage) {
        storage.clear()
    }

    // ===============================
    // Contract Tests
    // ===============================

    fun testBasicSaveAndRead() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val key = "test_key"
            val value = "test_value"

            storage.save(key, value)
            val result = storage.read(key)

            assertEquals(value, result)
            tearDown(storage)
        }

    fun testReadNonExistentKeyReturnsNull() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val nonExistentKey = "non_existent_key_${kotlin.random.Random.nextInt()}"
            val result = storage.read(nonExistentKey)

            assertNull(result)
            tearDown(storage)
        }

    fun testDeleteExistingKey() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val key = "delete_key"
            val value = "delete_value"

            // Save first
            storage.save(key, value)
            assertEquals(value, storage.read(key))

            // Delete and verify
            storage.delete(key)
            assertNull(storage.read(key))

            tearDown(storage)
        }

    fun testDeleteNonExistentKeyDoesNotThrow() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val nonExistentKey = "non_existent_delete_${kotlin.random.Random.nextInt()}"

            // Should not throw exception
            storage.delete(nonExistentKey)
            assertNull(storage.read(nonExistentKey))

            tearDown(storage)
        }

    fun testClearRemovesAllData() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val testData =
                mapOf(
                    "key1" to "value1",
                    "key2" to "value2",
                    "key3" to "value3",
                )

            // Save all items
            testData.forEach { (key, value) ->
                storage.save(key, value)
            }

            // Verify they exist
            testData.forEach { (key, value) ->
                assertEquals(value, storage.read(key))
            }

            // Clear all
            storage.clear()

            // Verify all are gone
            testData.keys.forEach { key ->
                assertNull(storage.read(key))
            }

            tearDown(storage)
        }

    fun testOverwriteExistingKey() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val key = "overwrite_key"
            val originalValue = "original"
            val newValue = "updated"

            // Save original
            storage.save(key, originalValue)
            assertEquals(originalValue, storage.read(key))

            // Overwrite
            storage.save(key, newValue)
            assertEquals(newValue, storage.read(key))

            tearDown(storage)
        }

    fun testEmptyValues() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val key = "empty_key"
            val emptyValue = ""

            storage.save(key, emptyValue)
            assertEquals(emptyValue, storage.read(key))

            tearDown(storage)
        }

    fun testEmptyKeys() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val emptyKey = ""
            val value = "value"

            storage.save(emptyKey, value)
            assertEquals(value, storage.read(emptyKey))

            tearDown(storage)
        }

    fun testSpecialCharacters() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val specialCases =
                mapOf(
                    "json" to "{\"key\":\"value\",\"number\":123}",
                    "xml" to "<root><item>value</item></root>",
                    "newlines" to "line1\nline2\rline3\r\nline4",
                    "quotes" to "\"double\" and 'single' quotes",
                    "unicode" to "Unicode: 你好 🌍 café",
                    "symbols" to "!@#$%^&*()_+-=[]{}|;':\",./<>?",
                )

            specialCases.forEach { (key, value) ->
                storage.save(key, value)
                assertEquals(value, storage.read(key), "Failed for: $key")
            }

            tearDown(storage)
        }

    fun testMultipleKeysIsolation() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val keys =
                mapOf(
                    "user_token" to "token123",
                    "user_refresh" to "refresh456",
                    "user_session" to "session789",
                )

            // Save all
            keys.forEach { (key, value) ->
                storage.save(key, value)
            }

            // Delete one key
            storage.delete("user_token")

            // Verify isolation
            assertNull(storage.read("user_token"))
            assertEquals("refresh456", storage.read("user_refresh"))
            assertEquals("session789", storage.read("user_session"))

            tearDown(storage)
        }

    fun testDataPersistsThroughOperations() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val persistentKey = "persistent_key"
            val persistentValue = "persistent_value"
            val temporaryKey = "temp_key"
            val temporaryValue = "temp_value"

            // Save both
            storage.save(persistentKey, persistentValue)
            storage.save(temporaryKey, temporaryValue)

            // Delete temporary
            storage.delete(temporaryKey)

            // Verify persistent key still exists
            assertEquals(persistentValue, storage.read(persistentKey))
            assertNull(storage.read(temporaryKey))

            // Save another temporary key
            storage.save(temporaryKey, "new_temp_value")

            // Verify both exist now
            assertEquals(persistentValue, storage.read(persistentKey))
            assertEquals("new_temp_value", storage.read(temporaryKey))

            tearDown(storage)
        }

    fun testLargeValues() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            val key = "large_value_key"
            val largeValue = "x".repeat(1000) // 1KB value

            storage.save(key, largeValue)
            assertEquals(largeValue, storage.read(key))

            tearDown(storage)
        }

    // ===============================
    // Real-world Scenarios
    // ===============================

    fun testAuthenticationScenario() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            // Initial login
            storage.save("access_token", "token_abc123")
            storage.save("refresh_token", "refresh_xyz789")
            storage.save("user_id", "user_12345")
            storage.save("expires_at", "1698765432000")

            // Verify login data
            assertEquals("token_abc123", storage.read("access_token"))
            assertEquals("refresh_xyz789", storage.read("refresh_token"))
            assertEquals("user_12345", storage.read("user_id"))
            assertEquals("1698765432000", storage.read("expires_at"))

            // Token refresh
            storage.save("access_token", "new_token_def456")
            storage.save("expires_at", "1698765500000")

            // Verify refresh kept other data
            assertEquals("new_token_def456", storage.read("access_token"))
            assertEquals("refresh_xyz789", storage.read("refresh_token"))
            assertEquals("user_12345", storage.read("user_id"))
            assertEquals("1698765500000", storage.read("expires_at"))

            // Logout - clear all auth data
            listOf("access_token", "refresh_token", "user_id", "expires_at").forEach {
                storage.delete(it)
            }

            // Verify logout cleanup
            assertNull(storage.read("access_token"))
            assertNull(storage.read("refresh_token"))
            assertNull(storage.read("user_id"))
            assertNull(storage.read("expires_at"))

            tearDown(storage)
        }

    fun testUserPreferencesScenario() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            // Save initial preferences
            val preferences =
                mapOf(
                    "pref_theme" to "dark",
                    "pref_language" to "en",
                    "pref_notifications" to "true",
                    "pref_biometric" to "false",
                )

            preferences.forEach { (key, value) ->
                storage.save(key, value)
            }

            // Update some preferences
            storage.save("pref_theme", "light")
            storage.save("pref_biometric", "true")

            // Verify selective updates
            assertEquals("light", storage.read("pref_theme"))
            assertEquals("en", storage.read("pref_language"))
            assertEquals("true", storage.read("pref_notifications"))
            assertEquals("true", storage.read("pref_biometric"))

            tearDown(storage)
        }

    fun testSessionManagementScenario() =
        runTest {
            val storage = createSecureStorage()
            setUp(storage)

            // Save session data
            storage.save("session_id", "sess_abc123")
            storage.save("last_activity", "1698765432000")
            storage.save("device_id", "device_xyz789")

            // Update activity timestamp
            storage.save("last_activity", "1698765500000")

            // Verify session data integrity
            assertEquals("sess_abc123", storage.read("session_id"))
            assertEquals("1698765500000", storage.read("last_activity"))
            assertEquals("device_xyz789", storage.read("device_id"))

            // Session timeout - clear session but keep device
            storage.delete("session_id")
            storage.delete("last_activity")

            // Verify partial cleanup
            assertNull(storage.read("session_id"))
            assertNull(storage.read("last_activity"))
            assertEquals("device_xyz789", storage.read("device_id"))

            tearDown(storage)
        }
}
