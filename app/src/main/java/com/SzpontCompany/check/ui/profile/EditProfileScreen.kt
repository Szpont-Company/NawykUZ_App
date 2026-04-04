package com.SzpontCompany.check.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.components.CheckBackButton
import com.SzpontCompany.check.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("Marek Kowalski") }
    var nickname by remember { mutableStateOf("marekk") }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedAvatar by remember { mutableStateOf("") }
    var selectedBgColor by remember { mutableStateOf(Amber) }
    var hasChanges by remember { mutableStateOf(false) }

    var showEmailSheet by remember { mutableStateOf(false) }
    var showPasswordSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val nameParts = fullName.trim().split("\\s+".toRegex())
    val isFullNameValid = fullName.isNotBlank() && nameParts.size >= 2
    val isNicknameValid = nickname.isNotBlank() && nickname.matches(Regex("^[a-zA-Z0-9_.]+$"))
    val canSave = hasChanges && isFullNameValid && isNicknameValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        EditProfileTopBar(
            onBackClick = onBackClick,
            onSaveClick = { /* TODO: Save changes */ },
            isSaveEnabled = canSave
        )

        Spacer(modifier = Modifier.height(24.dp))

        LivePreviewSection(
            fullName = fullName,
            nickname = nickname,
            avatarEmoji = selectedAvatar,
            bgColor = selectedBgColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        AvatarCustomizationSection(
            fullName = fullName,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            selectedAvatar = selectedAvatar,
            onAvatarSelected = {
                selectedAvatar = it
                hasChanges = true
            },
            selectedBgColor = selectedBgColor,
            onBgColorSelected = {
                selectedBgColor = it
                hasChanges = true
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileFormSection(
            fullName = fullName,
            onFullNameChange = { newValue ->
                if (newValue.matches(Regex("^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\s\\-]*$")) && newValue.count { it == '-' } <= 1) {
                    fullName = newValue
                    hasChanges = true
                }
            },
            nickname = nickname,
            onNicknameChange = { nickname = it; hasChanges = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SecuritySection(
            onChangeEmail = { showEmailSheet = true },
            onChangePassword = { showPasswordSheet = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        BottomActions(
            onSaveClick = { /* TODO: Save */ },
            isSaveEnabled = canSave
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showEmailSheet) {
        ChangeEmailSheet(
            sheetState = sheetState,
            onDismiss = { showEmailSheet = false },
            onSave = { newEmail ->
                /* TODO: Handle email change */
                showEmailSheet = false
            }
        )
    }

    if (showPasswordSheet) {
        ChangePasswordSheet(
            sheetState = sheetState,
            onDismiss = { showPasswordSheet = false },
            onSave = { current, newPwd ->
                /* TODO: Handle password change */
                showPasswordSheet = false
            }
        )
    }
}

@Composable
fun EditProfileTopBar(onBackClick: () -> Unit, onSaveClick: () -> Unit, isSaveEnabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBackButton(onClick = onBackClick)

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Edytuj profil",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        TextButton(
            onClick = onSaveClick,
            enabled = isSaveEnabled
        ) {
            Text(
                text = "Zapisz",
                fontWeight = FontWeight.Bold,
                color = if (isSaveEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LivePreviewSection(
    fullName: String,
    nickname: String,
    avatarEmoji: String,
    bgColor: Color
) {
    val initials = fullName.trim().split("\\s+".toRegex()).mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    val displayAvatar = if (avatarEmoji.isEmpty()) initials else avatarEmoji

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayAvatar,
                        fontSize = if (avatarEmoji.isEmpty()) 34.sp else 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (avatarEmoji.isEmpty()) Color.White else Color.Unspecified
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = 4.dp)
                        .background(Color(0xFFBA7517), RoundedCornerShape(12.dp))
                        .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Lvl 8", color = MaterialTheme.colorScheme.surface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (fullName.isBlank()) "Brak danych" else fullName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (nickname.isBlank()) "@nick" else "@$nickname",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "🔥 21 Dni", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFBA7517).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBA7517), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("🏆 Top 14", color = Color(0xFFBA7517), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun AvatarCustomizationSection(
    fullName: String,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    selectedAvatar: String,
    onAvatarSelected: (String) -> Unit,
    selectedBgColor: Color,
    onBgColorSelected: (Color) -> Unit
) {
    Column {
        val tabs = listOf("Avatar", "Tło", "Ramka")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(index) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> AvatarList(fullName, selectedAvatar, onAvatarSelected)
            1 -> BackgroundColorList(selectedBgColor, onBgColorSelected)
            2 -> EmptyComingSoon(text = "Rzadkie ramki wkrótce!")
        }
    }
}

@Composable
fun AvatarList(fullName: String, selectedAvatar: String, onAvatarSelected: (String) -> Unit) {
    val avatars = listOf("", "👨", "👩", "🐱", "🐶", "🦊", "🦁", "🐼", "🤖", "👽", "👻")
    val initials = fullName.trim().split("\\s+".toRegex()).mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(avatars) { emoji ->
            val isSelected = emoji == selectedAvatar
            val displayAvatar = if (emoji.isEmpty()) initials else emoji

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAvatarSelected(emoji) }
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        2.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayAvatar,
                    fontSize = if (emoji.isEmpty()) 24.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (emoji.isEmpty()) MaterialTheme.colorScheme.onBackground else Color.Unspecified
                )
            }
        }
        item {
            StoreActionTile(onClick = { /* TODO: Otwórz sklep awatarów */ })
        }
    }
}

@Composable
fun BackgroundColorList(selectedBgColor: Color, onBgColorSelected: (Color) -> Unit) {
    val colors = listOf(Mint, Indigo, Coral, Sky, Rose, Cactus, Amber, Crimson)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(colors) { color ->
            val isSelected = color == selectedBgColor
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { onBgColorSelected(color) }
                    .background(color)
                    .border(
                        3.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {}
        }
    }
}

@Composable
fun StoreActionTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Sklep",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EmptyComingSoon(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

@Composable
fun ProfileFormSection(
    fullName: String, onFullNameChange: (String) -> Unit,
    nickname: String, onNicknameChange: (String) -> Unit
) {
    val nameParts = fullName.trim().split("\\s+".toRegex())
    val isFullNameError = fullName.isBlank() || nameParts.size < 2
    val fullNameErrorMsg = if (fullName.isBlank()) "Imię i nazwisko nie może być puste" else if (nameParts.size < 2) "Podaj również nazwisko" else null

    val isNickError = nickname.isBlank() || !nickname.matches(Regex("^[a-zA-Z0-9_.]+$"))
    val nickErrorMsg = if (nickname.isBlank()) "Nick nie może być pusty" else if (isNickError) "Dozwolone litery, cyfry, '_' oraz '.'" else null

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CustomTextField(
            modifier = Modifier.fillMaxWidth(),
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Imię i Nazwisko",
            isError = isFullNameError,
            errorMessage = fullNameErrorMsg
        )
        CustomTextField(
            modifier = Modifier.fillMaxWidth(),
            value = nickname,
            onValueChange = onNicknameChange,
            label = "Nazwa użytkownika (Nick)",
            prefix = "@",
            isError = isNickError,
            errorMessage = nickErrorMsg
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = prefix?.let {
                { Text(text = it, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorLeadingIconColor = MaterialTheme.colorScheme.error,
                errorTrailingIconColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                errorTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeEmailSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isEmailValid = newEmail.contains("@") && newEmail.contains(".")
    val canSave = isEmailValid && currentPassword.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Zmiana e-maila",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                "Twój obecny adres to marek.kowalski@gmail.com. Podaj nowy adres oraz hasło do konta, aby potwierdzić zmianę.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("Nowy adres e-mail") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Hasło do konta") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onSave(newEmail) },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Zaktualizuj e-mail", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isOldVisible by remember { mutableStateOf(false) }
    var isNewVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    val hasMinLength = newPassword.length >= 8
    val hasUpperChar = newPassword.any { it.isUpperCase() }
    val hasDigit = newPassword.any { it.isDigit() }
    val hasSpecialChar = newPassword.any { !it.isLetterOrDigit() }

    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotBlank()
    val isStrong = hasMinLength && hasUpperChar && hasDigit && hasSpecialChar

    val canSave = oldPassword.isNotBlank() && passwordsMatch && isStrong

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Zmiana hasła",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Aktualne hasło") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isOldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                trailingIcon = {
                    IconButton(onClick = { isOldVisible = !isOldVisible }) {
                        Icon(imageVector = if (isOldVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nowe hasło") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isNewVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                trailingIcon = {
                    IconButton(onClick = { isNewVisible = !isNewVisible }) {
                        Icon(imageVector = if (isNewVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                singleLine = true
            )

            if (newPassword.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val strengthParts = listOf(hasMinLength, hasUpperChar, hasDigit, hasSpecialChar)
                    val activeCount = strengthParts.count { it }
                    val barColor = when (activeCount) {
                        1 -> Color.Red
                        2 -> Color(0xFFFF9800)
                        3 -> Color(0xFFFFC107)
                        4 -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    for (i in 0 until 4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i < activeCount) barColor else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                Text(
                    text = "Hasło musi mieć co najmniej 8 znaków, w tym dużą literę, cyfrę i znak specjalny.",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isStrong) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Powtórz nowe hasło") },
                isError = confirmPassword.isNotBlank() && !passwordsMatch,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                trailingIcon = {
                    IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                        Icon(imageVector = if (isConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                supportingText = {
                    if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Text("Hasła nie są identyczne", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSave(oldPassword, newPassword) },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Zmień hasło", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SecuritySection(onChangeEmail: () -> Unit, onChangePassword: () -> Unit) {
    Column {
        Text(
            text = "Konto i bezpieczeństwo",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
        ) {
            SecurityItem(
                icon = Icons.Default.Mail,
                title = "Zmień e-mail",
                onClick = onChangeEmail
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SecurityItem(
                icon = Icons.Outlined.Lock,
                title = "Zmień hasło",
                onClick = onChangePassword
            )
        }
    }
}

@Composable
fun SecurityItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BottomActions(onSaveClick: () -> Unit, isSaveEnabled: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onSaveClick,
            enabled = isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                "Zapisz zmiany",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSaveEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        EditProfileScreen()
    }
}
