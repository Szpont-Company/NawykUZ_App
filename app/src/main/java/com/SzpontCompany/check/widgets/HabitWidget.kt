package com.SzpontCompany.check.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.glance.action.clickable
import com.SzpontCompany.check.MainActivity
import com.SzpontCompany.check.R


val widgetAccentColorKey = intPreferencesKey("widget_accent_color")
val widgetStreakKey = intPreferencesKey("widget_habit_streak")
val widgetRecordKey = intPreferencesKey("widget_habit_record")

class HabitWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()

            val savedColorInt = prefs[widgetAccentColorKey] ?: Color(0xFF857AE6).toArgb()
            val accentColor = Color(savedColorInt)

            val streak = prefs[widgetStreakKey] ?: 0
            val record = prefs[widgetRecordKey] ?: 0

            MyContent(streak, record, accentColor)
        }
    }

    @Composable
    private fun MyContent(streak: Int, record: Int, accentColor: Color) {
        val context = LocalContext.current

        val cardBackgroundColor = ColorProvider(day = accentColor, night = accentColor)
        val primaryTextColor = ColorProvider(day = Color.White, night = Color.White)
        val secondaryTextColor = ColorProvider(day = Color.White.copy(alpha = 0.8f), night = Color.White.copy(alpha = 0.8f))

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(cardBackgroundColor)
                .cornerRadius(24.dp)
                .padding(20.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = context.getString(R.string.habit_widget_streak),
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = context.getString(R.string.habit_widget_days, streak),
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "${context.getString(R.string.habit_widget_personal_best)} $record ${context.getString(R.string.habit_widget_days)}",
                    style = TextStyle(
                        color = secondaryTextColor,
                        fontSize = 12.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                val barHeights = listOf(14.dp, 20.dp, 26.dp, 16.dp, 36.dp, 28.dp, 10.dp)

                barHeights.forEach { height ->
                    Spacer(
                        modifier = GlanceModifier
                            .width(5.dp)
                            .height(height)
                            .background(ColorProvider(Color.White, Color.Black))
                            .cornerRadius(3.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                }
            }
        }
    }
}

fun updateHabitWidgetData(context: Context, newStreak: Int, newRecord: Int) {
    CoroutineScope(Dispatchers.IO).launch {
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(HabitWidget::class.java)

        ids.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[widgetStreakKey] = newStreak
                prefs[widgetRecordKey] = newRecord
            }
            HabitWidget().update(context, glanceId)
        }
    }
}

fun updateWidgetAccentColor(context: Context, newColor: Color) {
    CoroutineScope(Dispatchers.IO).launch {
        val manager = GlanceAppWidgetManager(context)
        val ids1 = manager.getGlanceIds(HabitWidget::class.java)
        ids1.forEach { /* aktualizacja */ }

        val ids2 = manager.getGlanceIds(StepsWidget::class.java)
        ids2.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[widgetAccentColorKey] = newColor.toArgb()
            }
            StepsWidget().update(context, glanceId)
        }
    }
}