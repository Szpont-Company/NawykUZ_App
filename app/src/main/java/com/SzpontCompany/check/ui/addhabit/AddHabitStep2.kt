package com.SzpontCompany.check.ui.addhabit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint

@Composable
fun AddHabitStep2(
    onNextClick: (frequency: String, days: Set<String>, timesPerWeek: Int, goal: Int, unit: String) -> Unit,
    onBackClick: () -> Unit
) {
    // Stan formularza
    var frequency by remember { mutableStateOf("Codziennie") }
    var selectedDays by remember { mutableStateOf(setOf("Pn", "Śr", "Pt")) }
    var timesPerWeek by remember { mutableStateOf(3) }
    var dailyGoal by remember { mutableStateOf(30) }
    var selectedUnit by remember { mutableStateOf("min") }

    val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
    val units = listOf("min", "kroków", "razy", "ml")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Wróć", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Harmonogram",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "2 / 4",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PASEK POSTĘPU --- (Zapełniony do połowy)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f) // 2/4 szerokości
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
        }

        // SCROLLOWANA ZAWARTOŚĆ
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- CZĘSTOTLIWOŚĆ ---
            SectionTitle("CZĘSTOTLIWOŚĆ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Codziennie", "Tygodniowo", "Własne").forEach { option ->
                    SelectableChip(
                        text = option,
                        isSelected = frequency == option,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { frequency = option }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- DNI TYGODNIA / RAZY W TYGODNIU (Logika ukrywania) ---
            if (frequency == "Własne") {
                SectionTitle("DNI TYGODNIA")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else if (frequency == "Tygodniowo") {
                SectionTitle("ILE RAZY W TYGODNIU?")
                NumberPicker(
                    value = timesPerWeek,
                    onMinus = { if (timesPerWeek > 1) timesPerWeek-- },
                    onPlus = { if (timesPerWeek < 6) timesPerWeek++ },
                    suffix = "razy"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- CEL DZIENNY ---
            SectionTitle(if (frequency == "Tygodniowo") "CEL NA JEDEN RAZ" else "CEL DZIENNY")
            NumberPicker(
                value = dailyGoal,
                onMinus = { if (dailyGoal > 1) dailyGoal-- },
                onPlus = { dailyGoal++ },
                suffix = selectedUnit,
                step = if (selectedUnit == "kroków") 500 else if (selectedUnit == "ml") 100 else 1 // Logika skoków
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- JEDNOSTKA ---
            SectionTitle("JEDNOSTKA")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                units.forEach { unit ->
                    SelectableChip(
                        text = unit,
                        isSelected = selectedUnit == unit,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedUnit = unit }
                    )
                }
            }
        }

        // --- PRZYCISKI DOLNE ---
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNextClick(frequency, selectedDays, timesPerWeek, dailyGoal, selectedUnit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        ) {
            Text(text = "Dalej", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(text = "Wróć", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    suffix: String,
    step: Int = 1
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Przycisk Minus
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable {
                    // Aby uwzględnić krok (np. +/- 500) wykonujemy to w pętli lub podpinamy wyżej
                    repeat(step) { onMinus() }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Mniej", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Pole tekstowe / Wyświetlacz liczby
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Przycisk Plus
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { repeat(step) { onPlus() } },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Więcej", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = suffix,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.width(50.dp)
        )
    }
}

@Preview(showBackground = true, name = "Krok 2")
@Composable
fun AddHabitStep2DarkPreview() {
    CheckTheme(darkTheme = true,
        accent = Mint
    ) {
        AddHabitStep2(
            onNextClick = { _, _, _, _ ,_ -> },
            onBackClick = { }
        )
    }
}