package com.SzpontCompany.check

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.auth.AnimatedSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.ui.auth.AuthViewModel
import com.SzpontCompany.check.ui.auth.LoginScreen
import com.SzpontCompany.check.ui.main.MainScreen
import com.SzpontCompany.check.ui.profile.ProfileScreen
import com.SzpontCompany.check.ui.settings.SettingsScreen
import com.SzpontCompany.check.ui.theme.Crimson
import com.SzpontCompany.check.ui.theme.Mint

enum class AppScreen { SPLASH, LOGIN, DASHBOARD }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val darkTheme = true
            val authViewModel: AuthViewModel = viewModel()

            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            var showSplash by remember { mutableStateOf(true) }
            var isLoggedIn by remember { mutableStateOf(authViewModel.isLoggedIn) }

            var currentScreen by remember {
                mutableStateOf(AppScreen.SPLASH)
            }

            CheckTheme(darkTheme = darkTheme, accent = Mint) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        when (targetState) {
                            AppScreen.DASHBOARD ->
                                (slideInHorizontally { it } + fadeIn(tween(400))) togetherWith
                                        (slideOutHorizontally { -it } + fadeOut(tween(300)))

                            AppScreen.LOGIN ->
                                fadeIn(tween(500)) togetherWith fadeOut(tween(300))

                            AppScreen.SPLASH ->
                                fadeIn() togetherWith fadeOut()
                        }
                    },
                    label = "app_screen_transition"
                ) { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> AnimatedSplashScreen(
                            onSplashFinished = { currentScreen = AppScreen.LOGIN }
                        )

                        AppScreen.LOGIN -> LoginScreen(
                            onLoginSuccess = { currentScreen = AppScreen.DASHBOARD }
                        )

                        AppScreen.DASHBOARD -> MainScreen()
                    }
                }
            }
        }
    }
}