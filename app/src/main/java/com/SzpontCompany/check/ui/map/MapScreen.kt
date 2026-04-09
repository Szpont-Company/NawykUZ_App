package com.SzpontCompany.check.ui.map

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MapTab { TODAY, ROUTES, FRIENDS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    var selectedTab by remember { mutableStateOf(MapTab.TODAY) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetPeekHeight = 125.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize() // ZMIANA: Zdejmujemy limit 0.65f, pasek rozwija się na pełny ekran!
            ) {
                // 1. Pasek statystyk na górze panelu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Spacer(Modifier.width(8.dp))
                    StatCell(value = "5057", label = "kroków", isHighlight = true)
                    Spacer(Modifier.weight(1f))
                    StatDivider()
                    Spacer(Modifier.weight(1f))
                    StatCell(value = "3.8", label = "km")
                    Spacer(Modifier.weight(1f))
                    StatDivider()
                    Spacer(Modifier.weight(1f))
                    StatCell(value = "182", label = "kcal")
                    Spacer(Modifier.weight(1f))
                    StatDivider()
                    Spacer(Modifier.weight(1f))
                    StatCell(value = "32:23", label = "czas")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Zakładki
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    MapTabItem("Dziś",    selectedTab == MapTab.TODAY)   { selectedTab = MapTab.TODAY }
                    MapTabItem("Trasy",   selectedTab == MapTab.ROUTES)  { selectedTab = MapTab.ROUTES }
                    MapTabItem("Znajomi", selectedTab == MapTab.FRIENDS) { selectedTab = MapTab.FRIENDS }
                }

                // 3. Treść wybranej zakładki
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (selectedTab) {
                        MapTab.TODAY   -> TodayTab()
                        MapTab.ROUTES  -> RoutesTab()
                        MapTab.FRIENDS -> FriendsTab()
                    }
                }
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MapPlaceholder(modifier = Modifier.fillMaxSize())

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapChip(
                        icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = "Eksploracja"
                    )
                    MapChip(
                        icon = { Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = "Znajomi"
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(16.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp)
                        .offset(y = (-40).dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ZoomButton("+")
                    ZoomButton("−")
                }
            }
        }
    )
}

@Composable
private fun TodayTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .navigationBarsPadding(),
        // ZMIANA: Usunięty verticalScroll, żeby karta mogła rozepchnąć się na wysokość
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GŁÓWNA KARTA – dzięki weight(1f) wypełnia calutką dostępną wysokość
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // DUŻY WYKRES - zajmuje max przestrzeni, zachowując proporcje koła
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                StepsRing(
                    current = 5057,
                    goal    = 8000,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // STATYSTYKI POD WYKRESEM
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Wiersz: Dystans + Kcal
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Dystans", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("3.8 km", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Kcal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("182", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))

                TempoRow(tempo = "5 min/km", percent = 63)

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Streak kroków", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "14 dni z rzędu ",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("🔥", fontSize = 16.sp)
                    }
                }
            }
        }

        // PRZYCISK – Zawsze twardo przyklejony na dole
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Rozpocznij trasę", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun RoutesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Historia tras", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        repeat(3) { i ->
            RouteHistoryCard(
                name     = listOf("Poranny spacer", "Bieg wieczorny", "Wycieczka do parku")[i],
                date     = listOf("Dziś, 07:15", "Wczoraj, 18:42", "Wt, 09:00")[i],
                distance = listOf("3.8 km", "6.2 km", "2.1 km")[i],
                duration = listOf("32:23", "38:15", "22:40")[i],
                kcal     = listOf("182", "310", "98")[i]
            )
        }
    }
}

@Composable
private fun FriendsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aktywni dziś", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        listOf(
            Triple("Kacper M.", "8 204 kroków", "#1"),
            Triple("Zuzia K.",  "6 731 kroków", "#2"),
            Triple("Bartek T.", "5 057 kroków", "#3 (Ty)")
        ).forEachIndexed { idx, (name, steps, rank) ->
            FriendRow(name = name, steps = steps, rank = rank, isMe = idx == 2)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Komponenty pomocnicze
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    val mapBgColor = MaterialTheme.colorScheme.surfaceVariant
    val mapRoadColor = MaterialTheme.colorScheme.outlineVariant
    val mapParkColor = MaterialTheme.colorScheme.secondaryContainer
    val accentColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        drawRect(color = mapBgColor)

        val roadW = 14f
        for (y in listOf(size.height * 0.2f, size.height * 0.4f, size.height * 0.6f, size.height * 0.8f)) {
            drawLine(mapRoadColor, Offset(0f, y), Offset(size.width, y), strokeWidth = roadW)
        }
        for (x in listOf(size.width * 0.2f, size.width * 0.45f, size.width * 0.7f, size.width * 0.88f)) {
            drawLine(mapRoadColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = roadW)
        }

        drawRect(
            color = mapParkColor,
            topLeft = Offset(size.width * 0.22f, size.height * 0.18f),
            size = Size(size.width * 0.22f, size.height * 0.20f)
        )

        val pathPoints = listOf(
            Offset(size.width * 0.38f, size.height * 0.50f),
            Offset(size.width * 0.38f, size.height * 0.45f),
            Offset(size.width * 0.33f, size.height * 0.30f),
            Offset(size.width * 0.33f, size.height * 0.20f),
            Offset(size.width * 0.45f, size.height * 0.12f)
        )
        val routePath = Path().apply {
            moveTo(pathPoints[0].x, pathPoints[0].y)
            pathPoints.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = routePath,
            color = accentColor,
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
            )
        )

        drawCircle(color = accentColor, radius = 14f, center = pathPoints.last())
        drawCircle(color = mapBgColor,  radius = 8f,  center = pathPoints.last())

        drawCircle(color = accentColor, radius = 18f, center = pathPoints.first())
        drawCircle(color = mapBgColor,  radius = 10f, center = pathPoints.first())
        drawCircle(color = accentColor, radius = 5f,  center = pathPoints.first())
    }
}

@Composable
private fun MapChip(icon: @Composable () -> Unit, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) { icon() }
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ZoomButton(symbol: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun StatCell(value: String, label: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = if (isHighlight) 22.sp else 16.sp,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.SemiBold,
            letterSpacing = if (isHighlight) (-0.5).sp else 0.sp
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun MapTabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val underlineColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200), label = "tab_underline"
    )
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(40.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(underlineColor)
        )
    }
}

@Composable
private fun StepsRing(current: Int, goal: Int, modifier: Modifier = Modifier) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx() // Zwiększona grubość linii dla dużego ringu
            val radius = (size.minDimension - stroke) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(color = trackColor, radius = radius, center = center, style = Stroke(stroke))

            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = current.toString(),
                color = textColor,
                fontSize = 32.sp, // ZMIANA: Zwiększona czcionka wewnątrz ringu (bo wykres będzie potężny!)
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Text(text = "/ ${goal / 1000}k", color = secondaryTextColor, fontSize = 16.sp)
        }
    }
}

@Composable
private fun TempoRow(tempo: String, percent: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tempo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Text("$percent%", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(tempo, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun RouteHistoryCard(name: String, date: String, distance: String, duration: String, kcal: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("🗺️", fontSize = 22.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$distance  ·  $duration  ·  $kcal kcal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun FriendRow(name: String, steps: String, rank: String, isMe: Boolean) {
    val bgColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isMe) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().toString(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name,  color = MaterialTheme.colorScheme.onBackground,   fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(steps, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Text(rank, color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}