package finance.votis.wallet.core.data.repository

import finance.votis.wallet.core.data.datasource.AuthSessionDataSource
import finance.votis.wallet.core.data.storage.SecureStorage
import finance.votis.wallet.core.domain.model.AuthSession
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class AuthRepositoryImplTest {
    // Test implementation of SecureStorage
    private val storage =
        object : SecureStorage {
            private val data = mutableMapOf<String, String>()

            override suspend fun save(
                key: String,
                value: String,
            ) {
                data[key] = value
            }

            override suspend fun read(key: String): String? = data[key]

            override suspend fun delete(key: String) {
                data.remove(key)
            }

            override suspend fun clear() {
                data.clear()
            }
        }

    private val dataSource = AuthSessionDataSource(storage)
    private val currentTime = Instant.fromEpochMilliseconds(1000000000000L)

    // Fixed clock for testing
    private val testClock =
        object : Clock {
            override fun now() = currentTime
        }

    @Test
    fun `isAuthenticated returns true when session is valid`() =
        runTest {
            // Given
            val validSession =
                AuthSession(
                    accessToken = "token",
                    refreshToken = "refresh",
                    expiresAt = currentTime.plus(1.hours),
                    userId = "user123",
                    walletId = "wallet123",
                )

            dataSource.save(validSession)
            val repository = AuthRepositoryImpl(dataSource, testClock)

            // When
            repository.initialize()
            val result = repository.isAuthenticated()

            // Then
            assertTrue(result)
        }

    @Test
    fun `isAuthenticated returns false when session is expired`() =
        runTest {
            // Given
            val expiredSession =
                AuthSession(
                    accessToken = "token",
                    refreshToken = "refresh",
                    expiresAt = currentTime.minus(1.hours),
                    userId = "user123",
                    walletId = "wallet123",
                )

            dataSource.save(expiredSession)
            val repository = AuthRepositoryImpl(dataSource, testClock)

            // When
            repository.initialize()
            val result = repository.isAuthenticated()

            // Then
            assertFalse(result)
        }

    @Test
    fun `isAuthenticated returns false when no session exists`() =
        runTest {
            // Given
            val repository = AuthRepositoryImpl(dataSource, testClock)

            // When
            repository.initialize()
            val result = repository.isAuthenticated()

            // Then
            assertFalse(result)
        }

    @Test
    fun `logout clears session`() =
        runTest {
            // Given
            val validSession =
                AuthSession(
                    accessToken = "token",
                    refreshToken = "refresh",
                    expiresAt = currentTime.plus(1.hours),
                    userId = "user123",
                    walletId = "wallet123",
                )

            dataSource.save(validSession)
            val repository = AuthRepositoryImpl(dataSource, testClock)
            repository.initialize()

            // When
            val result = repository.logout()

            // Then
            assertTrue(result.isSuccess)
            assertFalse(repository.isAuthenticated())
        }
}
