package com.SzpontCompany.check.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.SzpontCompany.check.MainActivity
import com.SzpontCompany.check.R

val widgetStepsKey = intPreferencesKey("widget_steps")
val widgetGoalKey = intPreferencesKey("widget_goal")
val widgetHabitDoneKey = booleanPreferencesKey("widget_habit_done_today")

class StepsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val prefs = currentState<Preferences>()
            val savedColorInt = prefs[widgetAccentColorKey] ?: Color(0xFF857AE6).toArgb()
            val accentColor = Color(savedColorInt)

            val steps = prefs[widgetStepsKey] ?: 0
            val goal = prefs[widgetGoalKey] ?: 8000

            val progress = if (goal > 0) {
                (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            StepsWidgetContent(steps, goal, progress, accentColor)
        }
    }

    @Composable
    private fun StepsWidgetContent(steps: Int, goal: Int, progress: Float, accentColor: Color) {
        val progressPercent = (progress * 100).toInt()
        val context = LocalContext.current

        val containerColor = ColorProvider(day = Color(0xFF1C1C1E), night = Color(0xFF1C1C1E))
        val accentColorProvider = ColorProvider(day = accentColor, night = accentColor)
        val grayColorProvider = ColorProvider(day = Color.Gray, night = Color.Gray)
        val whiteColorProvider = ColorProvider(day = Color.White, night = Color.White)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(containerColor)
                .cornerRadius(24.dp)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString((R.string.widget_steps_steps)),
                    style = TextStyle(
                        color = grayColorProvider,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                Text(
                    text = String.format("%,d", steps).replace(',', ' '),
                    style = TextStyle(
                        color = whiteColorProvider,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "$progressPercent%",
                    style = TextStyle(
                        color = accentColorProvider,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                ProgressBar(progress, accentColorProvider)

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = context.getString((R.string.widget_steps_goal), goal),
                    style = TextStyle(
                        color = grayColorProvider,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun ProgressBar(progress: Float, accentColor: ColorProvider) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(6.dp)
                .cornerRadius(3.dp)
        ) {
            val segments = 20
            val filledSegments = (progress * segments).toInt()
            val emptyColor = ColorProvider(day = Color(0xFF3A3A3C), night = Color(0xFF3A3A3C))

            for (i in 0 until segments) {
                val segmentColor = if (i < filledSegments) accentColor else emptyColor
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .background(segmentColor)
                ) {}
            }
        }
    }
}