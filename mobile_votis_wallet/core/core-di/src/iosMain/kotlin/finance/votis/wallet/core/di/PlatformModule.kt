package finance.votis.wallet.core.di

import finance.votis.wallet.core.data.storage.IosSecureStorage
import finance.votis.wallet.core.data.storage.SecureStorage
import org.koin.dsl.module

/**
 * iOS-specific DI module.
 */
val platformModule =
    module {
        single<SecureStorage> { IosSecureStorage() }
    }
