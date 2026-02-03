package app.krafted.spottheshade.di

import android.content.Context
import app.krafted.spottheshade.data.repository.PreferencesManager
import app.krafted.spottheshade.data.repository.UserPreferencesRepository
import app.krafted.spottheshade.services.SoundManager
import app.krafted.spottheshade.game.GameLogicManager
import app.krafted.spottheshade.monetization.MonetizationManager
import app.krafted.spottheshade.monetization.MockMonetizationManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideGameLogicManager(): GameLogicManager {
        return GameLogicManager()
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
