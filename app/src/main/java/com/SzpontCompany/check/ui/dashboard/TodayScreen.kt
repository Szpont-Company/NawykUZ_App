package com.SzpontCompany.check.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.R
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.ui.components.EmojiExplosionEffect
import com.SzpontCompany.check.ui.theme.getColorByName
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class ExplosionData(val id: Long, val emoji: String)

@Composable
fun TodayScreen(
    onProfileClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    viewModel: TodayViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val explosions = remember { mutableStateListOf<ExplosionData>() }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                TopSection(
                    onProfileClick = onProfileClick,
                    onOptionsClick = onOptionsClick,
                    onNotificationsClick = onNotificationsClick,
                    state = state
                )
                Spacer(modifier = Modifier.height(24.dp))

                val currentStreak = state.user?.currentStreak ?: 0
                val bestStreak = state.user?.bestStreak ?: 0
                val weeklyProgress = state.user?.weeklyProgress ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)

                HeroCard(
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    weeklyProgress = weeklyProgress
                )
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Twoje nawyki (${state.habits.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Zobacz wszystkie",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.habits.isEmpty() && !state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Brak aktywnych nawyków. Dodaj coś!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(items = state.habits, key = { _, habit -> habit.id }) { index, habit ->
                HabitCard(
                    habit = habit,
                    onToggleDone = { isNowDone ->
                        if (isNowDone) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val id = UUID.randomUUID().mostSignificantBits
                            val iconToExplode = if(habit.icon.isNotEmpty()) habit.icon else "🔥"
                            val newExplosion = ExplosionData(id, iconToExplode)

                            explosions.add(newExplosion)
                            coroutineScope.launch {
                                delay(2000)
                                explosions.removeAll { it.id == newExplosion.id }
                            }
                        }

                        viewModel.toggleHabitCompletion(habit.id, isNowDone)
                    },
                    onSaveNote = { date, newNote ->
                        viewModel.updateHabitDailyNote(habit.id, date, newNote)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if ((index + 1) % 3 == 0 && index != state.habits.lastIndex) {
                    NativeAdCard()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        explosions.forEach { explosion ->
            key(explosion.id) {
                EmojiExplosionEffect(
                    modifier = Modifier.fillMaxSize(),
                    emoji = explosion.emoji,
                    triggerId = explosion.id
                )
            }
        }
    }
}

@Composable
fun TopSection(
    onProfileClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    state: TodayUiState
) {
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }

    val greeting = when (currentHour) {
        in 0..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .then(
                    if (state.isLoading) Modifier.shimmerEffect()
                    else Modifier.background(getColorByName(state.user?.bgColor ?: "Mint"))
                )
                .clickable(enabled = !state.isLoading) { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            val displayAvatar = if (state.user?.avatarEmoji.isNullOrEmpty()) state.user?.initials ?: "MK" else state.user?.avatarEmoji ?: ""
            Text(displayAvatar, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(id = greeting), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if(state.isLoading) {
                Box(modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(0.6f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
                )
            } else {
                Text(state.user?.name ?: "Użytkownik", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Powiadomienia",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val hasUnreadNotifications = true
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-10).dp, y = 10.dp)
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onOptionsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Ustawienia",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HeroCard(currentStreak: Int, bestStreak: Int, weeklyProgress: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Aktualny streak", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("$currentStreak dni", color = MaterialTheme.colorScheme.onPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rekord: $bestStreak dni", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            MiniBarChart(
                color = MaterialTheme.colorScheme.onPrimary,
                weeklyProgress = weeklyProgress
            )
        }
    }
}

@Composable
fun MiniBarChart(color: Color, weeklyProgress: List<Float>) {
    val heights = if (weeklyProgress.size == 7) weeklyProgress else List(7) { 0f }
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(48.dp)) {
        heights.forEachIndexed { index, fraction ->
            val safeFraction = fraction.coerceIn(0f, 1f)
            val animatedFraction by animateFloatAsState(
                targetValue = if (animationPlayed) safeFraction else 0.01f,
                animationSpec = tween(durationMillis = 800, delayMillis = index * 100, easing = FastOutSlowInEasing),
                label = "bar_anim_$index"
            )
            Box(modifier = Modifier
                .width(6.dp)
                .fillMaxHeight(animatedFraction)
                .clip(RoundedCornerShape(3.dp))
                .background(color))
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onToggleDone: (Boolean) -> Unit,
    onSaveNote: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var editingNoteDate by remember { mutableStateOf<String?>(null) }

    val today = remember { LocalDate.now() }
    val todayString = remember { today.toString() }
    val isDoneToday = habit.completedDates.contains(todayString)
    val todayNote = habit.dailyNotes[todayString] ?: ""

    val expectedDatesInLast30 = remember(habit, today) {
        val last30Dates = (0..29).map { today.minusDays(it.toLong()) }

        when {
            habit.selectedDays.isNotEmpty() -> {
                last30Dates.filter { date ->
                    val dayNameEn = date.dayOfWeek.name
                    val dayNameShortPl = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")[date.dayOfWeek.value - 1]
                    val dayNameLongPl = listOf("poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota", "niedziela")[date.dayOfWeek.value - 1]
                    val dayValueStr = date.dayOfWeek.value.toString()

                    habit.selectedDays.any { selectedDay ->
                        selectedDay.equals(dayNameEn, ignoreCase = true) ||
                                selectedDay.equals(dayNameShortPl, ignoreCase = true) ||
                                selectedDay.equals(dayNameLongPl, ignoreCase = true) ||
                                selectedDay == dayValueStr
                    }
                }
            }
            else -> last30Dates
        }
    }

    val monthlyPercentage = remember(habit.completedDates, expectedDatesInLast30, habit, today) {
        val last30DaysStr = (0..29).map { today.minusDays(it.toLong()).toString() }

        when {
            habit.selectedDays.isNotEmpty() -> {
                if (expectedDatesInLast30.isEmpty()) return@remember 0

                val expectedDatesStr = expectedDatesInLast30.map { it.toString() }
                val completedExpectedCount = habit.completedDates.count { it in expectedDatesStr }

                ((completedExpectedCount.toFloat() / expectedDatesStr.size) * 100).toInt().coerceIn(0, 100)
            }
            habit.timesPerWeek > 0 -> {
                val expectedTotal = (habit.timesPerWeek * (30.0 / 7.0)).toInt()
                if (expectedTotal <= 0) return@remember 0

                val completedCount = habit.completedDates.count { it in last30DaysStr }
                ((completedCount.toFloat() / expectedTotal) * 100).toInt().coerceIn(0, 100)
            }
            else -> {
                val completedCount = habit.completedDates.count { it in last30DaysStr }
                ((completedCount.toFloat() / 30f) * 100).toInt().coerceIn(0, 100)
            }
        }
    }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .animateContentSize(
                finishedListener = { _, _ ->
                    if (expanded) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(getColorByName(habit.colorName).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if(habit.icon.isNotEmpty()) habit.icon else "🔥",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (habit.battleId != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "⚔️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bitwa z: ${habit.opponentName ?: "Nieznajomy"}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        habit.frequency,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        onToggleDone(!isDoneToday)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isDoneToday) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Zrobione", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    } else {
                        Box(modifier = Modifier
                            .size(22.dp)
                            .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(if (isDoneToday) "100%" else "0%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Rozwiń/Zwiń",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox(modifier = Modifier.weight(1f), value = habit.dailyGoal.toString(), label = habit.unit.ifEmpty { "Cel" })
                    StatBox(modifier = Modifier.weight(1f), value = habit.streak.toString(), label = "streak")
                    StatBox(modifier = Modifier.weight(1f), value = "${monthlyPercentage}%", label = "30 dni")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HabitHeatmap(
                    habit = habit,
                    onDayClick = { clickedDate ->
                        editingNoteDate = clickedDate
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onToggleDone(!isDoneToday)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDoneToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                            contentColor = if (isDoneToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isDoneToday) "Zrobione" else "Zaznacz", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { editingNoteDate = todayString },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = null
                    ) {
                        Text(if (todayNote.isNotEmpty()) "Edytuj notatkę" else "Notatka")
                    }
                }
            }
        }

        editingNoteDate?.let { date ->
            HabitNoteDialog(
                initialNote = habit.dailyNotes[date] ?: "",
                dateString = date,
                onDismiss = { editingNoteDate = null },
                onSave = { newNote ->
                    onSaveNote(date, newNote)
                    editingNoteDate = null
                }
            )
        }
    }
}

@Composable
fun HabitHeatmap(
    habit: Habit,
    onDayClick: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background
    val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    val today = LocalDate.now()

    val completedDates = habit.completedDates.mapNotNull { dateString ->
        try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        daysOfWeek.forEachIndexed { dayIndex, dayLabel ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (week in 0 until 8) {
                        val daysToSubtract = ((7 - week) * 7) + (today.dayOfWeek.value - 1) - dayIndex
                        val cellDate = today.minusDays(daysToSubtract.toLong())
                        val dateString = cellDate.toString()

                        val isFuture = cellDate.isAfter(today)
                        val isCompleted = completedDates.contains(cellDate)
                        val hasNote = habit.dailyNotes[dateString]?.isNotBlank() == true

                        val boxColor = when {
                            isFuture -> bgColor.copy(alpha = 0.3f)
                            isCompleted -> primaryColor
                            else -> bgColor
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(boxColor)
                                .clickable(enabled = !isFuture && hasNote) {
                                    onDayClick(dateString)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasNote) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Notatka",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isCompleted) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    val titleColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary.toArgb()
    val badgeBgColor = MaterialTheme.colorScheme.secondaryContainer.toArgb()
    val badgeTextColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()

    LaunchedEffect(Unit) {
        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    if (nativeAd != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val inflater = LayoutInflater.from(ctx)
                    val adView = inflater.inflate(R.layout.native_ad_habit_card, null) as NativeAdView

                    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                    val badgeView = adView.findViewById<TextView>(R.id.ad_badge)
                    val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
                    val ctaView = adView.findViewById<android.widget.Button>(R.id.ad_call_to_action)

                    headlineView.setTextColor(titleColor)
                    bodyView.setTextColor(bodyColor)
                    badgeView.setTextColor(badgeTextColor)
                    badgeView.backgroundTintList = android.content.res.ColorStateList.valueOf(badgeBgColor)
                    ctaView.setTextColor(onPrimaryColor)
                    ctaView.backgroundTintList = android.content.res.ColorStateList.valueOf(primaryColor)

                    headlineView.text = nativeAd?.headline
                    adView.headlineView = headlineView

                    if (nativeAd?.body == null) {
                        bodyView.visibility = View.INVISIBLE
                    } else {
                        bodyView.visibility = View.VISIBLE
                        bodyView.text = nativeAd?.body
                    }
                    adView.bodyView = bodyView

                    if (nativeAd?.icon == null) {
                        iconView.visibility = View.GONE
                    } else {
                        iconView.setImageDrawable(nativeAd?.icon?.drawable)
                        iconView.visibility = View.VISIBLE
                    }
                    adView.iconView = iconView

                    if (nativeAd?.callToAction == null) {
                        ctaView.visibility = View.INVISIBLE
                    } else {
                        ctaView.visibility = View.VISIBLE
                        ctaView.text = nativeAd?.callToAction
                    }
                    adView.callToActionView = ctaView

                    adView.setNativeAd(nativeAd!!)
                    adView
                }
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.ad_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, value: String, label: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HabitNoteDialog(
    initialNote: String,
    dateString: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteText by remember { mutableStateOf(initialNote) }
    val today = LocalDate.now().toString()
    val isPast = dateString < today

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isPast) "Podgląd notatki ($dateString)" else "Notatka ($dateString)",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { if (!isPast) noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                readOnly = isPast,
                placeholder = { Text("Zapisz swoje przemyślenia, przeszkody lub sukcesy z tego dnia...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )
        },
        confirmButton = {
            if (!isPast) {
                Button(
                    onClick = { onSave(noteText) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Zapisz", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Zamknij", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isPast) {
                TextButton(onClick = onDismiss) {
                    Text("Anuluj", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}