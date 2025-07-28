package com.example.spottheshade.di

import android.content.Context
import com.example.spottheshade.data.repository.PreferencesManager
import com.example.spottheshade.data.repository.SoundManager
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

    // Note: PreferencesManager, SoundManager, ErrorFeedbackManager, and NavigationHelper
    // are now @Singleton classes with @Inject constructors, so Hilt will provide them automatically.
    // No manual @Provides methods needed anymore.
}