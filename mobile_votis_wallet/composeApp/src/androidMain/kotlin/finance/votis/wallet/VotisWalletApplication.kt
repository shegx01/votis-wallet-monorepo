package finance.votis.wallet

import android.app.Application
import finance.votis.wallet.core.di.coreModule
import finance.votis.wallet.core.di.platformModule
import finance.votis.wallet.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VotisWalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@VotisWalletApplication)
            modules(
                platformModule,
                coreModule,
                appModule,
            )
        }
    }
}
