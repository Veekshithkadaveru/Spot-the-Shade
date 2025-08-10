package com.example.spottheshade.di

import android.content.Context
import com.example.spottheshade.data.repository.PreferencesManager
import com.example.spottheshade.data.repository.UserPreferencesRepository
import com.example.spottheshade.services.SoundManager
import com.example.spottheshade.game.GameLogicManager
import com.example.spottheshade.monetization.MonetizationManager
import com.example.spottheshade.monetization.MockMonetizationManager
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