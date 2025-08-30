package finance.votis.wallet.core.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of SecureStorage using EncryptedSharedPreferences.
 */
class AndroidSecureStorage(
    private val context: Context,
) : SecureStorage {
    private val masterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun save(
        key: String,
        value: String,
    ) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, value).apply()
    }

    override suspend fun read(key: String): String? =
        withContext(Dispatchers.IO) {
            prefs.getString(key, null)
        }

    override suspend fun delete(key: String) =
        withContext(Dispatchers.IO) {
            prefs.edit().remove(key).apply()
        }

    override suspend fun clear() =
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
        }

    companion object {
        private const val PREFS_NAME = "votis_secure_storage"
    }
}
