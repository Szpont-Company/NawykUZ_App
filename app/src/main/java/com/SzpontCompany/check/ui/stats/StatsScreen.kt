package com.SzpontCompany.check.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.History
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
import com.SzpontCompany.check.data.habit.Habit
import kotlin.random.Random

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
            HabitFilterChips(
                habits = uiState.habits,
                selectedHabitId = uiState.selectedHabitId,
                onSelect = { viewModel.setHabitFilter(it) }
            )
        }

        item {
            WeeklyActivityChart(
                chartData = uiState.weeklyChartData,
                averagePercentage = uiState.weeklyAveragePercentage
            )
        }

        item { HeatmapSection() }

        item { HabitDetailsSection(habits = uiState.filteredHabits) }

        item { StreakCalendarSection() }
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
            text = "Statystyki",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.FilterAlt, contentDescription = "Filtruj", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = { },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.History, contentDescription = "Historia", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TimeRangeSelector(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val options = listOf("7 dni", "30 dni", "3 mies.", "Wszystko")

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
        0 -> "Ostatnie 7 dni"
        1 -> "Ostatnie 30 dni"
        2 -> "Ostatnie 90 dni"
        else -> "Łącznie"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Ukończono",
                value = totalCompleted.toString(),
                subtext = timeRangeText, subtextColor = MaterialTheme.colorScheme.primary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Skuteczność",
                value = "$successRate%",
                subtext = timeRangeText, subtextColor = MaterialTheme.colorScheme.primary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Najdłuższy streak",
                value = bestStreak.toString(),
                valueSuffix = "dni",
                subtext = "Aktualny rekord", subtextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Monety (XP)",
                value = coins.toString(),
                subtext = "Na walkę Habit Battle", subtextColor = MaterialTheme.colorScheme.primary
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
            text = "Wszystko",
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
    chartData: List<Pair<String, Float>>,
    averagePercentage: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Aktywność tygodniowa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = "śr. $averagePercentage% / dzień", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
                val dayLabel = data.first
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
fun HeatmapSection() {
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
                text = "Mapa nawyków (10 tyg.)",
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

        val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEachIndexed { index, dayLabel ->
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
                            val intensity = Random.nextFloat()
                            val color = when {
                                intensity < 0.2f -> MaterialTheme.colorScheme.surfaceVariant
                                intensity < 0.4f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                intensity < 0.6f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                intensity < 0.8f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
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
            Text("10 tyg. temu", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("dziś", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HabitDetailsSection(habits: List<Habit>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Nawyki — szczegóły",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (habits.isEmpty()) {
                Text(
                    text = "Brak nawyków dla wybranego filtru.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color(0xFF7F77DD),
                    Color(0xFF378ADD),
                    Color(0xFFE24B4A)
                )

                habits.forEachIndexed { index, habit ->
                    val streak = habit.streak
                    val isHabitActive = streak > 0

                    HabitDetailItem(
                        emoji = habit.icon.ifEmpty { "🎯" },
                        name = habit.name.ifEmpty { "Nieznany nawyk" },
                        percent = if(isHabitActive) "Aktywny" else "Wstrzymany",
                        streak = "$streak dni streak",
                        progress = if(isHabitActive) 0.7f else 0.2f,
                        color = colors[index % colors.size]
                    )
                }
            }
        }
    }
}

@Composable
fun HabitDetailItem(emoji: String, name: String, percent: String, streak: String, progress: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
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
                drawRoundRect(color = color, size = size.copy(width = size.width * progress), cornerRadius = CornerRadius(2.dp.toPx()))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = streak, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.wrapContentWidth())
    }
}

@Composable
fun StreakCalendarSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Streak kalendarz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 12.dp))

        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEachIndexed { index, dayLabel ->
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
                                val status = Random.nextInt(10)
                                val color = when {
                                    status < 2 -> Color(0xFFE24B4A)
                                    status < 3 -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.primary
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
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Ukończono")
                LegendItem(color = Color(0xFFE24B4A), label = "Pominięto")
                LegendItem(color = MaterialTheme.colorScheme.surfaceVariant, label = "Brak danych")
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