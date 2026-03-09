package app.krafted.spottheshade.monetization

import app.krafted.spottheshade.data.model.ThemeType

interface MonetizationManager {
    suspend fun showRewardedAdForExtraTime(): Boolean
    suspend fun showRewardedAdForTheme(theme: ThemeType): Boolean
    fun isAdAvailable(): Boolean
}

// Intentional v1 implementation: all features are free with no ads.
// Replace with a real AdMob-backed implementation when monetization is needed.
class MockMonetizationManager : MonetizationManager {
    override suspend fun showRewardedAdForExtraTime(): Boolean {
        return true
    }

    override suspend fun showRewardedAdForTheme(theme: ThemeType): Boolean {
        return true
    }

    override fun isAdAvailable(): Boolean {
        return true
    }
}
