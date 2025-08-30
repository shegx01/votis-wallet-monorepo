package finance.votis.wallet.di

import finance.votis.wallet.AppViewModel
import org.koin.dsl.module

/**
 * App-level DI module with ViewModels.
 */
val appModule =
    module {
        single { AppViewModel(get()) }
    }
