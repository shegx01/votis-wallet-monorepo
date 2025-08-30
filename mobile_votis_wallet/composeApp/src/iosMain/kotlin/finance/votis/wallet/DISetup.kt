package finance.votis.wallet

import finance.votis.wallet.core.di.coreModule
import finance.votis.wallet.core.di.platformModule
import finance.votis.wallet.di.appModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * Initializes Koin DI for iOS.
 * Called from iOS native code before any Compose content is shown.
 * Safe to call multiple times.
 */
fun initializeKoin() {
    try {
        // Stop any existing Koin instance to avoid conflicts
        stopKoin()
    } catch (e: Exception) {
        // No existing Koin instance, continue
    }

    startKoin {
        modules(
            platformModule,
            coreModule,
            appModule,
        )
    }
}
