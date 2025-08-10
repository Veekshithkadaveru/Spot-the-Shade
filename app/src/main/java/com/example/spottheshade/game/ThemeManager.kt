package com.example.spottheshade.game

import com.example.spottheshade.data.model.ThemeType
import com.example.spottheshade.data.model.UserPreferences
import com.example.spottheshade.data.repository.PreferencesManager
import com.example.spottheshade.monetization.MonetizationManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ThemeManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val monetizationManager: MonetizationManager
) {
    
    suspend fun unlockTheme(theme: ThemeType) {
        preferencesManager.unlockTheme(theme)
    }
    
    suspend fun setCurrentTheme(theme: ThemeType) {
        preferencesManager.setCurrentTheme(theme)
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
    
    suspend fun checkThemeUnlockMilestones() {
        try {
            val prefs = preferencesManager.userPreferences.first()
            
            val unlockChecks = listOf(
                Triple(10, ThemeType.FOREST, prefs.highestLevel >= 10),
                Triple(20, ThemeType.OCEAN, prefs.highestLevel >= 20),
                Triple(30, ThemeType.SUNSET, prefs.highestLevel >= 30),
                Triple(40, ThemeType.WINTER, prefs.highestLevel >= 40),
                Triple(50, ThemeType.SPRING, prefs.highestLevel >= 50),
                Triple(1000, ThemeType.NEON_CYBER, prefs.highScore >= 1000),
                Triple(2000, ThemeType.VOLCANIC, prefs.highScore >= 2000)
            )
            
            for ((_, theme, condition) in unlockChecks) {
                if (condition && !prefs.unlockedThemes.contains(theme)) {
                    preferencesManager.unlockTheme(theme)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ThemeManager", "Failed to check theme unlock milestones", e)
        }
    }
}