package com.example.quitesmoking

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quitesmoking.navigation.Routes

sealed class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String) {
    object Home     : BottomNavItem(Routes.HOME, Icons.Default.Home, "Home")
    object Urge     : BottomNavItem(Routes.URGE, Icons.Default.Whatshot, "Urges")
    object Progress : BottomNavItem(Routes.PROGRESS, Icons.Default.BarChart, "Milestones")
    object Chat     : BottomNavItem(Routes.GPT_CHAT, Icons.Default.SmartToy, "Chat")
    object Weekly   : BottomNavItem(Routes.WEEKLY_GOAL_EDIT, Icons.Default.Flag, "Weekly")
}

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Urge,
        BottomNavItem.Progress,
        BottomNavItem.Chat,
        BottomNavItem.Weekly
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp) }
            )
        }
    }
}
