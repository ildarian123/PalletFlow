package com.palletflow.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavTab(val route: String, val title: String, val icon: ImageVector) {
    Dashboard(Screen.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
    Inventory(Screen.Inventory.route, "Inventory", Icons.Default.Inventory),
    Transactions(Screen.Transactions.route, "Transactions", Icons.Default.Receipt),
    Reports(Screen.Reports.route, "Reports", Icons.Default.BarChart),
    More(Screen.Settings.route, "More", Icons.Default.MoreHoriz)
}

@Composable
fun BottomNavigationBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color(0xFF00C2A0)
    ) {
        BottomNavTab.entries.forEach { tab ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (selected) Color(0xFF00C2A0) else Color(0xFFA1A1A6)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
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
