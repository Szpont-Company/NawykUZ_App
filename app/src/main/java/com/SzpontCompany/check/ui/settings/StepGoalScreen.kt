package com.SzpontCompany.check.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import kotlin.math.roundToInt
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.components.CheckBackButton

fun formatGoalNumber(goal: Int): String {
    return String.format("%,d", goal).replace(',', ' ')
}

data class PopularGoal(val value: Int, val label: String)

@Composable
fun StepGoalScreen(
    onBackClick: () -> Unit = {},
    onSaveGoal: (Int) -> Unit = {}
) {
    val viewModel: StepGoalViewModel = viewModel()

    // Obserwacje ze zaktualizowanego ViewModelu
    val habits by viewModel.userHabitsFlow.collectAsState(initial = emptyList())
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var sliderPosition by remember { mutableFloatStateOf(8000f) }
    var isInitialized by remember { mutableStateOf(false) }

    // Wczytanie aktualnego celu od użytkownika przy starcie widoku
    LaunchedEffect(habits) {
        if (!isInitialized && habits.isNotEmpty()) {
            val stepHabit = habits.find { it.isStepsHabit == true || it.id == "steps" }
            if (stepHabit != null) {
                // Ustawiamy odczytaną wartość na suwak
                sliderPosition = stepHabit.dailyGoal.toFloat().coerceIn(1000f, 20000f)
            }
            isInitialized = true
        }
    }

    // Automatyczny powrót na poprzedni ekran TYLKO RAZ gdy zapis się powiedzie
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onBackClick()
        }
    }

    val currentGoal = (sliderPosition / 100).roundToInt() * 100
    val isSaving = viewModel.isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CheckBackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.step_goal_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.step_goal_section_daily),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { sliderPosition = (sliderPosition - 500).coerceAtLeast(1000f) },
                        contentAlignment = Alignment.Center
                    ) { Text("−", fontSize = 28.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground) }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = formatGoalNumber(currentGoal), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(text = stringResource(R.string.step_goal_steps_suffix), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { sliderPosition = (sliderPosition + 500).coerceAtMost(20000f) },
                        contentAlignment = Alignment.Center
                    ) { Text("+", fontSize = 28.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground) }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 1000f..20000f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.step_goal_section_popular),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val popularGoals = listOf(
            PopularGoal(5000, stringResource(R.string.step_goal_popular_active)),
            PopularGoal(8000, stringResource(R.string.step_goal_popular_who)),
            PopularGoal(10000, stringResource(R.string.step_goal_popular_classic)),
            PopularGoal(15000, stringResource(R.string.step_goal_popular_sport))
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PopularGoalItem(goal = popularGoals[0], isSelected = currentGoal == popularGoals[0].value, modifier = Modifier.weight(1f), onClick = { sliderPosition = popularGoals[0].value.toFloat() })
                PopularGoalItem(goal = popularGoals[1], isSelected = currentGoal == popularGoals[1].value, modifier = Modifier.weight(1f), onClick = { sliderPosition = popularGoals[1].value.toFloat() })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PopularGoalItem(goal = popularGoals[2], isSelected = currentGoal == popularGoals[2].value, modifier = Modifier.weight(1f), onClick = { sliderPosition = popularGoals[2].value.toFloat() })
                PopularGoalItem(goal = popularGoals[3], isSelected = currentGoal == popularGoals[3].value, modifier = Modifier.weight(1f), onClick = { sliderPosition = popularGoals[3].value.toFloat() })
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                viewModel.saveStepGoal(currentGoal)
                onSaveGoal(currentGoal)
            },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.step_goal_save_button), color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PopularGoalItem(goal: PopularGoal, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val borderColor = if (isSelected) primaryColor else Color.Transparent
    val textColor = if (isSelected) primaryColor else MaterialTheme.colorScheme.onBackground
    val labelColor = if (isSelected) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = formatGoalNumber(goal.value), color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = goal.label, color = labelColor, fontWeight = FontWeight.Normal, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StepGoalScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        StepGoalScreen(onBackClick = {})
    }
}