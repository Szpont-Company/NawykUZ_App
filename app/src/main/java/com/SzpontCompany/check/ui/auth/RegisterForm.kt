package com.SzpontCompany.check.ui.auth

import android.widget.Space
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.SzpontCompany.check.R

@Composable
fun RegisterForm(
    name: String,
    email: String,
    password: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleRegisterClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    Text(
        text = stringResource(R.string.register_name),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = name,
        onValueChange = {onNameChange(it)},
        placeholder = {Text("John Black")},
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground
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
        onValueChange = {onEmailChange(it)},
        placeholder = {Text("twoj@email.com")},
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
    )
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
        onValueChange = {onPasswordChange(it)},
        placeholder = {Text(stringResource(R.string.passwordRule))},
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground
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
    // todo: wizualizacja sily hasla
    Spacer(modifier = Modifier.height(2.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24))
            .background(MaterialTheme.colorScheme.primary),
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22))
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Captcha",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { },
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
        Text(
            text = stringResource(R.string.create_account),
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.continueWith),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { },
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
            text = stringResource(R.string.LogInGoogle),
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.register_notice1),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.login_privacy_2),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { }
        )
        Text(
            text = stringResource(R.string.login_privacy_3),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.login_privacy_4),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { }
        )
    }
}