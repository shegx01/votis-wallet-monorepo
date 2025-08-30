package finance.votis.wallet.core.di

import finance.votis.wallet.core.auth.AndroidGoogleAuthClient
import finance.votis.wallet.core.auth.GoogleAuthClient
import finance.votis.wallet.core.data.storage.AndroidSecureStorage
import finance.votis.wallet.core.data.storage.SecureStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific DI module.
 */
val platformModule =
    module {
        single<SecureStorage> { AndroidSecureStorage(androidContext()) }
        
        // Note: AndroidGoogleAuthClient requires ComponentActivity
        // This will need to be registered where the Activity is available
        // For now, we'll provide a factory that can be used with the Activity
        factory<GoogleAuthClient> { (activity: androidx.activity.ComponentActivity) ->
            AndroidGoogleAuthClient(androidContext(), activity)
        }
    }
