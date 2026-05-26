package com.SzpontCompany.check.ui.stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.SzpontCompany.check.R
import com.SzpontCompany.check.data.habit.Habit
import java.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { StatsTopSection() }

        item {
            HabitFilterChips(
                habits = uiState.habits,
                selectedHabitId = uiState.selectedHabitId,
                onSelect = { viewModel.setHabitFilter(it) }
            )
        }

        item {
            TimeRangeSelector(
                selectedIndex = uiState.selectedTimeRangeIndex,
                onSelect = { viewModel.setTimeRange(it) }
            )
        }

        item {
            MetricsGrid(
                totalCompleted = uiState.totalCompletedHabits,
                successRate = uiState.overallSuccessRate,
                bestStreak = uiState.user?.bestStreak ?: 0,
                coins = uiState.coins,
                timeRangeIndex = uiState.selectedTimeRangeIndex
            )
        }

        item {
            HabitDetailsSection(
                habits = uiState.filteredHabits,
                timeRangeIndex = uiState.selectedTimeRangeIndex
            )
        }

        item {
            WeeklyActivityChart(
                chartData = uiState.weeklyChartData,
                averagePercentage = uiState.weeklyAveragePercentage
            )
        }

        if (uiState.selectedHabitId == null) {
            item {
                HeatmapSection(habits = uiState.filteredHabits)
            }
        } else {
            item {
                StreakCalendarSection(habits = uiState.filteredHabits)
            }
        }
    }
}

@Composable
fun StatsTopSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.stats_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimeRangeSelector(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val options = listOf(
        stringResource(R.string.stats_range_7d),
        stringResource(R.string.stats_range_30d),
        stringResource(R.string.stats_range_3m),
        stringResource(R.string.stats_range_all)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MetricsGrid(
    totalCompleted: Int,
    successRate: Int,
    bestStreak: Int,
    coins: Int,
    timeRangeIndex: Int
) {
    val timeRangeText = when(timeRangeIndex) {
        0 -> stringResource(R.string.stats_last_7d)
        1 -> stringResource(R.string.stats_last_30d)
        2 -> stringResource(R.string.stats_last_90d)
        else -> stringResource(R.string.stats_total)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.stats_completed),
                value = totalCompleted.toString(),
                subtext = timeRangeText, subtextColor = MaterialTheme.colorScheme.primary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.stats_effectiveness),
                value = "$successRate%",
                subtext = timeRangeText, subtextColor = MaterialTheme.colorScheme.primary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.stats_best_streak),
                value = bestStreak.toString(),
                valueSuffix = stringResource(R.string.habit_widget_days_unit),
                subtext = stringResource(R.string.stats_current_record), subtextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.stats_coins_xp),
                value = coins.toString(),
                subtext = stringResource(R.string.stats_coins_desc), subtextColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier, title: String, value: String, valueSuffix: String? = null,
    subtext: String, subtextColor: Color
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            if (valueSuffix != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = valueSuffix, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtext, fontSize = 11.sp, color = subtextColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HabitFilterChips(
    habits: List<Habit>,
    selectedHabitId: String?,
    onSelect: (String?) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            text = stringResource(R.string.stats_filter_all),
            isSelected = selectedHabitId == null,
            onClick = { onSelect(null) }
        )
        habits.forEach { habit ->
            FilterChip(
                text = "${habit.icon.ifEmpty { "🎯" }} ${habit.name}",
                isSelected = selectedHabitId == habit.id,
                onClick = { onSelect(habit.id) }
            )
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(20.dp)
            )
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WeeklyActivityChart(
    chartData: List<Pair<Int, Float>>,
    averagePercentage: Int
) {
    val daysOfWeek = stringArrayResource(R.array.days_of_week_short)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.stats_weekly_activity), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = stringResource(R.string.stats_weekly_avg, averagePercentage), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            chartData.forEachIndexed { index, data ->
                val dayIndex = data.first
                val dayLabel = daysOfWeek.getOrElse(dayIndex) { "" }
                val value = data.second
                val isToday = index == chartData.lastIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(0.7f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(value)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = dayLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun HeatmapSection(habits: List<Habit>) {
    val today = remember { LocalDate.now() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.stats_habit_map),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                val legendColors = listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    legendColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "5+", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val daysOfWeek = stringArrayResource(R.array.days_of_week_short).toList()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEachIndexed { dayIndex, dayLabel ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayLabel,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp)
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (week in 0 until 10) {
                            val daysToSubtract = ((9 - week) * 7) + (today.dayOfWeek.value - 1) - dayIndex
                            val cellDate = today.minusDays(daysToSubtract.toLong())
                            val isFuture = cellDate.isAfter(today)

                            val completedCount = if (isFuture) {
                                0
                            } else {
                                habits.count { it.completedDates.contains(cellDate.toString()) }
                            }

                            val color = when {
                                isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                completedCount == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                completedCount == 1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                completedCount in 2..3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                completedCount == 4 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.stats_10_weeks_ago), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.stats_today), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HabitDetailsSection(habits: List<Habit>, timeRangeIndex: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.stats_details_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.stats_details_desc),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp, top = 2.dp)
        )

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (habits.isEmpty()) {
                Text(
                    text = stringResource(R.string.stats_no_habits_filter),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val colorsList = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color(0xFF7F77DD),
                    Color(0xFF378ADD),
                    Color(0xFFE24B4A)
                )

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cutoffDate = Calendar.getInstance().apply {
                    when (timeRangeIndex) {
                        0 -> add(Calendar.DAY_OF_YEAR, -7)
                        1 -> add(Calendar.DAY_OF_YEAR, -30)
                        2 -> add(Calendar.DAY_OF_YEAR, -90)
                    }
                }.time
                val cutoffString = dateFormat.format(cutoffDate)

                habits.forEachIndexed { index, habit ->
                    val completedInWindow = habit.completedDates.count { dateStr ->
                        if (timeRangeIndex == 3) true else dateStr >= cutoffString
                    }

                    val todayMs = System.currentTimeMillis()
                    val daysSinceCreation = habit.createdAt?.let {
                        val diff = todayMs - it.time
                        (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                    } ?: habit.completedDates.size.coerceAtLeast(1)

                    val windowDays = when(timeRangeIndex) {
                        0 -> 7
                        1 -> 30
                        2 -> 90
                        else -> daysSinceCreation
                    }

                    val possibleDays = minOf(windowDays, daysSinceCreation)

                    val realProgress = if (possibleDays > 0) {
                        (completedInWindow.toFloat() / possibleDays.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    val percentString = "${(realProgress * 100).toInt()}%"
                    val habitColor = colorsList[index % colorsList.size]

                    HabitDetailItem(
                        emoji = habit.icon.ifEmpty { "🎯" },
                        name = habit.name.ifEmpty { stringResource(R.string.unknown_habit) },
                        percent = percentString,
                        streak = stringResource(R.string.stats_streak_days_suffix, habit.streak),
                        progress = realProgress,
                        color = habitColor
                    )
                }
            }
        }
    }
}

@Composable
fun HabitDetailItem(emoji: String, name: String, percent: String, streak: String, progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                Text(text = percent, fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                drawRoundRect(color = color.copy(alpha = 0.2f), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(color = color, size = size.copy(width = size.width * animatedProgress), cornerRadius = CornerRadius(2.dp.toPx()))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = streak, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.wrapContentWidth())
    }
}

@Composable
fun StreakCalendarSection(habits: List<Habit>) {
    val today = remember { LocalDate.now() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.stats_streak_calendar),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            val daysOfWeek = stringArrayResource(R.array.days_of_week_short).toList()

            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEachIndexed { dayIndex, dayLabel ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(28.dp)
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (week in 0 until 5) {
                                val daysToSubtract = ((4 - week) * 7) + (today.dayOfWeek.value - 1) - dayIndex
                                val cellDate = today.minusDays(daysToSubtract.toLong())
                                val isFuture = cellDate.isAfter(today)
                                val dateStr = cellDate.toString()

                                val color = if (isFuture) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                } else {
                                    val anyCompleted = habits.any { it.completedDates.contains(dateStr) }
                                    when {
                                        anyCompleted -> MaterialTheme.colorScheme.primary
                                        habits.isNotEmpty() -> Color(0xFFE24B4A)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                LegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.stats_legend_completed))
                LegendItem(color = Color(0xFFE24B4A), label = stringResource(R.string.stats_legend_skipped))
                LegendItem(color = MaterialTheme.colorScheme.surfaceVariant, label = stringResource(R.string.stats_legend_no_data))
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}