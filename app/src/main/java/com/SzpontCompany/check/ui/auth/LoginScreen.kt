package com.SzpontCompany.check.ui.auth

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.Mint
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.ui.theme.Amber
import com.SzpontCompany.check.ui.theme.Cactus
import com.SzpontCompany.check.ui.theme.Coral
import com.SzpontCompany.check.ui.theme.Crimson
import com.SzpontCompany.check.ui.theme.Indigo
import com.SzpontCompany.check.ui.theme.Rose
import com.SzpontCompany.check.ui.theme.Sky
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

fun getLogoForAccent(accent: Color): Int {
    return when (accent) {
        Mint -> R.drawable.logo_mint
        Indigo -> R.drawable.logo_indigo
        Coral -> R.drawable.logo_coral
        Sky -> R.drawable.logo_sky
        Rose -> R.drawable.logo_rose
        Cactus -> R.drawable.logo_cactus
        Amber -> R.drawable.logo_amber
        Crimson -> R.drawable.logo_crimson
        else -> R.drawable.logo_mint
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,

) {

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val name = viewModel.name
    val email = viewModel.email
    val password = viewModel.password

    val scope = rememberCoroutineScope()
    val accent = MaterialTheme.colorScheme.primary

    val recaptchaToken by viewModel.recaptcha.token.collectAsStateWithLifecycle()
    val captchaVerified = recaptchaToken != null

    var loginError by remember { mutableStateOf<String?>(null) }
    var registrationError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    fun onGoogleClick() {
        scope.launch {
            val res = viewModel.signInWithGoogle(context)
            res.onSuccess { user ->
                Log.d("Auth", "Zalogowano: ${user?.displayName}")
                onLoginSuccess()
            }.onFailure { error ->
                Log.e("Auth", "Błąd: ${error.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LogInHeader(
            accent = accent
        )

        val tabTitles = listOf(stringResource(R.string.login), stringResource((R.string.register)))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {

            BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                val tabWidth = maxWidth / 2

                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedTab,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "indicator_offset"
                )

                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }


            Row(modifier = Modifier.fillMaxWidth()) {
                tabTitles.forEachIndexed { index, title ->

                    val textColor by animateColorAsState(
                        targetValue = if (selectedTab == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "text_color"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24))
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = textColor,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = selectedTab,
            label = "auth_tab",
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                } else {
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut()
                    )
                }
            }
        ) { tab ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                when (tab) {
                    0 -> LogInForm(
                        email,
                        password,
                        onEmailChange = { viewModel.onEmailChange(it) },
                        onPasswordChange = { viewModel.onPasswordChange(it) },
                        onLogInClick = { scope.launch {
                            isLoggingIn = true
                            val res = viewModel.signInWithEmail(email.trim(), password)
                            isLoggingIn = false
                            res.onSuccess { onLoginSuccess() }
                            res.onFailure { error ->
                                Log.d("RegisterError", "message: '${error.message}' | class: ${error::class.simpleName}")
                                loginError  = when (error.message) {
                                    "Invalid_credentials" -> context.getString(R.string.login_incorrect_credentials)
                                    "Account_not_found" -> context.getString(R.string.login_notfound)
                                    else -> context.getString(R.string.register_unknown_error)
                                }
                            }
                        }},
                        onForgotPasswordClick = {
                            onForgotPasswordClick()
                        },
                        onGoogleLogInClick = {
                            onGoogleClick()
                        },
                        validateCredentials = {viewModel.validateCredentials()},
                        loginErrorMessage = loginError,
                        isLoading = isLoggingIn
                    )

                    1 -> RegisterForm(
                        name,
                        email,
                        password,
                        onNameChange = { viewModel.onNameChange(it) },
                        onEmailChange = { viewModel.onEmailChange(it) },
                        onPasswordChange = { viewModel.onPasswordChange(it) },
                        emailErrorMessage = registrationError,
                        onRegisterClick = { scope.launch {
                            isRegistering = true
                            val res = viewModel.signUpWithEmail(name, email, password)
                            isRegistering = false
                            res.onSuccess { 
                                Log.d("Auth", "Zarejestrowano: ${it.displayName}")
                                registrationError = null
                                onRegisterSuccess()
                            }
                            res.onFailure { error ->
                                Log.d("RegisterError", "message: '${error.message}' | class: ${error::class.simpleName}")
                                registrationError  = when (error.message) {
                                    "Email_already_in_use" -> context.getString(R.string.register_email_taken_error)
                                    else -> context.getString(R.string.register_unknown_error)
                                }
                            }
                        }},
                        onGoogleRegisterClick = {
                            onGoogleClick()
                        },
                        captchaVerified = captchaVerified,
                        onCaptchaClick = {
                            viewModel.recaptcha.execute()
                        },
                        validateCredentials = {viewModel.validateCredentials()},
                        isLoading = isRegistering
                    )
                }
            }
        }
    }
}