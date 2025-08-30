package finance.votis.wallet.core.di

import finance.votis.wallet.core.auth.GoogleAuthClient
import org.koin.dsl.module

/**
 * Authentication DI module with platform-specific implementations
 */
val authModule = module {
    // GoogleAuthClient will be provided by platform modules
    // This is declared here for dependency injection
}
