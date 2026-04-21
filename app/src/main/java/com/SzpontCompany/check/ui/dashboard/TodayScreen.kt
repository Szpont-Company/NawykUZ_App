package com.SzpontCompany.check.ui.dashboard

import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.SzpontCompany.check.ui.components.EmojiExplosionEffect
import com.SzpontCompany.check.ui.theme.getColorByName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.SzpontCompany.check.R
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

data class HabitMock(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val progress: String,
    val stat1Value: String, val stat1Label: String,
    val stat2Value: String, val stat2Label: String,
    val stat3Value: String, val stat3Label: String,
    val isDoneToday: Boolean = false
)

data class ExplosionData(val id: Long, val emoji: String)

@Composable
fun TodayScreen(
    onProfileClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val habitsList = remember {
        mutableStateListOf(
            HabitMock("🚶", "Spacer", "8 000 kroków • codziennie", "63%", "5 040", "kroków", "14 dni", "streak", "82%", "tydzień"),
            HabitMock("📖", "Czytanie", "30 min • 5×tydzień", "40%", "12 min", "dziś", "7 dni", "streak", "60%", "tydzień"),
            HabitMock("💧", "Picie wody", "2 litry • codziennie", "50%", "1 litr", "dziś", "3 dni", "streak", "90%", "tydzień"),
            HabitMock("🏋️‍♀️", "Siłownia", "30 min • codziennie", "0%", "0 min", "dziś", "0 dni", "streak", "10%", "tydzień")

        )
    }

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
                    onNotificationsClick = onNotificationsClick
                )
                Spacer(modifier = Modifier.height(24.dp))
                HeroCard()
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Nawyki dziś (${habitsList.size})",
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

            itemsIndexed(habitsList) { index, habit ->
                HabitCard(
                    habit = habit,
                    onDoneClick = {
                        val listIndex = habitsList.indexOf(habit)
                        if (index != -1) {
                            val wasDone = habit.isDoneToday
                            habitsList[listIndex] = habit.copy(isDoneToday = !wasDone)

                            if (!wasDone) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val currentId = java.util.UUID.randomUUID().mostSignificantBits
                                val newExplosion = ExplosionData(currentId, habit.emoji)
                                explosions.add(newExplosion)
                                coroutineScope.launch {
                                    delay(2000)
                                    explosions.remove(newExplosion)
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if ((index + 1) % 3 == 0 && index != habitsList.lastIndex) {
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
fun TopSection(onProfileClick: () -> Unit,
               onOptionsClick: () -> Unit,
               onNotificationsClick: () -> Unit,
               viewModel: TodayViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

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
                    if(state.isLoading) Modifier.shimmerEffect()
                    else Modifier.background(getColorByName(state.user?.bgColor ?: "Mint"))
                )
                .clickable(enabled = !state.isLoading) {onProfileClick() },
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
                Text(state.user?.name ?: "", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
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
fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Aktualny streak", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("21 dni", color = MaterialTheme.colorScheme.onPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rekord: 34 dni", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            MiniBarChart(color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun MiniBarChart(color: Color) {
    val heights = listOf(0.4f, 0.6f, 0.8f, 0.5f, 0.9f, 1.0f, 0.3f)
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(48.dp)) {
        heights.forEachIndexed { index, fraction ->
            val animatedFraction by animateFloatAsState(
                targetValue = if (animationPlayed) fraction else 0.01f,
                animationSpec = tween(durationMillis = 800, delayMillis = index * 100, easing = FastOutSlowInEasing),
                label = "bar_anim_$index"
            )
            Box(modifier = Modifier.width(6.dp).fillMaxHeight(animatedFraction).clip(RoundedCornerShape(3.dp)).background(color))
        }
    }
}

@Composable
fun HabitCard(habit: HabitMock, onDoneClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

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
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = habit.emoji,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(habit.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(onClick = onDoneClick, modifier = Modifier.size(32.dp)) {
                    if (habit.isDoneToday) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Zrobione", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    } else {
                        Box(modifier = Modifier.size(22.dp).border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(habit.progress, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

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
                    StatBox(modifier = Modifier.weight(1f), value = habit.stat1Value, label = habit.stat1Label)
                    StatBox(modifier = Modifier.weight(1f), value = habit.stat2Value, label = habit.stat2Label)
                    StatBox(modifier = Modifier.weight(1f), value = habit.stat3Value, label = habit.stat3Label)
                }

                Spacer(modifier = Modifier.height(16.dp))

                HeatmapMock()

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDoneClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (habit.isDoneToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                            contentColor = if (habit.isDoneToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (habit.isDoneToday) "Zrobione" else "Zaznacz", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = null
                    ) {
                        Text("Notatka")
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
        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110") // Testowe ID dla reklam natywnych
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    if (nativeAd != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
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
                    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)

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
                .height(82.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Ładowanie...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, value: String, label: String) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.background).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HeatmapMock() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background
    val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        daysOfWeek.forEachIndexed { index, dayLabel ->

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
                        val intensity = Random.nextFloat()
                        val boxColor = if (intensity > 0.4f) primaryColor.copy(alpha = intensity) else bgColor

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(boxColor)
                        )
                    }
                }
            }
        }
    }
}