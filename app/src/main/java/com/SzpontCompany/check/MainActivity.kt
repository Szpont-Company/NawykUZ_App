package com.SzpontCompany.check

import android.R.attr.label
import android.graphics.Color
import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.ui.auth.AuthViewModel
import com.SzpontCompany.check.ui.auth.LoginScreen
import com.SzpontCompany.check.ui.main.MainScreen
import com.SzpontCompany.check.ui.profile.ProfileScreen
import com.SzpontCompany.check.ui.settings.SettingsScreen
import com.SzpontCompany.check.ui.theme.Crimson
import com.SzpontCompany.check.ui.theme.Mint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.SzpontCompany.check.ui.main.BottomTab
import com.SzpontCompany.check.ui.main.CheckBottomNavigationBar
import com.SzpontCompany.check.ui.settings.SettingsNavHost
import com.SzpontCompany.check.ui.rewards.RewardsScreen

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

            var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

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
                ) { targetScreen ->
                    when (targetScreen) {
                        AppScreen.SPLASH -> {
                            AnimatedSplashScreen(
                                onSplashFinished = {
                                    currentScreen = if (authViewModel.isLoggedIn) AppScreen.DASHBOARD else AppScreen.LOGIN
                                }
                            )
                        }

                        AppScreen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { currentScreen = AppScreen.DASHBOARD }
                            )
                        }

                        AppScreen.DASHBOARD -> {
                            RootNavigationGraph()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RootNavigationGraph() {
    val navController = rememberNavController()

    var currentTab by remember { mutableStateOf<BottomTab?>(BottomTab.TODAY) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        if (currentRoute != "main") {
            currentTab = null
        } else if (currentTab == null) {
            currentTab = BottomTab.TODAY
        }
    }

    Scaffold(
        bottomBar = {
            CheckBottomNavigationBar(
                currentTab = currentTab,
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
                    onProfileClick = { navController.navigate("profile") }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate("settings") },
                    onRewardsClick = { navController.navigate("rewards") }
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