package app.krafted.spottheshade.data.repository

import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>

    suspend fun updateHighScore(score: Int)
    suspend fun updateHighestLevel(level: Int)
    suspend fun unlockTheme(theme: ThemeType)
    suspend fun setCurrentTheme(theme: ThemeType)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun incrementGamesPlayed()
    suspend fun incrementCorrectAnswers()
}
