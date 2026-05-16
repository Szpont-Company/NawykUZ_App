package com.SzpontCompany.check.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.SzpontCompany.check.ui.settings.StepGoalScreen

@Composable
fun SettingsNavHost(
    navController: NavHostController = rememberNavController(),
    onExitSettings: () -> Unit,
    onLogout: () -> Unit
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
                onDeleteAccountConfirmed = { onLogout() }
            )
        }

        // 2. Daily Step Goal Screen
        composable("settings_step_goal") {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(LocalContext.current.applicationContext)
            )
            val currentGoal by viewModel.stepGoalState.collectAsState()

            StepGoalScreen(
                initialGoal = currentGoal,
                onSaveGoal = { newGoal ->
                    viewModel.updateStepGoal(newGoal)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. Privacy Screen
        composable("settings_privacy") {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(LocalContext.current.applicationContext)
            )
            PrivacyScreen(
                viewModel = viewModel,
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