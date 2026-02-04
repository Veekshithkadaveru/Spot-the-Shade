package app.krafted.spottheshade.game

import app.krafted.spottheshade.data.model.ThemeType
import app.krafted.spottheshade.data.model.UserPreferences
import app.krafted.spottheshade.data.repository.PreferencesManager
import app.krafted.spottheshade.monetization.MonetizationManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ThemeManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val monetizationManager: MonetizationManager
) {

    suspend fun unlockTheme(theme: ThemeType) {
        preferencesManager.unlockTheme(theme)
    }

    /**
     * Sets the current theme if it's unlocked.
     * @return true if theme was set successfully, false if theme is locked
     */
    suspend fun setCurrentTheme(theme: ThemeType): Boolean {
        val prefs = preferencesManager.userPreferences.first()
        // Security: Only allow setting theme if it's unlocked
        return if (prefs.unlockedThemes.contains(theme)) {
            preferencesManager.setCurrentTheme(theme)
            true
        } else {
            android.util.Log.w("ThemeManager", "Attempted to set locked theme: $theme")
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
            val prefs = preferencesManager.userPreferences.first()

            val unlockChecks = listOf(
                Triple(10, ThemeType.FOREST, prefs.highestLevel >= 10),
                Triple(20, ThemeType.OCEAN, prefs.highestLevel >= 20),
                Triple(30, ThemeType.SUNSET, prefs.highestLevel >= 30),
                Triple(40, ThemeType.WINTER, prefs.highestLevel >= 40),
                Triple(50, ThemeType.SPRING, prefs.highestLevel >= 50),
                Triple(1000, ThemeType.NEON_CYBER, prefs.highScore >= 1000),
                Triple(2000, ThemeType.VOLCANIC, prefs.highScore >= 2000),
                Triple(0, ThemeType.ROYAL_GOLD, true)
            )

            for ((_, theme, condition) in unlockChecks) {
                if (condition && !prefs.unlockedThemes.contains(theme)) {
                    preferencesManager.unlockTheme(theme)
                    newlyUnlocked.add(theme)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ThemeManager", "Failed to check theme unlock milestones", e)
        }
        return newlyUnlocked
    }
}
