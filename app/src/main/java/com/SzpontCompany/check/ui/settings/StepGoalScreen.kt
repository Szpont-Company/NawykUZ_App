@file:OptIn(ExperimentalMaterial3Api::class)

package com.SzpontCompany.check.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import kotlin.math.roundToInt

// Pomocnicza funkcja formatująca liczbę z odstępem (np. 8 000)
fun formatGoalNumber(goal: Int): String {
    return String.format("%,d", goal).replace(',', ' ')
}

data class PopularGoal(
    val value: Int,
    val label: String
)

@Composable
fun StepGoalScreen() {
    // Stan dla suwaka (Float jest wymagany przez Slider)
    var sliderPosition by remember { mutableFloatStateOf(8000f) }

    // Zaokrąglamy pozycję suwaka do pełnych setek, żeby nie było dziwnych liczb np. 8123
    val currentGoal = (sliderPosition / 100).roundToInt() * 100

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // --- GÓRNY PASEK ---
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Cel kroków",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Wróć */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        // --- PRZYCISK NA DOLE ---
        bottomBar = {
            Button(
                onClick = { /* TODO: Zapisz currentGoal */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Mint)
            ) {
                Text(
                    text = "Zapisz cel",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- SEKCJA DZIENNY CEL ---
            Text(
                text = "DZIENNY CEL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )

            // --- BOX Z GŁÓWNĄ WARTOŚCIĄ I SUWAKIEM ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // --- GŁÓWNA WARTOŚĆ Z PRZYCISKAMI +/- ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Przycisk MINUS
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    val newValue = (sliderPosition - 500).coerceAtLeast(1000f)
                                    sliderPosition = newValue
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "−",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // GŁÓWNA WARTOŚĆ
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formatGoalNumber(currentGoal),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "kroków",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // Przycisk PLUS
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    val newValue = (sliderPosition + 500).coerceAtMost(20000f)
                                    sliderPosition = newValue
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- SUWAK (SLIDER) ---
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 1000f..20000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Mint,
                            activeTrackColor = Mint,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Wartości minimalna i maksymalna pod suwakiem
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1 000",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "20 000",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- POPULARNE CELE ---
            Text(
                text = "POPULARNE CELE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 opcje ułożone w 2 rzędach po 2
            val popularGoals = listOf(
                PopularGoal(5000, "Aktywny tryb życia"),
                PopularGoal(8000, "Zalecane WHO"),
                PopularGoal(10000, "Klasyczny cel"),
                PopularGoal(15000, "Sport i bieganie")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PopularGoalItem(
                        goal = popularGoals[0],
                        isSelected = currentGoal == popularGoals[0].value,
                        modifier = Modifier.weight(1f),
                        onClick = { sliderPosition = popularGoals[0].value.toFloat() }
                    )
                    PopularGoalItem(
                        goal = popularGoals[1],
                        isSelected = currentGoal == popularGoals[1].value,
                        modifier = Modifier.weight(1f),
                        onClick = { sliderPosition = popularGoals[1].value.toFloat() }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PopularGoalItem(
                        goal = popularGoals[2],
                        isSelected = currentGoal == popularGoals[2].value,
                        modifier = Modifier.weight(1f),
                        onClick = { sliderPosition = popularGoals[2].value.toFloat() }
                    )
                    PopularGoalItem(
                        goal = popularGoals[3],
                        isSelected = currentGoal == popularGoals[3].value,
                        modifier = Modifier.weight(1f),
                        onClick = { sliderPosition = popularGoals[3].value.toFloat() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PopularGoalItem(
    goal: PopularGoal,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Mint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) Mint else Color.Transparent
    val textColor = if (isSelected) Mint else MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = formatGoalNumber(goal.value),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = goal.label,
                color = labelColor,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StepGoalScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        StepGoalScreen()
    }
}
