package com.palletflow.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
        BottomNavItem(Screen.Inventory.route, "Inventory", Icons.Default.Inventory),
        BottomNavItem(Screen.Transactions.route, "Transactions", Icons.Default.Receipt),
        BottomNavItem(Screen.Reports.route, "Reports", Icons.Default.BarChart),
        BottomNavItem(Screen.Settings.route, "More", Icons.Default.MoreHoriz)
    )

    NavigationBar(
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color(0xFF00C2A0)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) Color(0xFF00C2A0) else Color(0xFFA1A1A6)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) Color(0xFF00C2A0) else Color(0xFFA1A1A6)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF1E1E1E)
                )
            )
        }
    }
}
