package finance.votis.wallet.core.data.storage

/**
 * Cross-platform secure storage interface for sensitive data.
 * Implementations should use platform-specific secure storage mechanisms:
 * - Android: EncryptedSharedPreferences
 * - iOS: Keychain
 */
interface SecureStorage {
    /**
     * Stores a value securely with the given key.
     */
    suspend fun save(
        key: String,
        value: String,
    )

    /**
     * Retrieves a value by key, returns null if not found.
     */
    suspend fun read(key: String): String?

    /**
     * Deletes a stored value by key.
     */
    suspend fun delete(key: String)

    /**
     * Clears all stored values.
     */
    suspend fun clear()
}
