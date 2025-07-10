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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for Context to create DataStore
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {
    
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
                unlockedThemes = preferences[UNLOCKED_THEMES_KEY]?.map { 
                    ThemeType.valueOf(it) 
                }?.toSet() ?: setOf(ThemeType.DEFAULT),
                currentTheme = preferences[CURRENT_THEME_KEY]?.let { 
                    ThemeType.valueOf(it) 
                } ?: ThemeType.DEFAULT,
                soundEnabled = preferences[SOUND_ENABLED_KEY] ?: true,
                totalGamesPlayed = preferences[TOTAL_GAMES_PLAYED_KEY] ?: 0,
                totalCorrectAnswers = preferences[TOTAL_CORRECT_ANSWERS_KEY] ?: 0
            )
        }
    
    suspend fun updateHighScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentHighScore = preferences[HIGH_SCORE_KEY] ?: 0
            if (score > currentHighScore) {
                preferences[HIGH_SCORE_KEY] = score
            }
        }
    }
    
    suspend fun updateHighestLevel(level: Int) {
        context.dataStore.edit { preferences ->
            val currentHighestLevel = preferences[HIGHEST_LEVEL_KEY] ?: 1
            if (level > currentHighestLevel) {
                preferences[HIGHEST_LEVEL_KEY] = level
            }
        }
    }
    
    suspend fun unlockTheme(theme: ThemeType) {
        context.dataStore.edit { preferences ->
            val currentThemes = preferences[UNLOCKED_THEMES_KEY] ?: emptySet()
            preferences[UNLOCKED_THEMES_KEY] = currentThemes + theme.name
        }
    }
    
    suspend fun setCurrentTheme(theme: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_THEME_KEY] = theme.name
        }
    }
    
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun incrementGamesPlayed() {
        context.dataStore.edit { preferences ->
            val current = preferences[TOTAL_GAMES_PLAYED_KEY] ?: 0
            preferences[TOTAL_GAMES_PLAYED_KEY] = current + 1
        }
    }
    
    suspend fun incrementCorrectAnswers() {
        context.dataStore.edit { preferences ->
            val current = preferences[TOTAL_CORRECT_ANSWERS_KEY] ?: 0
            preferences[TOTAL_CORRECT_ANSWERS_KEY] = current + 1
        }
    }
    
    suspend fun resetAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 