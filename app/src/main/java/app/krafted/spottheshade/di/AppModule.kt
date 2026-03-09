package app.krafted.spottheshade.di

import app.krafted.spottheshade.data.repository.PreferencesManager
import app.krafted.spottheshade.data.repository.UserPreferencesRepository
import app.krafted.spottheshade.monetization.MonetizationManager
import app.krafted.spottheshade.monetization.MockMonetizationManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideMonetizationManager(): MonetizationManager {
        return MockMonetizationManager()
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindUserPreferencesRepository(
        preferencesManager: PreferencesManager
    ): UserPreferencesRepository
}
