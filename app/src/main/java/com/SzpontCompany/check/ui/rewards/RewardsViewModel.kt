package com.SzpontCompany.check.ui.rewards

import android.util.Log
import androidx.lifecycle.ViewModel

class RewardsViewModel : ViewModel() {
    fun onAdWatched(rewardAmount: Int) {
        // Handle the reward logic here, e.g., update user points or unlock features
        // This is a placeholder for your actual reward handling code
        Log.d("ADS", "User earned reward: $rewardAmount points")
    }
}