package com.SzpontCompany.check.ui.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SettingsNavHost(
    navController: NavHostController = rememberNavController(),
    onExitSettings: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "settings_main"
    ) {
        // 1. Main settings screen
        composable("settings_main") {
            SettingsScreen(
                onBackClick = onExitSettings,
                onStepGoalClick = { navController.navigate("settings_step_goal") },
                onPrivacyClick = { navController.navigate("settings_privacy") },
                onNotificationsClick = { navController.navigate("settings_notifications") },
                onDeleteAccountConfirmed = {
                    navController.navigate("login_route") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 2. Daily Step Goal Screen
        composable("settings_step_goal") {
            StepGoalScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. Privacy Screen
        composable("settings_privacy") {
            PrivacyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 4. Notifications Screen
        composable("settings_notifications") {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}