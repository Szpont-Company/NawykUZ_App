package com.SzpontCompany.check.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null

    fun load(context: Context) {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917", // Test ad unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d("ADS", "Rewarded loaded")
                }

                override fun onAdFailedToLoad(errorCode: LoadAdError) {
                    rewardedAd = null
                    Log.e("ADS", "Failed to load rewarded ad: ${errorCode.message}")
                }
            }
        )
    }

    fun show(activity: Activity, onReward: (Int) -> Unit) {

        rewardedAd?.show(activity) { rewardItem ->
            onReward(rewardItem.amount)
        }

        rewardedAd = null
        load(activity.applicationContext)
    }
}