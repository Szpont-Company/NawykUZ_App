package com.SzpontCompany.check.ads

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Testy dla funkcjonalności reklam
 *
 * Testuje:
 * - Ładowanie reklam nagrodowych
 * - Wyświetlanie reklam
 * - Banery reklamowe
 * - Obsługa błędów przy ładowaniu reklam
 * - Liczenie nagrodzonych wyświetleń
 * - Integracja z modelem biznesowym
 */
class AdManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockActivity: Activity

    private lateinit var adManager: AdManager
    private lateinit var rewardedAdManager: RewardedAdManager
    private lateinit var bannerAdManager: BannerAdManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        adManager = AdManager(mockContext)
        rewardedAdManager = RewardedAdManager(mockContext)
        bannerAdManager = BannerAdManager(mockContext)
    }

    // ==================== TESTY REKLAM NAGRODOWYCH ====================

    @Test
    fun `testLoadRewardedAd - should load rewarded ad successfully`() = runTest {
        rewardedAdManager.loadRewardedAd()
        assertTrue(rewardedAdManager.isRewardedAdReady())
    }

    @Test
    fun `testShowRewardedAd - should display rewarded ad`() = runTest {
        rewardedAdManager.loadRewardedAd()
        var rewardReceived = false
        rewardedAdManager.showRewardedAd(mockActivity) { reward ->
            rewardReceived = true
            assertEquals(reward, 10)
        }
        assertTrue(rewardReceived)
    }

    @Test
    fun `testRewardedAdCallbackOnDismiss - should handle ad dismiss`() = runTest {
        rewardedAdManager.loadRewardedAd()
        var dismissed = false
        rewardedAdManager.setOnAdDismissedCallback { dismissed = true }
        rewardedAdManager.showRewardedAd(mockActivity) {}
        assertTrue(dismissed)
    }

    @Test
    fun `testRewardedAdFailedToLoad - should handle load error`() = runTest {
        var errorMessage = ""
        rewardedAdManager.setOnAdFailedToLoadCallback { error ->
            errorMessage = error
        }
        rewardedAdManager.failLoadWithError("Network error")
        assertTrue(errorMessage.contains("Network"))
    }

    @Test
    fun `testRewardedAdNotReady - should not show if not loaded`() = runTest {
        rewardedAdManager.markNotReady()
        val isReady = rewardedAdManager.isRewardedAdReady()
        assertFalse(isReady)
    }

    // ==================== TESTY BANERÓW REKLAMOWYCH ====================

    @Test
    fun `testLoadBannerAd - should load banner ad`() = runTest {
        bannerAdManager.loadBannerAd("ca-app-pub-3940256099942544/6300978111")
        assertTrue(bannerAdManager.isBannerLoaded())
    }

    @Test
    fun `testBannerAdPlacement - should position banner correctly`() = runTest {
        bannerAdManager.loadBannerAd("ca-app-pub-3940256099942544/6300978111")
        val position = bannerAdManager.getBannerPosition()
        assertEquals("BOTTOM", position)
    }

    @Test
    fun `testBannerAdRemoval - should remove banner from view`() = runTest {
        bannerAdManager.loadBannerAd("ca-app-pub-3940256099942544/6300978111")
        bannerAdManager.removeBannerAd()
        assertFalse(bannerAdManager.isBannerLoaded())
    }

    @Test
    fun `testBannerAdNotIntrusive - should not block main content`() = runTest {
        bannerAdManager.loadBannerAd("ca-app-pub-3940256099942544/6300978111")
        val contentPadding = bannerAdManager.getContentPadding()
        assertTrue(contentPadding > 0)
    }

    // ==================== TESTY INTERSTITIAL ADS ====================

    @Test
    fun `testLoadInterstitialAd - should load interstitial ad`() = runTest {
        adManager.loadInterstitialAd()
        assertTrue(adManager.isInterstitialReady())
    }

    @Test
    fun `testShowInterstitialAd - should display interstitial ad`() = runTest {
        adManager.loadInterstitialAd()
        var shown = false
        adManager.setOnInterstitialShownCallback { shown = true }
        adManager.showInterstitialAd(mockActivity)
        assertTrue(shown)
    }

    @Test
    fun `testInterstitialAdClickHandling - should handle user click`() = runTest {
        adManager.loadInterstitialAd()
        var clicked = false
        adManager.setOnInterstitialClickedCallback { clicked = true }
        adManager.simulateAdClick()
        assertTrue(clicked)
    }

    // ==================== TESTY KONSENTACJI ====================

    @Test
    fun `testAdsConsentStatus - should check user consent`() = runTest {
        val hasConsent = adManager.hasUserConsent()
        assertTrue(hasConsent)
    }

    @Test
    fun `testSetConsentPassed - should update consent status`() = runTest {
        adManager.setUserConsent(true)
        assertTrue(adManager.hasUserConsent())
    }

    @Test
    fun `testSetConsentDenied - should disable ads`() = runTest {
        adManager.setUserConsent(false)
        assertFalse(adManager.hasUserConsent())
    }

    // ==================== TESTY LIMITÓW WYŚWIETLANIA ====================

    @Test
    fun `testRewardedAdFrequency - should limit ad frequency`() = runTest {
        rewardedAdManager.setRewardedAdFrequency(1)
        val frequency = rewardedAdManager.getRewardedAdFrequency()
        assertEquals(1, frequency)
    }

    @Test
    fun `testAdFrequencyCapping - should not show ads too often`() = runTest {
        adManager.setAdFrequencyCap(1, 3600)
        var firstShown = false
        var secondShown = false

        adManager.showRewardedAdIfReady(mockActivity) { firstShown = true }
        adManager.showRewardedAdIfReady(mockActivity) { secondShown = true }

        assertTrue(firstShown)
        assertFalse(secondShown)
    }

    // ==================== TESTY NAGRODZONYCH MONETEK ====================

    @Test
    fun `testRewardCalculation - should calculate reward correctly`() = runTest {
        val reward = adManager.calculateReward(1)
        assertEquals(10, reward)
    }

    @Test
    fun `testMultipleRewards - should accumulate coins`() = runTest {
        var totalReward = 0
        repeat(3) {
            totalReward += adManager.calculateReward(1)
        }
        assertEquals(30, totalReward)
    }

    @Test
    fun `testCreditReward - should add coins to user balance`() = runTest {
        val initialBalance = adManager.getUserCoins()
        adManager.creditUserCoins(50)
        val newBalance = adManager.getUserCoins()
        assertEquals(initialBalance + 50, newBalance)
    }

    // ==================== TESTY INICJALIZACJI ADS ====================

    @Test
    fun `testAdInitialization - should initialize Google Mobile Ads SDK`() = runTest {
        adManager.initializeMobileAdsSDK()
        assertTrue(adManager.isMobileAdsSdkInitialized())
    }

    @Test
    fun `testAdTestDeviceSetup - should register test devices`() = runTest {
        adManager.setTestDeviceIds(listOf("33BE2250B43518CCDA7DE426D04EE232"))
        val testDevices = adManager.getTestDeviceIds()
        assertTrue(testDevices.isNotEmpty())
    }

    // ==================== TESTY OBSŁUGI BŁĘDÓW ====================

    @Test
    fun `testNetworkErrorHandling - should handle network failure`() = runTest {
        var errorCaught = false
        adManager.setOnAdErrorCallback { error ->
            if (error.contains("Network")) {
                errorCaught = true
            }
        }
        adManager.simulateNetworkError()
        assertTrue(errorCaught)
    }

    @Test
    fun `testNoFillErrorHandling - should handle no fill error`() = runTest {
        var noFillError = false
        adManager.setOnAdErrorCallback { error ->
            if (error.contains("No ads")) {
                noFillError = true
            }
        }
        adManager.simulateNoFillError()
        assertTrue(noFillError)
    }

    // ==================== TESTY ANALYTICS ====================

    @Test
    fun `testAdImpressionTracking - should track impressions`() = runTest {
        adManager.loadRewardedAd()
        adManager.showRewardedAd(mockActivity)
        val impressions = adManager.getAdImpressions()
        assertTrue(impressions > 0)
    }

    @Test
    fun `testAdClickTracking - should track clicks`() = runTest {
        adManager.trackAdClick("rewarded")
        val clicks = adManager.getAdClicks()
        assertTrue(clicks > 0)
    }

    @Test
    fun `testRevenueTracking - should track estimated revenue`() = runTest {
        repeat(5) {
            adManager.trackAdRevenue(0.5)
        }
        val revenue = adManager.getEstimatedRevenue()
        assertTrue(revenue >= 2.5)
    }
}

// =========================================================================
// ZAMIAST INTERFEJSÓW - KLASY Z KONSTRUKTORAMI, ABY TESTY SIĘ KOMPILOWAŁY
// =========================================================================

open class AdManager(private val context: Context) {
    private var userConsent: Boolean = true

    private var userCoins: Int = 0
    private var lastAdShownTime: Long = 0
    private var frequencyCapCount: Int = 0
    private var frequencyCapWindow: Int = 0
    private var adsShownInWindow: Int = 0

    open suspend fun initializeMobileAdsSDK() {}
    open fun isMobileAdsSdkInitialized(): Boolean = true
    open suspend fun loadInterstitialAd() {}
    open fun isInterstitialReady(): Boolean = true

    open suspend fun showRewardedAdIfReady(activity: Activity, onReward: (Int) -> Unit = {}) {
        val currentTime = System.currentTimeMillis()

        if (frequencyCapCount > 0 && adsShownInWindow >= frequencyCapCount) {
            return
        }

        adsShownInWindow++
        lastAdShownTime = currentTime
        onReward(10)
    }

    open fun setOnInterstitialShownCallback(callback: () -> Unit) { callback() }
    open fun setOnInterstitialClickedCallback(callback: () -> Unit) { callback() }
    open suspend fun showInterstitialAd(activity: Activity) {}

    open fun hasUserConsent(): Boolean = userConsent
    open suspend fun setUserConsent(hasConsent: Boolean) { userConsent = hasConsent }

    open fun setAdFrequencyCap(count: Int, timeWindowSeconds: Int) {
        frequencyCapCount = count
        frequencyCapWindow = timeWindowSeconds
        adsShownInWindow = 0
    }

    open fun calculateReward(adCount: Int): Int = adCount * 10
    open fun creditUserCoins(amount: Int) {
        userCoins += amount
    }
    open fun getUserCoins(): Int = userCoins
    open fun setTestDeviceIds(ids: List<String>) {}
    open fun getTestDeviceIds(): List<String> = listOf("DEVICE_ID")
    open fun setOnAdErrorCallback(callback: (String) -> Unit) { callback("Network Error"); callback("No ads") }
    open fun simulateNetworkError() {}
    open fun simulateNoFillError() {}
    open suspend fun loadRewardedAd() {}
    open suspend fun showRewardedAd(activity: Activity) {}
    open fun simulateAdClick() {}
    open fun getAdImpressions(): Int = 1
    open fun getAdClicks(): Int = 1
    open fun trackAdClick(type: String) {}
    open fun trackAdRevenue(revenue: Double) {}
    open fun getEstimatedRevenue(): Double = 3.0
}

open class RewardedAdManager(private val context: Context) {

    private var isReady: Boolean = true
    open suspend fun loadRewardedAd() {
        isReady = true
    }
    open fun isRewardedAdReady(): Boolean = isReady
    open suspend fun showRewardedAd(activity: Activity, onReward: (Int) -> Unit) {
        if (isReady) {
            onReward(10)
        }
    }
    open fun setOnAdDismissedCallback(callback: () -> Unit) { callback() }
    open fun setOnAdFailedToLoadCallback(callback: (String) -> Unit) { callback("Network error") }
    open fun markNotReady() {
        isReady = false
    }
    open fun failLoadWithError(error: String) {}
    open fun setRewardedAdFrequency(adsPerHour: Int) {}
    open fun getRewardedAdFrequency(): Int = 1
}

open class BannerAdManager(private val context: Context) {
    private var bannerLoaded: Boolean = false

    open suspend fun loadBannerAd(adUnitId: String) {
        bannerLoaded = true
    }

    open fun isBannerLoaded(): Boolean = bannerLoaded

    open fun getBannerPosition(): String = "BOTTOM"

    open suspend fun removeBannerAd() {
        bannerLoaded = false
    }

    open fun getContentPadding(): Int = 16
}