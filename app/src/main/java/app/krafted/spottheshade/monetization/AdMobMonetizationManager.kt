package app.krafted.spottheshade.monetization

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import app.krafted.spottheshade.data.model.ThemeType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AdMobMonetizationManager(private val context: Context) : MonetizationManager {

    // Test Ad Unit ID for safe development.
    // TODO: Swap this to your LIVE ID ("ca-app-pub-5747334947769080/2909012864") right before publishing to the Play Store!
    private val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    override suspend fun showRewardedAdForExtraTime(activity: Activity): Boolean {
        return loadAndShowRewardedAd(activity)
    }

    override suspend fun showRewardedAdForTheme(theme: ThemeType, activity: Activity): Boolean {
        return loadAndShowRewardedAd(activity)
    }

    override suspend fun showRewardedAdForSkipLevel(activity: Activity): Boolean {
        return loadAndShowRewardedAd(activity)
    }

    override fun isAdAvailable(): Boolean {
        return true
    }

    private suspend fun loadAndShowRewardedAd(activity: Activity): Boolean = suspendCancellableCoroutine { continuation ->
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                var userEarnedReward = false

                rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        if (continuation.isActive) {
                            continuation.resume(userEarnedReward)
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                }

                rewardedAd.show(activity) { _ ->
                    userEarnedReward = true
                }
            }
        })
    }
}
