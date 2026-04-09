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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.SzpontCompany.check.ui.addhabit.AddHabitHost
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.ui.dashboard.TodayScreen
import com.SzpontCompany.check.ui.stats.StatsScreen
import com.SzpontCompany.check.ui.map.MapScreen
import com.SzpontCompany.check.ui.community.CommunityScreen
import com.SzpontCompany.check.ui.community.components.NotificationsSheet

enum class BottomTab {
    TODAY, STATS, MAP, COMMUNITY, ADD
}

@Composable
fun MainScreen(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onProfileClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onFriendProfileClick: () -> Unit = {},
    onMessageClick: (Friend) -> Unit = {}
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
                onNotificationsClick = { showNotifications = true }
            )
            BottomTab.STATS -> StatsScreen()
            BottomTab.MAP -> MapScreen()
            BottomTab.COMMUNITY -> CommunityScreen(
                onProfileClick = onProfileClick,
                onFriendProfileClick = onFriendProfileClick,
                onMessageClick = onMessageClick
            )
            BottomTab.ADD -> {}
        }

        if (showNotifications) {
            NotificationsSheet(
                onDismiss = { showNotifications = false }
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
    onAddClick: () -> Unit
) {
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
            label = { Text("Dziś") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentTab == BottomTab.STATS,
            onClick = { onTabSelected(BottomTab.STATS) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Statystyki") },
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
                        contentDescription = "Dodaj",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text("Dodaj") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentTab == BottomTab.MAP,
            onClick = { onTabSelected(BottomTab.MAP) },
            icon = { Icon(Icons.Default.Map, contentDescription = null) },
            label = { Text("Mapa") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentTab == BottomTab.COMMUNITY,
            onClick = { onTabSelected(BottomTab.COMMUNITY) },
            icon = { Icon(Icons.Default.People, contentDescription = null) },
            label = { Text("Społeczność") },
            colors = navItemColors
        )
    }
}