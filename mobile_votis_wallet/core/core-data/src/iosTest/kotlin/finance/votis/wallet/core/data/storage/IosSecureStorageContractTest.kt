package finance.votis.wallet.core.data.storage

import kotlin.test.Test

/**
 * Contract tests for IosSecureStorage implementation.
 * Ensures the iOS implementation adheres to the SecureStorage contract.
 */
class IosSecureStorageContractTest : SecureStorageContract() {
    override fun createSecureStorage(): SecureStorage = IosSecureStorage()

    // ===============================
    // Basic Contract Tests
    // ===============================

    @Test
    fun contractTest_BasicSaveAndRead() = testBasicSaveAndRead()

    @Test
    fun contractTest_ReadNonExistentKeyReturnsNull() = testReadNonExistentKeyReturnsNull()

    @Test
    fun contractTest_DeleteExistingKey() = testDeleteExistingKey()

    @Test
    fun contractTest_DeleteNonExistentKeyDoesNotThrow() = testDeleteNonExistentKeyDoesNotThrow()

    @Test
    fun contractTest_ClearRemovesAllData() = testClearRemovesAllData()

    @Test
    fun contractTest_OverwriteExistingKey() = testOverwriteExistingKey()

    @Test
    fun contractTest_EmptyValues() = testEmptyValues()

    @Test
    fun contractTest_EmptyKeys() = testEmptyKeys()

    @Test
    fun contractTest_SpecialCharacters() = testSpecialCharacters()

    @Test
    fun contractTest_MultipleKeysIsolation() = testMultipleKeysIsolation()

    @Test
    fun contractTest_DataPersistsThroughOperations() = testDataPersistsThroughOperations()

    @Test
    fun contractTest_LargeValues() = testLargeValues()

    // ===============================
    // Real-world Scenario Tests
    // ===============================

    @Test
    fun contractTest_AuthenticationScenario() = testAuthenticationScenario()

    @Test
    fun contractTest_UserPreferencesScenario() = testUserPreferencesScenario()

    @Test
    fun contractTest_SessionManagementScenario() = testSessionManagementScenario()
}
