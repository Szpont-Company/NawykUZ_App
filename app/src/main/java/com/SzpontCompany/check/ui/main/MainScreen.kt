// Ścieżka: src/main/java/com/SzpontCompany/check/ui/main/MainScreen.kt
package com.SzpontCompany.check.ui.main

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
import androidx.compose.ui.unit.dp
import com.SzpontCompany.check.ui.dashboard.TodayScreen
import com.SzpontCompany.check.ui.stats.StatsScreen
import com.SzpontCompany.check.ui.map.MapScreen
import com.SzpontCompany.check.ui.community.CommunityScreen

enum class BottomTab {
    TODAY, STATS, MAP, COMMUNITY
}

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(BottomTab.TODAY) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CheckBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { newTab -> currentTab = newTab },
                onAddClick = { /* TODO: Otwórz okno dodawania nawyku */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                BottomTab.TODAY -> TodayScreen()
                BottomTab.STATS -> StatsScreen()
                BottomTab.MAP -> MapScreen()
                BottomTab.COMMUNITY -> CommunityScreen()
            }
        }
    }
}

@Composable
fun CheckBottomNavigationBar(
    currentTab: BottomTab,
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
            onClick = onAddClick,
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