package app.krafted.spottheshade.game

import app.krafted.spottheshade.BuildConfig
import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.data.model.isUnlockConditionMet
import app.krafted.spottheshade.data.repository.UserPreferencesRepository
import app.krafted.spottheshade.monetization.MonetizationManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ThemeManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val monetizationManager: MonetizationManager
) {

    suspend fun unlockTheme(theme: ThemeType) {
        userPreferencesRepository.unlockTheme(theme)
    }

    /**
     * Sets the current theme if it's unlocked.
     * @return true if theme was set successfully, false if theme is locked
     */
    suspend fun setCurrentTheme(theme: ThemeType): Boolean {
        val prefs = userPreferencesRepository.userPreferences.first()
        // Security: Only allow setting theme if it's unlocked
        return if (prefs.unlockedThemes.contains(theme)) {
            userPreferencesRepository.setCurrentTheme(theme)
            true
        } else {
            if (BuildConfig.DEBUG) android.util.Log.w("ThemeManager", "Attempted to set locked theme: $theme")
            false
        }
    }

    fun isThemeUnlocked(theme: ThemeType, userPreferences: UserPreferences): Boolean {
        return userPreferences.unlockedThemes.contains(theme)
    }

    suspend fun unlockThemeWithRewardedAd(theme: ThemeType): Boolean {
        val success = monetizationManager.showRewardedAdForTheme(theme)
        if (success) {
            unlockTheme(theme)
        }
        return success
    }

    /**
     * Checks if any theme unlock milestones have been reached and unlocks them.
     * @return List of newly unlocked themes (empty if none were unlocked)
     */
    suspend fun checkThemeUnlockMilestones(): List<ThemeType> {
        val newlyUnlocked = mutableListOf<ThemeType>()
        try {
            val prefs = userPreferencesRepository.userPreferences.first()

            for (theme in ThemeType.entries) {
                if (theme == ThemeType.DEFAULT) continue
                if (!prefs.unlockedThemes.contains(theme) && theme.isUnlockConditionMet(prefs)) {
                    userPreferencesRepository.unlockTheme(theme)
                    newlyUnlocked.add(theme)
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.w("ThemeManager", "Failed to check theme unlock milestones", e)
        }
        return newlyUnlocked
    }
}
