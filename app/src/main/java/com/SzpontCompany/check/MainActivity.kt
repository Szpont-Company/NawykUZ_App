package com.SzpontCompany.check

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.auth.AnimatedSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation.navArgument
import com.SzpontCompany.check.ui.main.BottomTab
import com.SzpontCompany.check.ui.main.CheckBottomNavigationBar
import com.SzpontCompany.check.ui.profile.EditProfileScreen
import com.SzpontCompany.check.ui.profile.ProfileScreen
import com.SzpontCompany.check.ui.settings.SettingsNavHost
import com.SzpontCompany.check.ui.rewards.RewardsScreen
import com.SzpontCompany.check.ui.settings.SettingsViewModel
import com.SzpontCompany.check.ui.settings.SettingsViewModelFactory
import com.SzpontCompany.check.ui.theme.Amber
import com.SzpontCompany.check.ui.theme.Cactus
import com.SzpontCompany.check.ui.theme.Coral
import com.SzpontCompany.check.ui.theme.Crimson
import com.SzpontCompany.check.ui.theme.Indigo
import com.SzpontCompany.check.ui.theme.Rose
import com.SzpontCompany.check.ui.theme.Sky
import com.SzpontCompany.check.ui.profile.FriendProfileScreen
import com.SzpontCompany.check.ui.community.ChatScreen
import androidx.compose.runtime.rememberCoroutineScope
import com.SzpontCompany.check.ui.user.OnboardingScreen
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.SzpontCompany.check.ui.community.BattleDetailScreen
import com.SzpontCompany.check.ui.community.CommunityViewModel
import com.SzpontCompany.check.ui.community.components.NotificationsViewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore

enum class AppScreen { SPLASH, LOGIN, DASHBOARD, REGISTER_SUCCESS, RESET_PASSWORD, SET_NICKNAME }

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            Log.e("MainActivity", "Initializing Mobile Ads SDK")
            MobileAds.initialize(this@MainActivity) {}
        }

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val context = LocalContext.current

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(context.applicationContext)
            )

            val themeState by settingsViewModel.themeState.collectAsState()
            val accentColorState by settingsViewModel.accentColorState.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeState) {
                "Ciemny" -> true
                "Jasny" -> false
                else -> isSystemDark
            }

            val accentColor = when (accentColorState) {
                "Indigo" -> Indigo
                "Coral" -> Coral
                "Sky" -> Sky
                "Rose" -> Rose
                "Cactus" -> Cactus
                "Amber" -> Amber
                "Crimson" -> Crimson
                else -> Mint
            }

            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            val scope = rememberCoroutineScope()
            var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

            CheckTheme(darkTheme = darkTheme, accent = accentColor) {
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

                            AppScreen.SET_NICKNAME ->
                                (slideInHorizontally { it } + fadeIn(tween(400))) togetherWith
                                        (slideOutHorizontally { -it } + fadeOut(tween(300)))

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
                                    if (!authViewModel.isLoggedIn) {
                                        currentScreen = AppScreen.LOGIN
                                    } else {
                                        scope.launch {
                                            val uid = authViewModel.currentUser.value?.uid
                                            currentScreen =
                                                if (uid != null && authViewModel.isNicknameSet(uid)) {
                                                    AppScreen.DASHBOARD
                                                } else {
                                                    AppScreen.SET_NICKNAME
                                                }
                                        }
                                    }
                                }
                            )
                        }

                        AppScreen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    scope.launch {
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                                            ?: run {
                                                delay(300)
                                                FirebaseAuth.getInstance().currentUser?.uid
                                            }
                                            ?: return@launch

                                        currentScreen = if (authViewModel.isNicknameSet(uid)) {
                                            AppScreen.DASHBOARD
                                        } else {
                                            AppScreen.SET_NICKNAME
                                        }
                                    }
                                },
                                onRegisterSuccess = { currentScreen = AppScreen.REGISTER_SUCCESS },
                                onForgotPasswordClick = { currentScreen = AppScreen.RESET_PASSWORD }
                            )
                        }

                        AppScreen.SET_NICKNAME -> OnboardingScreen(
                            onNicknameSaved = { currentScreen = AppScreen.DASHBOARD },
                        )

                        AppScreen.DASHBOARD -> {
                            LaunchedEffect(Unit) {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser != null) {
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            Log.w(
                                                "FCM",
                                                "Fetching FCM registration token failed",
                                                task.exception
                                            )
                                            return@addOnCompleteListener
                                        }
                                        val token = task.result
                                        Log.d("FCM", "FCM Token: $token")

                                        FirebaseFirestore.getInstance().collection("users")
                                            .document(currentUser.uid)
                                            .update("fcmToken", token)
                                    }
                                }
                            }

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
                            onPasswordReset = {
                                authViewModel.resetPassword { result ->
                                    if (result.isSuccess) {
                                        currentScreen = AppScreen.LOGIN
                                    } else {
                                        Log.e(
                                            "ResetPassword",
                                            "Error resetting password: ${result.exceptionOrNull()?.message}"
                                        )
                                    }
                                }
                            },
                            email = authViewModel.email,
                            onEmailChange = { authViewModel.onEmailChange(it) }
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
    val communityViewModel: CommunityViewModel = viewModel()

    val notificationsViewModel: NotificationsViewModel = viewModel()

    var currentTab by remember { mutableStateOf<BottomTab?>(BottomTab.TODAY) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    Scaffold(
        bottomBar = {
            if (currentRoute == "main") {
                CheckBottomNavigationBar(
                    currentTab = currentTab ?: BottomTab.TODAY,
                    onTabSelected = { newTab ->
                        currentTab = newTab
                        navController.popBackStack("main", inclusive = false)
                    },
                    onAddClick = { /* TODO: Otwórz okno dodawania */ },
                    notificationsViewModel = notificationsViewModel

                )
            }
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
                    onTabSelected = { newTab -> currentTab = newTab },
                    onProfileClick = {
                        navController.navigate("profile") {
                            launchSingleTop = true
                        }
                    },
                    onOptionsClick = {
                        navController.navigate("settings") {
                            launchSingleTop = true
                        }
                    },
                    onFriendProfileClick = {
                        navController.navigate("friend_profile") {
                            launchSingleTop = true
                        }
                    },
                    onMessageClick = { friend ->
                        val encodedName = java.net.URLEncoder.encode(friend.name, "UTF-8")
                        val encodedEmoji = java.net.URLEncoder.encode(
                            friend.avatarEmoji.ifEmpty { friend.initials },
                            "UTF-8"
                        )
                        val route =
                            "chat_screen?friendId=${friend.uid}&friendName=$encodedName&friendEmoji=$encodedEmoji&friendBgColor=${friend.bgColor}"
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                    onBattleClick = { battle ->
                        navController.navigate("battle_detail/${battle.id}") {
                            launchSingleTop = true
                        }
                    },

                    notificationsViewModel = notificationsViewModel,

                    onNavigateToBattleDetail = { battleId ->
                        navController.navigate("battle_detail/$battleId") {
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
                    onEditProfileClick = {
                        if (navController.currentDestination?.route == "profile") {
                            navController.navigate("edit_profile")
                        }
                    },
                    onRewardsClick = {
                        if (navController.currentDestination?.route == "profile") {
                            navController.navigate("rewards")
                        }
                    },
                    onLogoutClick = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("fcmToken", com.google.firebase.firestore.FieldValue.delete())
                                .addOnCompleteListener {
                                    authViewModel.signOut()
                                    onLogout()
                                }
                        } else {
                            authViewModel.signOut()
                            onLogout()
                        }
                    }
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("friend_profile") {
                FriendProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onMessageClick = { navController.navigate("chat_screen") },
                    isInitiallyPrivate = false
                )
            }

            composable(
                route = "chat_screen?friendId={friendId}&friendName={friendName}&friendEmoji={friendEmoji}&friendBgColor={friendBgColor}",
                arguments = listOf(
                    navArgument("friendId") { defaultValue = "" },
                    navArgument("friendName") { defaultValue = "" },
                    navArgument("friendEmoji") { defaultValue = "" },
                    navArgument("friendBgColor") { defaultValue = "Mint" }
                )
            ) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
                val rawName = backStackEntry.arguments?.getString("friendName") ?: ""
                val rawEmoji = backStackEntry.arguments?.getString("friendEmoji") ?: ""

                val friendName = java.net.URLDecoder.decode(rawName, "UTF-8")
                val friendEmoji = java.net.URLDecoder.decode(rawEmoji, "UTF-8")
                val friendBgColor = backStackEntry.arguments?.getString("friendBgColor") ?: "Mint"

                ChatScreen(
                    friendId = friendId,
                    onBackClick = { navController.popBackStack() },
                    friendName = friendName,
                    friendEmoji = friendEmoji,
                    friendBgColor = friendBgColor
                )
            }

            composable("rewards") {
                RewardsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsNavHost(
                    onExitSettings = { navController.popBackStack() },
                    onLogout = onLogout
                )

            }

            composable(
                route = "battle_detail/{battleId}",
                arguments = listOf(navArgument("battleId") { defaultValue = "" })
            ) { backStackEntry ->
                val battleId = backStackEntry.arguments?.getString("battleId") ?: ""
                val battles by communityViewModel.battles.collectAsState()
                val battle = battles.find { it.id == battleId }
                val currentUserId = authViewModel.currentUser.value?.uid ?: ""

                if (battle != null) {
                    BattleDetailScreen(
                        battle = battle,
                        currentUserId = currentUserId,
                        onBackClick = { navController.popBackStack() },
                        onMarkDoneClick = {
                            communityViewModel.toggleBattleDone(battle, true)
                        },
                        onSurrenderClick = {
                            communityViewModel.surrenderBattle(battle)
                        }
                    )
                }
            }
        }
    }
}
