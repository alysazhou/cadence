package com.cs407.cadence.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

@Composable
fun BottomNav(navController: NavController) {
    val items = listOf(NavigationItem.Home, NavigationItem.Log, NavigationItem.Settings)

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        items.forEach { item ->
            NavigationBarItem(
                    icon = {
                        Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    label = null,
                    selected = false, // This will be dynamic later
                    onClick = { navController.navigate(route = item.route) }
            )
        }
    }
}

// navigation items
sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Home")
    object Log : NavigationItem("log", Icons.Default.List, "Log")
    object Settings : NavigationItem("settings", Icons.Default.Settings, "Settings")
}
