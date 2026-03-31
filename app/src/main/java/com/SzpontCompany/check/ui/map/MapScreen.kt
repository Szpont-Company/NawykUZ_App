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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Kolory motywu (dark) ──────────────────────────────────────────────────────
private val BgDark        = Color(0xFF0F1117)
private val SurfaceDark   = Color(0xFF1A1D26)
private val CardDark      = Color(0xFF22262F)
private val GreenAccent   = Color(0xFF4ADE80)
private val GreenDim      = Color(0xFF1A3A28)
private val MapBg         = Color(0xFF141922)
private val MapRoad       = Color(0xFF1E2533)
private val MapPark       = Color(0xFF1A3328)
private val TextPrimary   = Color(0xFFE8EAF0)
private val TextSecondary = Color(0xFF6B7280)
private val TextMuted     = Color(0xFF3D4350)
private val RedDot        = Color(0xFFEF4444)
private val TabUnderline  = Color(0xFF4ADE80)

enum class MapTab { TODAY, ROUTES, FRIENDS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    var selectedTab by remember { mutableStateOf(MapTab.TODAY) }

    // Zaktualizowany, poprawny stan BottomSheet'a
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        // Kolor tła samego "uchwytu" i górnej części
        sheetContainerColor = SurfaceDark,
        // Jak dużo ekranu wystaje na dole w stanie zwiniętym
        sheetPeekHeight = 160.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        },
        sheetContent = {
            // ── ZAWARTOŚĆ BOTTOM SHEET ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.85f) // Rozwija się do max 85% wysokości ekranu
                    .fillMaxWidth()
            ) {
                // 1. Pasek statystyk na górze panelu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(9.dp).clip(CircleShape).background(RedDot)
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

                // 3. Treść wybranej zakładki (z tłem BgDark)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
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
            // ── TŁO (MAPA) ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark)
            ) {
                // Mapa przyjmuje teraz cały dostępny rozmiar w tle
                MapPlaceholder(modifier = Modifier.fillMaxSize())

                // Przyciski górne: Eksploracja / Znajomi / Ustawienia
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
                            .background(SurfaceDark.copy(alpha = 0.85f))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                // Zoom +/- (umieszczony nieco niżej po prawej stronie)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp, top = 100.dp),
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Karta z ringiem kroków + szczegółami
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Ring postępu – większy
            StepsRing(
                current = 5057,
                goal    = 8000,
                modifier = Modifier.size(96.dp)
            )

            // Metryki – zajmują resztę szerokości
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wiersz: Dystans + Kcal
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Dystans", color = TextSecondary, fontSize = 11.sp)
                        Text("3.8 km", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Kcal", color = TextSecondary, fontSize = 11.sp)
                        Text("182", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Separator
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextMuted.copy(alpha = 0.4f)))

                // Tempo + pasek
                TempoRow(tempo = "5 min/km", percent = 63)

                // Separator
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextMuted.copy(alpha = 0.4f)))

                // Streak
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Streak kroków", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        "14 dni z rzędu",
                        color = GreenAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("🔥", fontSize = 13.sp)
                }
            }
        }

        // Przycisk Rozpocznij trasę
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardDark)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Rozpocznij trasę", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        // Spacer na samym dole aby wygodnie scrollować przy NavigationBar
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Zakładka TRASY
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RoutesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Historia tras", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        repeat(3) { i ->
            RouteHistoryCard(
                name     = listOf("Poranny spacer", "Bieg wieczorny", "Wycieczka do parku")[i],
                date     = listOf("Dziś, 07:15", "Wczoraj, 18:42", "Wt, 09:00")[i],
                distance = listOf("3.8 km", "6.2 km", "2.1 km")[i],
                duration = listOf("32:23", "38:15", "22:40")[i],
                kcal     = listOf("182", "310", "98")[i]
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Zakładka ZNAJOMI
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FriendsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aktywni dziś", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        listOf(
            Triple("Kacper M.", "8 204 kroków", "#1"),
            Triple("Zuzia K.",  "6 731 kroków", "#2"),
            Triple("Bartek T.", "5 057 kroków", "#3 (Ty)")
        ).forEachIndexed { idx, (name, steps, rank) ->
            FriendRow(name = name, steps = steps, rank = rank, isMe = idx == 2)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Komponenty pomocnicze
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(color = MapBg)

        // Siatka ulic (dostosowana pod pełny ekran)
        val roadW = 14f
        val roadColor = MapRoad

        for (y in listOf(size.height * 0.2f, size.height * 0.4f, size.height * 0.6f, size.height * 0.8f)) {
            drawLine(roadColor, Offset(0f, y), Offset(size.width, y), strokeWidth = roadW)
        }
        for (x in listOf(size.width * 0.2f, size.width * 0.45f, size.width * 0.7f, size.width * 0.88f)) {
            drawLine(roadColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = roadW)
        }

        // Park (zielony blok)
        drawRect(
            color = MapPark,
            topLeft = Offset(size.width * 0.22f, size.height * 0.18f),
            size = Size(size.width * 0.22f, size.height * 0.20f)
        )

        // Trasa przerywana
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
            color = GreenAccent,
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
            )
        )

        // Punkt końcowy (góra trasy)
        drawCircle(color = GreenAccent, radius = 14f, center = pathPoints.last())
        drawCircle(color = MapBg,       radius = 8f,  center = pathPoints.last())

        // Punkt startowy (aktualny) – zielone kółko
        drawCircle(color = GreenAccent, radius = 18f, center = pathPoints.first())
        drawCircle(color = MapBg,       radius = 10f, center = pathPoints.first())
        drawCircle(color = GreenAccent, radius = 5f,  center = pathPoints.first())
    }
}

@Composable
private fun MapChip(icon: @Composable () -> Unit, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark.copy(alpha = 0.90f))
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides TextPrimary) { icon() }
        Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ZoomButton(symbol: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun StatCell(value: String, label: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = TextPrimary,
            fontSize = if (isHighlight) 22.sp else 16.sp,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.SemiBold,
            letterSpacing = if (isHighlight) (-0.5).sp else 0.sp
        )
        Text(
            text = label,
            color = TextSecondary,
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
            .background(TextMuted)
    )
}

@Composable
private fun MapTabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val underlineColor by animateColorAsState(
        targetValue = if (selected) TabUnderline else Color.Transparent,
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
            color = if (selected) GreenAccent else TextSecondary,
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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            // Tło
            drawCircle(color = CardDark, radius = radius, center = center, style = Stroke(stroke))
            // Postęp
            drawArc(
                color = GreenAccent,
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
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(text = "/ ${goal / 1000}k", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TempoRow(tempo: String, percent: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tempo", color = TextSecondary, fontSize = 11.sp)
            Text("$percent%", color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(tempo, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        // Pasek postępu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CardDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(GreenAccent)
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
            .background(SurfaceDark)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MapBg),
            contentAlignment = Alignment.Center
        ) {
            Text("🗺️", fontSize = 22.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(date, color = TextSecondary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$distance  ·  $duration  ·  $kcal kcal", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun FriendRow(name: String, steps: String, rank: String, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isMe) GreenDim else SurfaceDark)
            .border(
                width = if (isMe) 1.dp else 0.dp,
                color = if (isMe) GreenAccent.copy(alpha = 0.4f) else Color.Transparent,
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
                .background(CardDark),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().toString(), color = GreenAccent, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name,  color = TextPrimary,   fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(steps, color = TextSecondary, fontSize = 12.sp)
        }
        Text(rank, color = if (isMe) GreenAccent else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}