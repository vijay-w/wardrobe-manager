package com.wardrobemanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = WardrobeDestinations.WARDROBE_ROUTE,
        icon = Icons.Default.Checkroom,
        label = "衣橱"
    ),
    BottomNavItem(
        route = WardrobeDestinations.ADD_CLOTHING_ROUTE,
        icon = Icons.Default.Add,
        label = "添加"
    ),
    BottomNavItem(
        route = WardrobeDestinations.OUTFIT_LIST_ROUTE,
        icon = Icons.Default.Style,
        label = "穿搭"
    ),
    BottomNavItem(
        route = WardrobeDestinations.STATISTICS_ROUTE,
        icon = Icons.Default.Analytics,
        label = "统计"
    ),
    BottomNavItem(
        route = WardrobeDestinations.BACKUP_ROUTE,
        icon = Icons.Default.Backup,
        label = "备份"
    )
)

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}