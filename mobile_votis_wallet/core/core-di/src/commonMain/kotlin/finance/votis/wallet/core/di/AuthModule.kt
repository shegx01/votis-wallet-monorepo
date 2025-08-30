package finance.votis.wallet.core.di

import org.koin.dsl.module

/**
 * Authentication DI module with platform-specific implementations
 */
val authModule =
    module {
        // GoogleAuthClient will be provided by platform modules
        // This is declared here for dependency injection
    }
