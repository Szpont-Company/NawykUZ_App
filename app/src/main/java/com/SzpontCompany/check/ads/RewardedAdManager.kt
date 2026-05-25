package com.SzpontCompany.check.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * RewardedAdManager - manager do pokazywania nagrodowych reklam video.
 *
 * Singleton zarządzający cyklem życia reklam video z nagrodami.
 * Umożliwia użytkownikowi zarobienie monetek/XP poprzez obejrzenie reklamy.
 *
 * Funkcje:
 * - Ładowanie reklam video nagrodzonych
 * - Wyświetlanie reklam i pobieranie nagród
 * - Automatyczne przeładowanie reklam po pokazaniu
 *
 * Używa Google AdMob z testowym ID jednostki reklamowej.
 *
 * @since 1.0
 * @author Szpont Company
 */
object RewardedAdManager {
    /** Instancja załadowanej reklamy */
    private var rewardedAd: RewardedAd? = null

    /**
     * Ładuje nagrodzony spot reklamowy z AdMob.
     *
     * Musi być wywoływane przed pierwszym wywołaniem [show].
     * Reklamy mogą być ładowane w tle nawet gdy użytkownik nie ma zamiaru jej ogląda.
     *
     * @param context Kontekst aplikacji do ładowania reklamy
     */
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

    /**
     * Wyświetla nagrodzony spot reklamowy użytkownikowi.
     *
     * Po obejrzeniu reklamy callback [onReward] otrzyma ilość przyznanych punktów.
     * Po wyświetleniu reklama jest automatycznie zwalniania i następnie ładowana nowa.
     *
     * @param activity Aktywność do wyświetlenia reklamy
     * @param onReward Callback z ilością nagrodę (np. monety lub XP)
     */
    fun show(activity: Activity, onReward: (Int) -> Unit) {

        rewardedAd?.show(activity) { rewardItem ->
            onReward(rewardItem.amount)
        }

        rewardedAd = null
        load(activity.applicationContext)
    }
}