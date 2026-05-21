package com.SzpontCompany.check.ui.addhabit

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddHabitStep1(
    onNextClick: (name: String, icon: String, color: Color) -> Unit,
    onBackClick: () -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🎯") }
    var selectedColor by remember { mutableStateOf(Mint) }

    // --- ANIMACJA PASKA POSTĘPU ---
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )
    LaunchedEffect(Unit) {
        progress = 0.25f
    }

    val availableColors = listOf(Mint, Indigo, Coral, Sky, Rose, Cactus, Amber, Crimson)
    val availableIcons = listOf("🏃", "🚶", "📖", "💧", "🧘", "🏋️", "🚴", "🍎", "😴", "✍️", "🎸", "🧠", "💊", "🌿", "🏊", "🎯", "⚽", "💕")

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
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.habit_back_btn), tint = MaterialTheme.colorScheme.onBackground)
            }

            Text(
                text = stringResource(R.string.habit_new),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "1 / 4",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PASEK POSTĘPU ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(stringResource(R.string.habit_name_label))
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                placeholder = { Text(stringResource(R.string.habit_name_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.habit_icon_label))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableIcons.forEach { icon ->
                    val isSelected = icon == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                )
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 22.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.habit_color_label))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableColors) { color ->
                    val isSelected = color == selectedColor
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(stringResource(R.string.habit_preview_label))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedColor.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedIcon, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habitName.ifEmpty { stringResource(R.string.habit_unnamed) },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            // Przykładowy tekst podglądu – tu zostawiłem statyczny dla efektu wizualnego
                            text = stringResource(R.string.add_habit_preview),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "0%",
                        style = MaterialTheme.typography.titleMedium,
                        color = selectedColor
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.background,
                            RoundedCornerShape(2.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0f)
                            .fillMaxHeight()
                            .background(
                                selectedColor,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onNextClick(habitName, selectedIcon, selectedColor) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Text(text = stringResource(R.string.habit_next_btn), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Preview(showBackground = true, name = "Krok 1")
@Composable
fun AddHabitStep1Preview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        AddHabitStep1(
            onNextClick = { _, _, _ -> },
            onBackClick = { }
        )
    }
}