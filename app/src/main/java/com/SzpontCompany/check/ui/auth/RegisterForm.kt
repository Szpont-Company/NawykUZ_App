package com.SzpontCompany.check.ui.auth

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.SzpontCompany.check.R

enum class PwdStrength {
    EMPTY, WEAK, MEDIUM , STRONG
}

fun evaluatePwdStrength(pwd: String): PwdStrength {
    if(pwd.isEmpty()) return PwdStrength.EMPTY

    var strengthScore = 0
    if(pwd.length >= 8) strengthScore++
    if(pwd.any {it.isUpperCase()}) strengthScore++
    if(pwd.any {it.isDigit()}) strengthScore++
    if(pwd.any { !it.isLetterOrDigit() }) strengthScore++

    return when {
        strengthScore <= 1 -> PwdStrength.WEAK
        strengthScore <= 3 -> PwdStrength.MEDIUM
        else -> PwdStrength.STRONG
    }
}

@Composable
fun PasswordStrengthIndicator(password: String, modifier: Modifier = Modifier) {
    val strength = evaluatePwdStrength(password)

    val activeColor = when (strength) {
        PwdStrength.EMPTY -> Color.Transparent
        PwdStrength.WEAK -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        PwdStrength.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        PwdStrength.STRONG -> MaterialTheme.colorScheme.primary
    }

    val activePanels = when (strength) {
        PwdStrength.EMPTY -> 0
        PwdStrength.WEAK -> 2
        PwdStrength.MEDIUM -> 3
        PwdStrength.STRONG -> 4
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(4) {index ->
            val isActive = index < activePanels

            val color by animateColorAsState(
                targetValue = if(isActive) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(durationMillis = 300),
                label = "panel_${index}_color"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))

            )
        }
    }
}


@Composable
fun RegisterForm(
    name: String,
    email: String,
    password: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleRegisterClick: () -> Unit,
    captchaVerified: Boolean,
    onCaptchaClick: () -> Unit,
    validateCredentials: () -> Boolean,
    emailErrorMessage: String?,
    isLoading: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var isNameError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    Text(
        text = stringResource(R.string.register_name),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = name,
        onValueChange = {onNameChange(it)
            if(it.isNotBlank()) isNameError = false},
        placeholder = {Text("John Black")},
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = if (isNameError) Color.Red else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isNameError) Color.Red else Color.Transparent
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "E-MAIL",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = email,
        onValueChange = {
            onEmailChange(it)
            if(it.isNotBlank()) isEmailError = false},
        placeholder = {Text(stringResource(R.string.mail_hint))},
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = if (isEmailError) Color.Red else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isEmailError) Color.Red else Color.Transparent
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
    )
    emailErrorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 2.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.password),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = password,
        onValueChange = {
            onPasswordChange(it)
            if(it.isNotBlank()) isPasswordError = false
                        },
        placeholder = {Text(stringResource(R.string.password_rule))},
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = if (isPasswordError) Color.Red else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isPasswordError) Color.Red else Color.Transparent
        ),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = {visible = !visible}) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(2.dp))
    PasswordStrengthIndicator(
        password = password,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top=6.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    CaptchaBox(
        verified = captchaVerified,
        onCaptchaClick = { onCaptchaClick() }
    )
    Button(
        onClick = {
            if(name.isBlank()) isNameError = true
            if(email.isBlank()) isEmailError = true
            if(password.isBlank()) isPasswordError = true
            if(captchaVerified && validateCredentials()) {
                onRegisterClick()
            } else {
                Toast.makeText(context, "Please verify your input", Toast.LENGTH_SHORT).show()
            }

        },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(22),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        if(!isLoading) {
            Text(
                text = stringResource(R.string.login),
                style = MaterialTheme.typography.bodyLarge
            )} else {
                CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.continue_with),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { onGoogleRegisterClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(22),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.google_logo),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.login_google),
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

        TermsText(
            onTosClick = { /*TODO*/ },
            onPrivacyClick = { /*TODO*/ },
            type = TermsType.SIGN_UP
        )
}