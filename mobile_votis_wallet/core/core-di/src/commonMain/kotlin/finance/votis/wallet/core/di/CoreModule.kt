package finance.votis.wallet.core.di

import finance.votis.wallet.core.data.datasource.AuthSessionDataSource
import finance.votis.wallet.core.data.repository.AuthRepositoryImpl
import finance.votis.wallet.core.domain.repository.AuthRepository
import org.koin.dsl.module

/**
 * Core DI module with common dependencies.
 */
val coreModule = module {
    // Data layer
    single { AuthSessionDataSource(get()) }
    
    // Repository layer
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
