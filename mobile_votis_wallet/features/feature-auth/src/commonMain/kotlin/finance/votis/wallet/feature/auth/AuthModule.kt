package finance.votis.wallet.feature.auth

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Feature auth DI module for authentication-related components
 */
val featureAuthModule =
    module {
        // ViewModel registration - using factoryOf for multiplatform compatibility
        factoryOf(::AuthViewModel)
    }
