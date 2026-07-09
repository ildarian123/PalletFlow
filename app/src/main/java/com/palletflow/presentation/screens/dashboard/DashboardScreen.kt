package com.palletflow.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.palletflow.presentation.navigation.BottomNavTab
import com.palletflow.presentation.navigation.BottomNavigationBar
import com.palletflow.presentation.screens.inventory.InventoryScreen
import com.palletflow.presentation.screens.sales.SalesScreen

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val SuccessGreen = Color(0xFF4CD964)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onReceiveLotClick: () -> Unit,
    onSellItemClick: () -> Unit,
    onAddSaleClickWithId: (String) -> Unit,
    onMoveInventoryClick: () -> Unit,
    onAddTransferClickWithId: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onSeeAllActivityClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onInventoryItemClick: (String) -> Unit,
    onEditLotClick: (String) -> Unit,
    onAddItemClick: () -> Unit,
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    DashboardScreenContent(
        uiState = uiState,
        onReceiveLotClick = onReceiveLotClick,
        onSellItemClick = onSellItemClick,
        onAddSaleClickWithId = onAddSaleClickWithId,
        onMoveInventoryClick = onMoveInventoryClick,
        onAddTransferClickWithId = onAddTransferClickWithId,
        onAddExpenseClick = onAddExpenseClick,
        onSeeAllActivityClick = onSeeAllActivityClick,
        onLogoutClick = {
            viewModel.logout()
            onLogoutClick()
        },
        onInventoryItemClick = onInventoryItemClick,
        onEditLotClick = onEditLotClick,
        onAddItemClick = onAddItemClick,
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenContent(
    uiState: DashboardUiState,
    onReceiveLotClick: () -> Unit,
    onSellItemClick: () -> Unit,
    onAddSaleClickWithId: (String) -> Unit,
    onMoveInventoryClick: () -> Unit,
    onAddTransferClickWithId: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onSeeAllActivityClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onInventoryItemClick: (String) -> Unit,
    onEditLotClick: (String) -> Unit,
    onAddItemClick: () -> Unit,
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    navController: NavHostController
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hi, ${uiState.userName}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    Box {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentColor)
                                .clickable { menuExpanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.userName.firstOrNull()?.toString()?.uppercase() ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(SurfaceColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout", color = TextPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onLogoutClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = AccentColor
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentTab) {
                BottomNavTab.Dashboard -> {
                    DashboardContent(
                        uiState = uiState,
                        onReceiveLotClick = onReceiveLotClick,
                        onSellItemClick = onSellItemClick,
                        onMoveInventoryClick = onMoveInventoryClick,
                        onAddExpenseClick = onAddExpenseClick,
                        onSeeAllActivityClick = onSeeAllActivityClick
                    )
                }
                BottomNavTab.Inventory -> {
                    InventoryScreen(
                        onItemClick = onInventoryItemClick,
                        onAddClick = onAddItemClick,
                        navController = navController
                    )
                }
                BottomNavTab.Transactions -> {
                    SalesScreen(
                        onAddLotClick = onReceiveLotClick,
                        onEditLotClick = onEditLotClick,
                        onAddSaleClick = onSellItemClick,
                        onAddSaleClickWithId = onAddSaleClickWithId,
                        onAddTransferClick = onMoveInventoryClick,
                        onAddTransferClickWithId = onAddTransferClickWithId
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "${currentTab.title} Screen", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onReceiveLotClick: () -> Unit,
    onSellItemClick: () -> Unit,
    onMoveInventoryClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onSeeAllActivityClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Section
        MetricsSection(uiState)

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions
        QuickActionsSection(
            onReceiveLotClick = onReceiveLotClick,
            onSellItemClick = onSellItemClick,
            onMoveInventoryClick = onMoveInventoryClick,
            onAddExpenseClick = onAddExpenseClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Recent Activity
        RecentActivitySection(
            activities = uiState.recentActivities,
            onSeeAllActivityClick = onSeeAllActivityClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricsSection(uiState: DashboardUiState) {
    val metrics = listOf(
        MetricData("Total Profit", uiState.totalProfit, "This month", null),
        MetricData("Items Sold", uiState.itemsSold, "Total sold", null),
        MetricData("Active Lots", uiState.activeLots, "In stock", null),
        MetricData("Avg. Profit", uiState.avgProfit, "Per item", null),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(metrics) { metric ->
            MetricCard(metric)
        }
    }
}

data class MetricData(
    val title: String,
    val value: String,
    val subtitle: String,
    val percentage: String? = null
)

@Composable
fun MetricCard(data: MetricData) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(data.title, color = TextSecondary, fontSize = 12.sp)
            Column {
                Text(
                    data.value,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(data.subtitle, color = TextSecondary, fontSize = 11.sp)
                    if (data.percentage != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(data.percentage, color = SuccessGreen, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onReceiveLotClick: () -> Unit,
    onSellItemClick: () -> Unit,
    onMoveInventoryClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            "Quick Actions",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton("Receive", Icons.Default.AddBox, onReceiveLotClick)
            QuickActionButton("Sell", Icons.Default.Sell, onSellItemClick)
            QuickActionButton("Move", Icons.Default.LocalShipping, onMoveInventoryClick)
            QuickActionButton("Expense", Icons.Default.Payments, onAddExpenseClick)
        }
    }
}

@Composable
fun QuickActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = AccentColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun RecentActivitySection(
    activities: List<RecentActivityUiState>,
    onSeeAllActivityClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Activity",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "See All",
                color = AccentColor,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeAllActivityClick() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (activities.isEmpty()) {
            Text(
                "No recent activity",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            activities.forEach { activity ->
                ActivityItem(
                    title = activity.title,
                    detail = activity.detail,
                    detailColor = when (activity.detailColorType) {
                        ActivityDetailColorType.SUCCESS -> SuccessGreen
                        ActivityDetailColorType.NORMAL -> TextPrimary
                    }
                )
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, detail: String, detailColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                detail,
                color = detailColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardContent(
        uiState = DashboardUiState(
            userName = "PalletFlow Demo",
            totalProfit = "+$1,240.00",
            itemsSold = "48",
            activeLots = "12",
            avgProfit = "$25.80",
            recentActivities = listOf(
                RecentActivityUiState(
                    title = "Sold Samsung Tablet",
                    detail = "+$120.00",
                    detailColorType = ActivityDetailColorType.SUCCESS
                ),
                RecentActivityUiState(
                    title = "Received #1043",
                    detail = "-$850.00",
                    detailColorType = ActivityDetailColorType.NORMAL
                )
            )
        ),
        onReceiveLotClick = {},
        onSellItemClick = {},
        onMoveInventoryClick = {},
        onAddExpenseClick = {},
        onSeeAllActivityClick = {}
    )
}
