package com.example.spottheshade.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.spottheshade.data.model.ThemeType
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.data.repository.ErrorFeedbackManager
import com.example.spottheshade.data.repository.UserError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Extension property for Context to create DataStore
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val errorFeedbackManager: ErrorFeedbackManager
) {
    
    companion object {
        private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
        private val HIGHEST_LEVEL_KEY = intPreferencesKey("highest_level")
        private val UNLOCKED_THEMES_KEY = stringSetPreferencesKey("unlocked_themes")
        private val CURRENT_THEME_KEY = stringPreferencesKey("current_theme")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val TOTAL_GAMES_PLAYED_KEY = intPreferencesKey("total_games_played")
        private val TOTAL_CORRECT_ANSWERS_KEY = intPreferencesKey("total_correct_answers")
    }
    
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                highScore = preferences[HIGH_SCORE_KEY] ?: 0,
                highestLevel = preferences[HIGHEST_LEVEL_KEY] ?: 1,
                unlockedThemes = preferences[UNLOCKED_THEMES_KEY]?.mapNotNull { 
                    try {
                        ThemeType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        android.util.Log.w("PreferencesManager", "Invalid theme name found: $it", e)
                        errorFeedbackManager.showError(UserError.ThemeCorrupted)
                        null
                    }
                }?.toSet() ?: setOf(ThemeType.DEFAULT),
                currentTheme = preferences[CURRENT_THEME_KEY]?.let { 
                    try {
                        ThemeType.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        android.util.Log.w("PreferencesManager", "Invalid current theme found: $it", e)
                        errorFeedbackManager.showError(UserError.ThemeCorrupted)
                        tryFallbackTheme()
                    }
                } ?: ThemeType.DEFAULT,
                soundEnabled = preferences[SOUND_ENABLED_KEY] ?: true,
                totalGamesPlayed = preferences[TOTAL_GAMES_PLAYED_KEY] ?: 0,
                totalCorrectAnswers = preferences[TOTAL_CORRECT_ANSWERS_KEY] ?: 0
            )
        }
    
    suspend fun updateHighScore(score: Int) {
        try {
            context.dataStore.edit { preferences ->
                val currentHighScore = preferences[HIGH_SCORE_KEY] ?: 0
                if (score > currentHighScore) {
                    preferences[HIGH_SCORE_KEY] = score
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to update high score", e)
            errorFeedbackManager.showError(UserError.PreferencesSaveFailed)
        }
    }
    
    suspend fun updateHighestLevel(level: Int) {
        try {
            context.dataStore.edit { preferences ->
                val currentHighestLevel = preferences[HIGHEST_LEVEL_KEY] ?: 1
                if (level > currentHighestLevel) {
                    preferences[HIGHEST_LEVEL_KEY] = level
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to update highest level", e)
            errorFeedbackManager.showError(UserError.PreferencesSaveFailed)
        }
    }
    
    suspend fun unlockTheme(theme: ThemeType) {
        try {
            context.dataStore.edit { preferences ->
                val currentThemes = preferences[UNLOCKED_THEMES_KEY] ?: emptySet()
                preferences[UNLOCKED_THEMES_KEY] = currentThemes + theme.name
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to unlock theme: $theme", e)
            errorFeedbackManager.showError(UserError.PreferencesSaveFailed)
        }
    }
    
    suspend fun setCurrentTheme(theme: ThemeType) {
        try {
            context.dataStore.edit { preferences ->
                preferences[CURRENT_THEME_KEY] = theme.name
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to set current theme: $theme", e)
            errorFeedbackManager.showError(UserError.ThemeLoadFailed)
        }
    }
    
    suspend fun setSoundEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[SOUND_ENABLED_KEY] = enabled
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to set sound enabled: $enabled", e)
            errorFeedbackManager.showError(UserError.PreferencesSaveFailed)
        }
    }
    
    suspend fun incrementGamesPlayed() {
        try {
            context.dataStore.edit { preferences ->
                val current = preferences[TOTAL_GAMES_PLAYED_KEY] ?: 0
                preferences[TOTAL_GAMES_PLAYED_KEY] = current + 1
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to increment games played", e)
            // Don't show error to user for analytics failures
        }
    }
    
    suspend fun incrementCorrectAnswers() {
        try {
            context.dataStore.edit { preferences ->
                val current = preferences[TOTAL_CORRECT_ANSWERS_KEY] ?: 0
                preferences[TOTAL_CORRECT_ANSWERS_KEY] = current + 1
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to increment correct answers", e)
            // Don't show error to user for analytics failures
        }
    }
    
    suspend fun resetAllData() {
        try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to reset all data", e)
            errorFeedbackManager.showError(UserError.PreferencesSaveFailed)
        }
    }
    
    private fun tryFallbackTheme(): ThemeType? {
        // Try fallback themes in order of preference
        val fallbackThemes = listOf(
            ThemeType.DEFAULT,
            ThemeType.FOREST,
            ThemeType.OCEAN
        )
        
        return fallbackThemes.firstOrNull() ?: ThemeType.DEFAULT
    }
} 