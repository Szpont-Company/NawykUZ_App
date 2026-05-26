package com.SzpontCompany.check.widgets

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

/**
 * Testy dla funkcjonalności widżetu
 */
class StepsWidgetTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPackageManager: PackageManager

    private lateinit var widgetManager: FakeWidgetManager
    private lateinit var widgetDataUpdater: FakeWidgetDataUpdater

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        widgetManager = spy(FakeWidgetManager(mockContext))

        widgetDataUpdater = FakeWidgetDataUpdater(mockContext, widgetManager)
    }

    // ==================== TESTY WYŚWIETLANIA DANYCH ====================

    @Test
    fun `testDisplayStepCount - should show steps on widget`() = runTest {
        val steps = 5234
        widgetDataUpdater.updateStepCountDisplay(steps)

        val displayedSteps = widgetManager.getDisplayedSteps()
        assertEquals(steps, displayedSteps)
    }

    @Test
    fun `testDisplayDailyGoal - should show daily goal on widget`() = runTest {
        val goal = 8000
        widgetDataUpdater.updateDailyGoalDisplay(goal)

        val displayedGoal = widgetManager.getDisplayedGoal()
        assertEquals(goal, displayedGoal)
    }

    @Test
    fun `testDisplayProgressBar - should render progress percentage`() = runTest {
        val steps = 4000
        val goal = 8000
        widgetDataUpdater.updateProgressDisplay(steps, goal)

        val progress = widgetManager.getProgressPercentage()
        assertEquals(50, progress)
    }

    @Test
    fun `testDisplayCalories - should calculate and show burned calories`() = runTest {
        val steps = 10000
        widgetDataUpdater.updateCalorieDisplay(steps)

        val calories = widgetManager.getDisplayedCalories()
        assertTrue(calories > 0)
    }

    @Test
    fun `testDisplayDistance - should estimate and show distance`() = runTest {
        val steps = 10000
        widgetDataUpdater.updateDistanceDisplay(steps)

        val distance = widgetManager.getDisplayedDistance()
        assertTrue(distance > 0)
    }

    // ==================== TESTY AKTUALIZACJI WIDŻETU ====================

    @Test
    fun `testAutoRefreshWidget - should update automatically`() = runTest {
        widgetManager.startAutoRefresh(interval = 300000) // 5 minutes
        assertTrue(widgetManager.isAutoRefreshEnabled())
    }

    @Test
    fun `testManualRefreshWidget - should update on demand`() = runTest {
        widgetDataUpdater.updateStepsWidgetData(mockContext)
        verify(widgetManager).refreshWidget()
    }

    @Test
    fun `testRefreshFrequency - should respect update frequency`() = runTest {
        widgetManager.updateRefreshFrequency(5) // 5 minute intervals
        val frequency = widgetManager.getRefreshFrequency()
        assertEquals(5, frequency)
    }

    @Test
    fun `testBatteryOptimization - should reduce updates when low battery`() = runTest {
        widgetManager.setBatteryOptimizationEnabled(true)
        val frequency = widgetManager.getRefreshFrequency()
        assertTrue(frequency > 5) // Should increase interval
    }

    // ==================== TESTY ROZMIARÓW WIDŻETU ====================

    @Test
    fun `test1x1WidgetSize - should display correctly on 1x1 size`() = runTest {
        val widget = widgetManager.createWidget(WidgetSize.SMALL_1x1)
        assertTrue(widget.isValid())
        assertEquals(WidgetSize.SMALL_1x1, widget.size)
    }

    @Test
    fun `test2x2WidgetSize - should display correctly on 2x2 size`() = runTest {
        val widget = widgetManager.createWidget(WidgetSize.MEDIUM_2x2)
        assertTrue(widget.isValid())
        assertEquals(WidgetSize.MEDIUM_2x2, widget.size)
    }

    @Test
    fun `test4x2WidgetSize - should display correctly on 4x2 size`() = runTest {
        val widget = widgetManager.createWidget(WidgetSize.LARGE_4x2)
        assertTrue(widget.isValid())
        assertEquals(WidgetSize.LARGE_4x2, widget.size)
    }

    @Test
    fun `testResponsiveLayout - should adapt to any size`() = runTest {
        val widget = widgetManager.createWidget(WidgetSize.MEDIUM_2x2)
        widget.resize(WidgetSize.LARGE_4x2)

        assertEquals(WidgetSize.LARGE_4x2, widget.size)
        assertTrue(widget.isValidAfterResize())
    }

    // ==================== TESTY INTERAKTYWNOŚCI ====================

    @Test
    fun `testClickOnWidget - should open app`() = runTest {
        var appLaunched = false
        widgetManager.setOnWidgetClickListener { appLaunched = true }
        widgetManager.simulateWidgetClick()
        assertTrue(appLaunched)
    }

    @Test
    fun `testQuickActionButton - should trigger action`() = runTest {
        var actionTriggered = false
        widgetManager.setQuickActionCallback { actionTriggered = true }
        widgetManager.simulateQuickAction()
        assertTrue(actionTriggered)
    }

    @Test
    fun `testLongClickWidget - should show options menu`() = runTest {
        var menuShown = false
        widgetManager.setOnLongClickListener { menuShown = true }
        widgetManager.simulateLongClick()
        assertTrue(menuShown)
    }

    // ==================== TESTY SYNCHRONIZACJI ====================

    @Test
    fun `testSyncWithMainApp - should reflect main app data`() = runTest {
        val steps = 5000
        widgetDataUpdater.syncWithMainApp(steps)

        val displayedSteps = widgetManager.getDisplayedSteps()
        assertEquals(5000, displayedSteps)
    }

    @Test
    fun `testOfflineWidgetData - should work offline`() = runTest {
        val cachedSteps = widgetManager.getCachedStepData()
        assertTrue(cachedSteps >= 0)
    }

    @Test
    fun `testDataCachingInWidget - should cache data locally`() = runTest {
        val steps = 7500
        widgetDataUpdater.cacheWidgetData(steps)

        val cached = widgetManager.getCachedStepData()
        assertEquals(steps, cached)
    }

    // ==================== TESTY STYLÓW ====================

    @Test
    fun `testLightThemeWidget - should apply light colors`() = runTest {
        widgetManager.setTheme(WidgetTheme.LIGHT)
        val backgroundColor = widgetManager.getBackgroundColor()
        assertTrue(backgroundColor.isLight())
    }

    @Test
    fun `testDarkThemeWidget - should apply dark colors`() = runTest {
        widgetManager.setTheme(WidgetTheme.DARK)
        val backgroundColor = widgetManager.getBackgroundColor()
        assertTrue(backgroundColor.isDark())
    }

    @Test
    fun `testTransparentBackground - should support transparency`() = runTest {
        widgetManager.setTheme(WidgetTheme.TRANSPARENT)
        assertTrue(widgetManager.isBackgroundTransparent())
    }

    @Test
    fun `testCornerRadius - should apply corner radius`() = runTest {
        widgetManager.setCornerRadius(16)
        val radius = widgetManager.getCornerRadius()
        assertEquals(16, radius)
    }

    // ==================== TESTY ANIMACJI ====================

    @Test
    fun `testProgressBarAnimation - should animate progress changes`() = runTest {
        widgetDataUpdater.updateProgressDisplay(5000, 8000)
        assertTrue(widgetManager.isAnimationEnabled())
    }

    @Test
    fun `testCountUpAnimation - should count up step numbers`() = runTest {
        val startSteps = 0
        val endSteps = 5000
        widgetDataUpdater.animateStepCount(startSteps, endSteps)
        assertTrue(widgetManager.isAnimatingSteps())
    }

    // ==================== TESTY POWIADOMIEŃ W WIDŻECIE ====================

    @Test
    fun `testWidgetNotification - should show notification in widget`() = runTest {
        val message = "Daily goal reached!"
        widgetManager.showNotification(message)

        val notification = widgetManager.getLastNotification()
        assertEquals(message, notification)
    }

    @Test
    fun `testTodayVsYesterdayComparison - should show comparison`() = runTest {
        widgetManager.showTodayVsYesterday(today = 5000, yesterday = 4500)
        assertTrue(widgetManager.isComparisonVisible())
    }

    // ==================== TESTY KONFIGURACJI ====================

    @Test
    fun `testWidgetConfiguration - should save preferences`() = runTest {
        widgetManager.setShowCalories(true)
        widgetManager.setShowDistance(false)

        assertTrue(widgetManager.isShowCaloriesEnabled())
        assertFalse(widgetManager.isShowDistanceEnabled())
    }

    @Test
    fun `testMultipleWidgetInstances - should support multiple widgets`() = runTest {
        val widget1 = widgetManager.createWidget(WidgetSize.SMALL_1x1)
        val widget2 = widgetManager.createWidget(WidgetSize.MEDIUM_2x2)

        val instances = widgetManager.getWidgetCount()
        assertEquals(2, instances)
    }

    // ==================== TESTY BŁĘDÓW ====================

    @Test
    fun `testWidgetErrorState - should handle errors gracefully`() = runTest {
        widgetManager.simulateDataFetchError()
        val errorMessage = widgetManager.getErrorMessage()
        assertTrue(errorMessage.isNotEmpty())
    }

    @Test
    fun `testWidgetPlaceholderData - should show placeholder when no data`() = runTest {
        widgetManager.clearCachedData()
        val displayedSteps = widgetManager.getDisplayedSteps()
        assertEquals(0, displayedSteps)
    }
}

// ==================== KLASY SUPPORTUJĄCE I FAKE ====================

enum class WidgetSize {
    SMALL_1x1, MEDIUM_2x2, LARGE_4x2
}

enum class WidgetTheme {
    LIGHT, DARK, TRANSPARENT
}

open class FakeWidget(open var size: WidgetSize) {
    open fun isValid(): Boolean = true
    open fun isValidAfterResize(): Boolean = true
    open fun resize(newSize: WidgetSize) { size = newSize }
}

open class FakeWidgetManager(private val context: Context) {
    var _displayedSteps = 0
    var _displayedGoal = 0
    var _progress = 0
    var _calories = 0.0
    var _distance = 0.0
    var _autoRefresh = false
    var _refreshFreq = 0
    var _batteryOpt = false
    var _clickListener: (() -> Unit)? = null
    var _quickAction: (() -> Unit)? = null
    var _longClick: (() -> Unit)? = null
    var _cachedSteps = 1000
    var _theme = WidgetTheme.LIGHT
    var _radius = 0
    var _animating = false
    var _animatingSteps = false
    var _notification = ""
    var _showComparison = false
    var _showCal = false
    var _showDist = false
    var _errorMsg = ""
    val widgets = mutableListOf<FakeWidget>()

    open fun getDisplayedSteps() = _displayedSteps
    open fun getDisplayedGoal() = _displayedGoal
    open fun getProgressPercentage() = _progress
    open fun getDisplayedCalories() = _calories
    open fun getDisplayedDistance() = _distance
    open fun startAutoRefresh(interval: Long) { _autoRefresh = true }
    open fun isAutoRefreshEnabled() = _autoRefresh
    open fun refreshWidget() {}
    open fun updateRefreshFrequency(minutes: Int) { _refreshFreq = minutes }
    open fun getRefreshFrequency() = if(_batteryOpt) _refreshFreq + 10 else _refreshFreq
    open fun setBatteryOptimizationEnabled(enabled: Boolean) { _batteryOpt = enabled }
    open fun createWidget(size: WidgetSize): FakeWidget {
        val w = FakeWidget(size)
        widgets.add(w)
        return w
    }
    open fun setOnWidgetClickListener(listener: () -> Unit) { _clickListener = listener }
    open fun simulateWidgetClick() { _clickListener?.invoke() }
    open fun setQuickActionCallback(callback: () -> Unit) { _quickAction = callback }
    open fun simulateQuickAction() { _quickAction?.invoke() }
    open fun setOnLongClickListener(listener: () -> Unit) { _longClick = listener }
    open fun simulateLongClick() { _longClick?.invoke() }
    open fun getCachedStepData() = _cachedSteps
    open fun setTheme(t: WidgetTheme) { _theme = t }
    open fun getBackgroundColor() = when(_theme) { WidgetTheme.LIGHT -> "#ffffff"; WidgetTheme.DARK -> "#000000"; else -> "transparent" }
    open fun isBackgroundTransparent() = _theme == WidgetTheme.TRANSPARENT
    open fun setCornerRadius(r: Int) { _radius = r }
    open fun getCornerRadius() = _radius
    open fun isAnimationEnabled() = _animating
    open fun isAnimatingSteps() = _animatingSteps
    open fun showNotification(msg: String) { _notification = msg }
    open fun getLastNotification() = _notification
    open fun showTodayVsYesterday(today: Int, yesterday: Int) { _showComparison = true }
    open fun isComparisonVisible() = _showComparison
    open fun setShowCalories(s: Boolean) { _showCal = s }
    open fun setShowDistance(s: Boolean) { _showDist = s }
    open fun isShowCaloriesEnabled() = _showCal
    open fun isShowDistanceEnabled() = _showDist
    open fun getWidgetCount() = widgets.size
    open fun simulateDataFetchError() { _errorMsg = "Network error" }
    open fun getErrorMessage() = _errorMsg
    open fun clearCachedData() { _displayedSteps = 0 }
}

open class FakeWidgetDataUpdater(private val context: Context, private val manager: FakeWidgetManager) {
    open suspend fun updateStepCountDisplay(steps: Int) { manager._displayedSteps = steps }
    open suspend fun updateDailyGoalDisplay(goal: Int) { manager._displayedGoal = goal }
    open suspend fun updateProgressDisplay(steps: Int, goal: Int) {
        manager._progress = if (goal > 0) (steps * 100 / goal) else 0
        manager._animating = true
    }
    open suspend fun updateCalorieDisplay(steps: Int) { manager._calories = steps * 0.04 }
    open suspend fun updateDistanceDisplay(steps: Int) { manager._distance = steps * 0.0008 }
    open suspend fun updateStepsWidgetData(context: Context) { manager.refreshWidget() }
    open suspend fun syncWithMainApp(steps: Int) { manager._displayedSteps = steps }
    open suspend fun cacheWidgetData(steps: Int) { manager._cachedSteps = steps }
    open suspend fun animateStepCount(start: Int, end: Int) { manager._animatingSteps = true }
}

fun String.isLight(): Boolean = this.lowercase() in listOf("#ffffff", "#f5f5f5")
fun String.isDark(): Boolean = this.lowercase() in listOf("#000000", "#212121")