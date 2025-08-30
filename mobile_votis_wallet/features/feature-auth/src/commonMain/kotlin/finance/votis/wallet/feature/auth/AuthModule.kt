package finance.votis.wallet.feature.auth

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Feature auth DI module for authentication-related components
 */
val featureAuthModule = module {
    // ViewModel registration
    viewModelOf(::AuthViewModel)
}
