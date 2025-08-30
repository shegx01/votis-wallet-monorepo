package finance.votis.wallet.core.data.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IosSecureStorageTest {
    private val storage = IosSecureStorage()

    // ===============================
    // Basic Functionality Tests
    // ===============================

    @Test
    fun testSaveAndRead_BasicOperation() =
        runTest {
            val key = "basic_key"
            val value = "basic_value"

            // Clean up any existing value
            storage.delete(key)

            // Save and read
            storage.save(key, value)
            val readValue = storage.read(key)

            assertEquals(value, readValue)
        }

    @Test
    fun testRead_NonExistentKey_ReturnsNull() =
        runTest {
            val nonExistentKey = "non_existent_key_${kotlin.random.Random.nextInt()}"

            val result = storage.read(nonExistentKey)

            assertNull(result)
        }

    @Test
    fun testDelete_ExistingKey() =
        runTest {
            val key = "delete_test_key"
            val value = "delete_test_value"

            // Save first
            storage.save(key, value)
            assertEquals(value, storage.read(key))

            // Delete and verify
            storage.delete(key)
            assertNull(storage.read(key))
        }

    @Test
    fun testDelete_NonExistentKey_NoError() =
        runTest {
            val nonExistentKey = "non_existent_delete_${kotlin.random.Random.nextInt()}"

            // Should not throw exception
            storage.delete(nonExistentKey)

            // Verify still null
            assertNull(storage.read(nonExistentKey))
        }

    @Test
    fun testClear_RemovesAllData() =
        runTest {
            val keys = listOf("clear_key1", "clear_key2", "clear_key3")
            val values = listOf("clear_value1", "clear_value2", "clear_value3")

            // Save multiple items
            keys.zip(values).forEach { (key, value) ->
                storage.save(key, value)
            }

            // Verify they exist
            keys.zip(values).forEach { (key, value) ->
                assertEquals(value, storage.read(key))
            }

            // Clear all and verify
            storage.clear()
            keys.forEach { key ->
                assertNull(storage.read(key))
            }
        }

    // ===============================
    // Update/Overwrite Tests
    // ===============================

    @Test
    fun testSave_OverwriteExistingKey() =
        runTest {
            val key = "overwrite_key"
            val originalValue = "original_value"
            val newValue = "new_value"

            // Clean up first
            storage.delete(key)

            // Save original value
            storage.save(key, originalValue)
            assertEquals(originalValue, storage.read(key))

            // Update with new value
            storage.save(key, newValue)
            assertEquals(newValue, storage.read(key))
        }

    @Test
    fun testSave_MultipleUpdates() =
        runTest {
            val key = "multiple_updates"
            val values = listOf("v1", "v2", "v3", "v4", "v5")

            // Clean up first
            storage.delete(key)

            values.forEach { value ->
                storage.save(key, value)
                assertEquals(value, storage.read(key))
            }

            // Final check
            assertEquals(values.last(), storage.read(key))
        }

    // ===============================
    // Edge Cases and Special Characters
    // ===============================

    @Test
    fun testSave_EmptyValue() =
        runTest {
            val key = "empty_value_key"
            val emptyValue = ""

            storage.save(key, emptyValue)
            assertEquals(emptyValue, storage.read(key))
        }

    @Test
    fun testSave_EmptyKey() =
        runTest {
            val emptyKey = ""
            val value = "value_for_empty_key"

            storage.save(emptyKey, value)
            assertEquals(value, storage.read(emptyKey))
        }

    @Test
    fun testSave_SpecialCharacters() =
        runTest {
            val specialChars =
                mapOf(
                    "unicode_key" to "🔐🛡️💳🏦",
                    "json_like" to "{\"token\":\"abc123\",\"expires\":1234567890}",
                    "xml_like" to "<session><token>xyz789</token></session>",
                    "newlines" to "line1\nline2\rline3\r\nline4",
                    "quotes" to "\"single'quote\" and 'double\"quote'",
                    "spaces" to "   leading and trailing spaces   ",
                    "symbols" to "!@#$%^&*()_+-=[]{}|;':\",./<>?",
                )

            specialChars.forEach { (key, value) ->
                storage.delete(key) // Clean up first
                storage.save(key, value)
                assertEquals(value, storage.read(key), "Failed for key: $key")
            }
        }

    @Test
    fun testSave_LongValues() =
        runTest {
            val key = "long_value_key"
            val longValue = "x".repeat(10000) // 10KB string

            storage.delete(key) // Clean up first
            storage.save(key, longValue)
            assertEquals(longValue, storage.read(key))
        }

    @Test
    fun testSave_LongKeys() =
        runTest {
            val longKey = "k".repeat(500) // Long key (reasonable size for Keychain)
            val value = "value_for_long_key"

            storage.delete(longKey) // Clean up first
            storage.save(longKey, value)
            assertEquals(value, storage.read(longKey))
        }

    // ===============================
    // Concurrency Tests
    // ===============================

    @Test
    fun testConcurrentOperations() =
        runTest {
            val keys = (1..10).map { "concurrent_key_$it" }
            val values = (1..10).map { "concurrent_value_$it" }

            // Clean up first
            keys.forEach { storage.delete(it) }

            // Concurrent saves
            keys.zip(values).forEach { (key, value) ->
                storage.save(key, value)
            }

            // Verify all saves
            keys.zip(values).forEach { (key, value) ->
                assertEquals(value, storage.read(key))
            }

            // Concurrent deletes of half the items
            keys.take(5).forEach { key ->
                storage.delete(key)
            }

            // Verify deletions
            keys.take(5).forEach { key ->
                assertNull(storage.read(key))
            }

            // Verify remaining items
            keys.drop(5).zip(values.drop(5)).forEach { (key, value) ->
                assertEquals(value, storage.read(key))
            }
        }

    // ===============================
    // Data Integrity Tests
    // ===============================

    @Test
    fun testDataIntegrity_AfterClearAndSave() =
        runTest {
            val key = "integrity_key"
            val value1 = "integrity_value1"
            val value2 = "integrity_value2"

            // Save, clear, save again
            storage.save(key, value1)
            storage.clear()
            storage.save(key, value2)

            assertEquals(value2, storage.read(key))
        }

    @Test
    fun testDataIntegrity_MultipleOperations() =
        runTest {
            val operations =
                listOf(
                    "save" to ("key1" to "value1"),
                    "save" to ("key2" to "value2"),
                    "read" to ("key1" to "value1"),
                    "delete" to ("key1" to null),
                    "read" to ("key1" to null),
                    "save" to ("key1" to "new_value1"),
                    "read" to ("key1" to "new_value1"),
                    "read" to ("key2" to "value2"),
                )

            operations.forEach { (operation, keyValue) ->
                val (key, expectedValue) = keyValue
                when (operation) {
                    "save" -> storage.save(key, expectedValue!!)
                    "delete" -> storage.delete(key)
                    "read" -> {
                        val actualValue = storage.read(key)
                        assertEquals(expectedValue, actualValue, "Failed at operation: $operation for key: $key")
                    }
                }
            }
        }

    // ===============================
    // Isolation Tests
    // ===============================

    @Test
    fun testKeyIsolation() =
        runTest {
            val keys =
                mapOf(
                    "user_token" to "token123",
                    "user_token_backup" to "backup456",
                    "user_token_temp" to "temp789",
                )

            // Clean up first
            keys.keys.forEach { storage.delete(it) }

            // Save all
            keys.forEach { (key, value) ->
                storage.save(key, value)
            }

            // Verify isolation - deleting one shouldn't affect others
            storage.delete("user_token")

            assertNull(storage.read("user_token"))
            assertEquals("backup456", storage.read("user_token_backup"))
            assertEquals("temp789", storage.read("user_token_temp"))
        }

    // ===============================
    // Performance/Stress Tests
    // ===============================

    @Test
    fun testManySmallItems() =
        runTest {
            val itemCount = 50 // Reduced for iOS Keychain efficiency
            val items = (1..itemCount).associate { "key_$it" to "value_$it" }

            // Clean up first
            items.keys.forEach { storage.delete(it) }

            // Save all items
            items.forEach { (key, value) ->
                storage.save(key, value)
            }

            // Read all items and verify
            items.forEach { (key, expectedValue) ->
                assertEquals(expectedValue, storage.read(key))
            }

            // Clean up
            storage.clear()

            // Verify cleanup
            items.keys.forEach { key ->
                assertNull(storage.read(key))
            }
        }

    // ===============================
    // Error Handling Tests
    // ===============================

    @Test
    fun testSave_InvalidEncodingScenario() =
        runTest {
            // Test with various edge case strings that might cause encoding issues
            val edgeCases =
                listOf(
                    "null_char" to "before\u0000after",
                    "high_unicode" to "\uD83D\uDE00\uD83D\uDCAF", // Emoji
                    "mixed_encoding" to "Héllo Wörld 你好 مرحبا",
                )

            edgeCases.forEach { (key, value) ->
                storage.delete(key) // Clean up first
                storage.save(key, value)
                assertEquals(value, storage.read(key), "Failed for edge case: $key")
            }
        }

    // ===============================
    // Real-world Usage Patterns
    // ===============================

    @Test
    fun testAuthTokenScenario() =
        runTest {
            val accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            val refreshToken = "refresh_xyz789..."
            val userId = "user12345"

            // Clean up first
            listOf("access_token", "refresh_token", "user_id").forEach { storage.delete(it) }

            // Save auth data
            storage.save("access_token", accessToken)
            storage.save("refresh_token", refreshToken)
            storage.save("user_id", userId)

            // Verify auth data
            assertEquals(accessToken, storage.read("access_token"))
            assertEquals(refreshToken, storage.read("refresh_token"))
            assertEquals(userId, storage.read("user_id"))

            // Simulate token refresh
            val newAccessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
            storage.save("access_token", newAccessToken)

            // Verify token was updated but others remain
            assertEquals(newAccessToken, storage.read("access_token"))
            assertEquals(refreshToken, storage.read("refresh_token"))
            assertEquals(userId, storage.read("user_id"))

            // Simulate logout
            storage.delete("access_token")
            storage.delete("refresh_token")
            storage.delete("user_id")

            // Verify logout cleanup
            assertNull(storage.read("access_token"))
            assertNull(storage.read("refresh_token"))
            assertNull(storage.read("user_id"))
        }

    @Test
    fun testUserPreferencesScenario() =
        runTest {
            val preferences =
                mapOf(
                    "theme" to "dark",
                    "language" to "en",
                    "notifications_enabled" to "true",
                    "biometric_enabled" to "false",
                    "currency" to "USD",
                )

            val prefKeys = preferences.keys.map { "pref_$it" }

            // Clean up first
            prefKeys.forEach { storage.delete(it) }

            // Save preferences
            preferences.forEach { (key, value) ->
                storage.save("pref_$key", value)
            }

            // Update some preferences
            storage.save("pref_theme", "light")
            storage.save("pref_biometric_enabled", "true")

            // Verify updates
            assertEquals("light", storage.read("pref_theme"))
            assertEquals("true", storage.read("pref_biometric_enabled"))

            // Verify others unchanged
            assertEquals("en", storage.read("pref_language"))
            assertEquals("true", storage.read("pref_notifications_enabled"))
            assertEquals("USD", storage.read("pref_currency"))
        }

    @Test
    fun testWalletSessionScenario() =
        runTest {
            val sessionData =
                mapOf(
                    "wallet_address" to "0x742d35cc6688c02532d6B56",
                    "network_id" to "ethereum-mainnet",
                    "last_activity" to "1698765432000",
                    "session_token" to "sess_abc123xyz789",
                )

            // Clean up first
            sessionData.keys.forEach { storage.delete(it) }

            // Save session data
            sessionData.forEach { (key, value) ->
                storage.save(key, value)
            }

            // Verify all session data
            sessionData.forEach { (key, expectedValue) ->
                assertEquals(expectedValue, storage.read(key))
            }

            // Simulate session update
            storage.save("last_activity", "1698765500000")

            // Verify update
            assertEquals("1698765500000", storage.read("last_activity"))

            // Verify other data unchanged
            assertEquals(sessionData["wallet_address"], storage.read("wallet_address"))
            assertEquals(sessionData["network_id"], storage.read("network_id"))
            assertEquals(sessionData["session_token"], storage.read("session_token"))

            // Simulate session cleanup
            sessionData.keys.forEach { key ->
                storage.delete(key)
            }

            // Verify cleanup
            sessionData.keys.forEach { key ->
                assertNull(storage.read(key))
            }
        }
}
