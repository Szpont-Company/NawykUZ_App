package com.SzpontCompany.check

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.auth.AnimatedSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.ui.auth.AuthViewModel
import com.SzpontCompany.check.ui.auth.LoginScreen
import com.SzpontCompany.check.ui.auth.RegisterSuccessScreen
import com.SzpontCompany.check.ui.auth.ResetPasswordScreen
import com.SzpontCompany.check.ui.main.MainScreen
import com.SzpontCompany.check.ui.theme.Mint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.SzpontCompany.check.ui.main.BottomTab
import com.SzpontCompany.check.ui.main.CheckBottomNavigationBar
import com.SzpontCompany.check.ui.profile.ProfileScreen
import com.SzpontCompany.check.ui.settings.SettingsNavHost
import com.SzpontCompany.check.ui.rewards.RewardsScreen

enum class AppScreen { SPLASH, LOGIN, DASHBOARD, REGISTER_SUCCESS, RESET_PASSWORD }

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

            var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

            CheckTheme(darkTheme = darkTheme, accent = Mint) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        when (targetState) {
                            AppScreen.DASHBOARD ->
                                (slideInHorizontally { it } + fadeIn(tween(400))) togetherWith
                                        (slideOutHorizontally { -it } + fadeOut(tween(300)))

                            AppScreen.LOGIN -> {
                                if (initialState == AppScreen.REGISTER_SUCCESS || initialState == AppScreen.RESET_PASSWORD) {
                                    (slideInHorizontally { -it } + fadeIn(tween(400))) togetherWith
                                            (slideOutHorizontally { it } + fadeOut(tween(300)))
                                } else {
                                    fadeIn(tween(500)) togetherWith fadeOut(tween(300))
                                }
                            }

                            AppScreen.SPLASH ->
                                fadeIn() togetherWith fadeOut()

                            AppScreen.REGISTER_SUCCESS, AppScreen.RESET_PASSWORD ->
                                (slideInHorizontally { it } + fadeIn(tween(400))) togetherWith
                                        (slideOutHorizontally { -it } + fadeOut(tween(300)))

                            else -> fadeIn() togetherWith fadeOut()
                        }
                    },
                    label = "app_screen_transition"
                ) { targetScreen ->
                    when (targetScreen) {
                        AppScreen.SPLASH -> {
                            AnimatedSplashScreen(
                                onSplashFinished = {
                                    currentScreen = if (authViewModel.isLoggedIn) AppScreen.DASHBOARD else AppScreen.LOGIN
                                }
                            )
                        }

                        AppScreen.LOGIN -> LoginScreen(
                            onLoginSuccess = { currentScreen = AppScreen.DASHBOARD },
                            onRegisterSuccess = { currentScreen = AppScreen.REGISTER_SUCCESS },
                            onForgotPasswordClick = { currentScreen = AppScreen.RESET_PASSWORD },
                        )

                        AppScreen.DASHBOARD -> {
                            RootNavigationGraph(
                                onLogout = { currentScreen = AppScreen.LOGIN }
                            )
                        }

                        AppScreen.REGISTER_SUCCESS -> RegisterSuccessScreen(
                            onBack = { currentScreen = AppScreen.LOGIN },
                            onSuccess = { currentScreen = AppScreen.LOGIN },
                            accent = MaterialTheme.colorScheme.primary
                        )

                        AppScreen.RESET_PASSWORD -> ResetPasswordScreen(
                            onBack = { currentScreen = AppScreen.LOGIN },
                            accent = MaterialTheme.colorScheme.primary,
                            onPasswordReset = { authViewModel.resetPassword { result ->
                                if (result.isSuccess) {
                                    currentScreen = AppScreen.LOGIN
                                } else {
                                    Log.e("ResetPassword", "Error resetting password: ${result.exceptionOrNull()?.message}")
                                }
                            } },
                            email = authViewModel.email,
                            onEmailChange = { authViewModel.onEmailChange(it)}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RootNavigationGraph(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    var currentTab by remember { mutableStateOf<BottomTab?>(BottomTab.TODAY) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    Scaffold(
        bottomBar = {
            CheckBottomNavigationBar(
                currentTab = if (currentRoute == "main") currentTab else null,
                onTabSelected = { newTab ->
                    currentTab = newTab
                    navController.popBackStack("main", inclusive = false)
                },
                onAddClick = { /* TODO: Otwórz okno dodawania */ }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(paddingValues)
        ) {

            composable("main") {
                MainScreen(
                    currentTab = currentTab ?: BottomTab.TODAY,
                    onProfileClick = {
                        navController.navigate("profile"){
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onBackClick = {
                        navController.popBackStack("main", inclusive = false)
                    },
                    onSettingsClick = {
                        if (navController.currentDestination?.route == "profile") {
                            navController.navigate("settings")
                        }
                    },
                    onRewardsClick = {
                        if (navController.currentDestination?.route == "profile") {
                            navController.navigate("rewards")
                        }
                    },
                    onLogoutClick = {
                        authViewModel.signOut()
                        onLogout()
                    }
                )
            }

            composable("rewards") {
                RewardsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsNavHost(
                    onExitSettings = { navController.popBackStack() }
                )

            }
        }
    }
}