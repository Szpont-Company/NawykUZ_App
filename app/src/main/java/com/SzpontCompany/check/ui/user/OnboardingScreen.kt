package com.SzpontCompany.check.ui.user

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.auth.AuthViewModel
import com.SzpontCompany.check.ui.auth.LogInHeader
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNicknameSaved: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    BackHandler(enabled = true) {// Do nothing to disable back navigation
         }
    var nickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val isValid = nickname.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val nicknameTakenMsg = stringResource(R.string.error_nicknameTaken)
    val genericErrorMsg = stringResource(R.string.unknown_error_nickname)

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
                        if (nickname.length < 3) stringResource(R.string.nickname_min_characters) else stringResource(
                            R.string.nickname_character_hint
                        ),
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
                        onNicknameSaved()
                    } else {
                        val msg = result.exceptionOrNull()?.message
                        if(msg == "Nickname already taken") {
                            errorMsg = nicknameTakenMsg
                        } else {
                            Toast.makeText(context,
                                genericErrorMsg, Toast.LENGTH_LONG).show()
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
