package app.krafted.spottheshade.monetization

import android.app.Activity
import app.krafted.spottheshade.data.model.ThemeType

interface MonetizationManager {
    suspend fun showRewardedAdForExtraTime(activity: Activity): Boolean
    suspend fun showRewardedAdForTheme(theme: ThemeType, activity: Activity): Boolean
    suspend fun showRewardedAdForSkipLevel(activity: Activity): Boolean
    fun isAdAvailable(): Boolean
}

// Intentional v1 implementation: all features are free with no ads.
// Replace with a real AdMob-backed implementation when monetization is needed.
class MockMonetizationManager : MonetizationManager {
    override suspend fun showRewardedAdForExtraTime(activity: Activity): Boolean {
        return true
    }

    override suspend fun showRewardedAdForTheme(theme: ThemeType, activity: Activity): Boolean {
        return true
    }

    override suspend fun showRewardedAdForSkipLevel(activity: Activity): Boolean {
        return true
    }

    override fun isAdAvailable(): Boolean {
        return true
    }
}
