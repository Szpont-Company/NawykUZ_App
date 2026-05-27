package com.SzpontCompany.check.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.addhabit.AddHabitHost
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.data.social.Battle
import com.SzpontCompany.check.ui.dashboard.TodayScreen
import com.SzpontCompany.check.ui.stats.StatsScreen
import com.SzpontCompany.check.ui.map.MapScreen
import com.SzpontCompany.check.ui.community.CommunityScreen
import com.SzpontCompany.check.ui.community.components.NotificationsSheet
import com.SzpontCompany.check.ui.community.components.NotificationsViewModel

/**
 * Enum reprezentujący główne zakładki aplikacji.
 */
enum class BottomTab {
    /**
     * Zakładka dzisiejszych nawyków
     */
    TODAY,
    /**
     * Zakładka statystyk
     */
    STATS,
    /**
     * Zakładka z mapą przyjaciół
     */
    MAP,
    /**
     * Zakładka społeczności (bitwy, wyzwania)
     */
    COMMUNITY,
    /**
     * Przycisk dodania nowego nawyku
     */
    ADD
}

/**
 * Główny ekran aplikacji ze wszystkimi sekcjami.
 *
 * Wyświetla zawartość na podstawie wybranej zakładki:
 * - TODAY: Dzisiejsze nawyki i streaki
 * - STATS: Statystyki i wykresy postępu
 * - MAP: Mapa z lokalizacjami przyjaciół
 * - COMMUNITY: Społeczność, bitwy, wyzwania
 *
 * Zawiera górny pasek menu i dolny pasek nawigacji.
 * Obsługuje dialog dodawania nowych nawyków.
 *
 * @param currentTab Aktualnie wybrana zakładka
 * @param onTabSelected Callback wywoływany przy zmianie zakładki
 * @param onProfileClick Callback otwierający profil użytkownika
 * @param onOptionsClick Callback otwierający ustawienia
 * @param onNotificationsClick Callback otwierający powiadomienia
 * @param onFriendProfileClick Callback otwierający profil przyjaciela
 * @param onMessageClick Callback otwierający wiadomości z przyjacielem
 * @param onBattleClick Callback otwierający szczegóły bitwy
 * @param notificationsViewModel ViewModel do zarządzania powiadomieniami
 * @param onNavigateToBattleDetail Callback do nawigacji do szczegółów bitwy
 *
 * @since 1.0
 */
@Composable
fun MainScreen(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onProfileClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onFriendProfileClick: (String) -> Unit = {},
    onMessageClick: (Friend) -> Unit = {},
    onBattleClick: (Battle) -> Unit = {},
    notificationsViewModel: NotificationsViewModel = viewModel(),
    onNavigateToBattleDetail: (String) -> Unit = {}
) {
    var showNotifications by remember { mutableStateOf(false) }
    var showAddHabit by remember { mutableStateOf(false) }

    LaunchedEffect(currentTab) {
        if (currentTab == BottomTab.ADD) {
            showAddHabit = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentTab) {
            BottomTab.TODAY -> TodayScreen(
                onProfileClick = onProfileClick,
                onOptionsClick = onOptionsClick,
                onNotificationsClick = { showNotifications = true },
                notificationsViewModel = notificationsViewModel
            )

            BottomTab.STATS -> StatsScreen()
            BottomTab.MAP -> MapScreen()
            BottomTab.COMMUNITY -> CommunityScreen(
                onProfileClick = onProfileClick,
                onFriendProfileClick = onFriendProfileClick,
                onMessageClick = onMessageClick,
                onBattleClick = onBattleClick,
                notificationsViewModel = notificationsViewModel
            )

            BottomTab.ADD -> {}
        }

        if (showNotifications) {
            NotificationsSheet(
                onDismiss = { showNotifications = false },
                viewModel = notificationsViewModel,
                onNotificationClick = { notification ->
                    showNotifications = false

                    when (notification.type) {
                        "BATTLE_INVITE" -> {
                            onTabSelected(BottomTab.COMMUNITY)
                        }
                        "BATTLE_RESULT" -> {
                            notification.relatedEntityId?.let { battleId ->
                                onNavigateToBattleDetail(battleId)
                            }
                        }
                        "FRIEND_REQUEST", "FRIEND_ACCEPTED" -> {
                            onTabSelected(BottomTab.COMMUNITY)
                        }
                        // Kolejne akcje dodamy z czasem
                    }
                }
            )
        }
    }

    AnimatedVisibility(
        visible = showAddHabit,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 400)
        ) + fadeIn(animationSpec = tween(durationMillis = 400)),

        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        AddHabitHost(
            onClose = {
                showAddHabit = false
                onTabSelected(BottomTab.TODAY)
            }
        )
    }
}

@Composable
fun CheckBottomNavigationBar(
    currentTab: BottomTab?,
    onTabSelected: (BottomTab) -> Unit,
    onAddClick: () -> Unit,
    notificationsViewModel: NotificationsViewModel = viewModel()
) {

    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = Color.Transparent
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = currentTab == BottomTab.TODAY,
            onClick = { onTabSelected(BottomTab.TODAY) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_today)) },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentTab == BottomTab.STATS,
            onClick = { onTabSelected(BottomTab.STATS) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_stats)) },
            colors = navItemColors
        )

        NavigationBarItem(
            selected = false,
            onClick = { onTabSelected(BottomTab.ADD) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.nav_add),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_add)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentTab == BottomTab.MAP,
            onClick = { onTabSelected(BottomTab.MAP) },
            icon = { Icon(Icons.Default.Map, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_map)) },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentTab == BottomTab.COMMUNITY,
            onClick = { onTabSelected(BottomTab.COMMUNITY) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.People, contentDescription = null)
                }
            },
            label = { Text(stringResource(R.string.nav_community)) },
            colors = navItemColors
        )
    }
}
