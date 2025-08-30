package finance.votis.wallet.core.di

import finance.votis.wallet.core.data.storage.AndroidSecureStorage
import finance.votis.wallet.core.data.storage.SecureStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific DI module.
 */
val platformModule = module {
    single<SecureStorage> { AndroidSecureStorage(androidContext()) }
}
