package finance.votis.wallet.core.data.datasource

import finance.votis.wallet.core.data.storage.SecureStorage
import finance.votis.wallet.core.domain.model.AuthSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data source for persisting authentication sessions securely.
 */
class AuthSessionDataSource(
    private val secureStorage: SecureStorage,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }

    /**
     * Saves authentication session to secure storage.
     */
    suspend fun save(session: AuthSession) {
        val sessionJson = json.encodeToString(session)
        secureStorage.save(AUTH_SESSION_KEY, sessionJson)
    }

    /**
     * Retrieves authentication session from secure storage.
     * Returns null if no session exists or if deserialization fails.
     */
    suspend fun read(): AuthSession? =
        try {
            val sessionJson = secureStorage.read(AUTH_SESSION_KEY)
            sessionJson?.let { json.decodeFromString<AuthSession>(it) }
        } catch (e: Exception) {
            // If deserialization fails, the session is corrupted, clear it
            clear()
            null
        }

    /**
     * Clears the stored authentication session.
     */
    suspend fun clear() {
        secureStorage.delete(AUTH_SESSION_KEY)
    }

    companion object {
        private const val AUTH_SESSION_KEY = "auth_session"
    }
}
