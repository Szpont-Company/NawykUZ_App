package com.SzpontCompany.check.ui.user

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.auth.AuthViewModel
import com.SzpontCompany.check.ui.auth.LogInHeader
import kotlinx.coroutines.launch

/**
 * Enum reprezentujący kroki procesu onboardingu.
 */
enum class OnboardingStep {
    /**
     * Krok 1: Ustawienie nicku użytkownika
     */
    NICKNAME,
    /**
     * Krok 2: Ustawienie celu dzienny (liczba kroków)
     */
    FIRST_HABIT
}

/**
 * Ekran onboardingu dla nowych użytkowników.
 *
 * Przeprowadza użytkownika przez proces konfiguracji po pierwszym zalogowaniu:
 * 1. Wybór unikalnego nicku (3-20 znaków, tylko litery, cyfry, podkreślnik)
 * 2. Ustawienie celu dziennego dla liczenia kroków
 *
 * Walidacja:
 * - Nick musi spełniać format regex: ^[a-zA-Z0-9_]{3,20}$
 * - Nick nie może być już zajęty w bazie danych
 * - Cel musi być liczbą dodatnią
 *
 * @param onOnboardingComplete Callback wywoływany po ukończeniu onboardingu
 * @param authViewModel ViewModel do zarządzania autentykacją i danymi użytkownika
 *
 * @since 1.0
 */
@Composable
fun OnboardingScreen(
    onOnboardingComplete: (dailySteps: Int) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    BackHandler(enabled = true) {/* Do nothing to disable back navigation*/}
    var nickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val isValid = nickname.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))
    val context = LocalContext.current
    val nicknameTakenMsg = stringResource(R.string.error_nicknameTaken)
    val genericErrorMsg = stringResource(R.string.unknown_error_nickname)

    var currentStep by remember {mutableStateOf(OnboardingStep.NICKNAME) }
    var dailySteps by remember {mutableStateOf("8000")}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        LogInHeader(
           accent = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "onboarding_animation"
        ) { step ->
            when (step) {
                OnboardingStep.NICKNAME -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.nickname_hello),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.nickname_hint1),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = {
                                if (it.length <= 20) {
                                    nickname = it
                                    errorMsg = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.nickname_placeholder)) },
                            prefix = { Text("@", color = MaterialTheme.colorScheme.primary) },
                            suffix = {
                                Text(
                                    "${nickname.length}/20",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            },
                            supportingText = {
                                when {
                                    errorMsg != null -> Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                                    nickname.isNotEmpty() && !isValid -> Text(
                                        if (nickname.length < 3) stringResource(R.string.nickname_min_characters)
                                        else stringResource(R.string.nickname_character_hint),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    isValid -> Text(stringResource(R.string.nickname_praise), color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            isError = nickname.isNotEmpty() && !isValid,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    val result = authViewModel.saveNickname(nickname)
                                    if (result.isSuccess) {
                                        currentStep = OnboardingStep.FIRST_HABIT
                                    } else {
                                        val msg = result.exceptionOrNull()?.message
                                        if (msg == "Nickname already taken") {
                                            errorMsg = nicknameTakenMsg
                                        } else {
                                            Toast.makeText(context, genericErrorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    isLoading = false
                                }
                            },
                            enabled = isValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(stringResource(R.string.nickname_ready), fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.nickname_change_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                OnboardingStep.FIRST_HABIT -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.onboarding_first_habit), // Zastąp to docelowo stringResource()
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.onboarding_hero), // Zastąp to docelowo stringResource()
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))

                        OutlinedTextField(
                            value = dailySteps,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 6) {
                                    dailySteps = newValue
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            suffix = {
                                Text(
                                    "kroków",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    val stepsInt = dailySteps.toIntOrNull() ?: 8000

                                    val result = authViewModel.saveFirstHabit(stepsInt, context)

                                    if (result.isSuccess) {
                                        onOnboardingComplete(stepsInt)
                                    } else {
                                        Toast.makeText(context, "Nie udało się zapisać nawyku", Toast.LENGTH_SHORT).show()
                                    }
                                    isLoading = false
                                }
                            },
                            enabled = dailySteps.isNotEmpty() && (dailySteps.toIntOrNull() ?: 0) > 0 && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(stringResource(R.string.onboarding_begin), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}