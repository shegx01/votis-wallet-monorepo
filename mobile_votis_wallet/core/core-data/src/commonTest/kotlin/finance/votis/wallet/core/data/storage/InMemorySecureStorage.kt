package finance.votis.wallet.core.data.storage

/**
 * In-memory implementation of SecureStorage for testing purposes.
 * This is NOT secure and should only be used for tests.
 */
class InMemorySecureStorage : SecureStorage {
    private val storage = mutableMapOf<String, String>()

    override suspend fun save(
        key: String,
        value: String,
    ) {
        storage[key] = value
    }

    override suspend fun read(key: String): String? = storage[key]

    override suspend fun delete(key: String) {
        storage.remove(key)
    }

    override suspend fun clear() {
        storage.clear()
    }
}
