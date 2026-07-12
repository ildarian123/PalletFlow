package com.palletflow.presentation.screens.reports

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palletflow.domain.usecase.report.LotProfitData
import com.palletflow.domain.usecase.report.PlatformSalesData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val warehouses by viewModel.warehouses.collectAsStateWithLifecycle()
    
    var selectedDateFilter by remember { mutableStateOf(DateFilterReport.MONTH) }
    var selectedWarehouse by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reports",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5F5F7)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Date Filters
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterReport.entries.filter { it != DateFilterReport.CUSTOM }.forEach { filter ->
                    FilterChip(
                        selected = selectedDateFilter == filter,
                        onClick = { 
                            selectedDateFilter = filter
                            viewModel.onDateFilterChange(filter)
                        },
                        label = { Text(filter.name.lowercase().capitalize(), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00C2A0),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E1E1E),
                            labelColor = Color(0xFFA1A1A6)
                        ),
                        border = null
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Warehouse filter
            var expandedWarehouse by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(
                    onClick = { expandedWarehouse = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            selectedWarehouse?.let { id ->
                                warehouses.find { it.id == id }?.name ?: "All Warehouses"
                            } ?: "All Warehouses",
                            color = Color(0xFFF5F5F7)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFA1A1A6))
                    }
                }
                DropdownMenu(
                    expanded = expandedWarehouse, 
                    onDismissRequest = { expandedWarehouse = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("All Warehouses", color = Color.White) },
                        onClick = {
                            selectedWarehouse = null
                            viewModel.onWarehouseChange(null)
                            expandedWarehouse = false
                        }
                    )
                    warehouses.forEach { warehouse ->
                        DropdownMenuItem(
                            text = { Text(warehouse.name, color = Color.White) },
                            onClick = {
                                selectedWarehouse = warehouse.id
                                viewModel.onWarehouseChange(warehouse.id)
                                expandedWarehouse = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Loading state
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00C2A0))
                }
            } else if (uiState.error != null) {
                Text(uiState.error!!, color = Color(0xFFFF453A), modifier = Modifier.padding(vertical = 16.dp))
            } else {
                // KPI Cards
                uiState.overview?.let { overview ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiCard(
                                title = "Total Incoming",
                                value = "$${String.format("%,.0f", overview.totalIncoming)}",
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Total Revenue",
                                value = "$${String.format("%,.0f", overview.totalRevenue)}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiCard(
                                title = "Total Profit",
                                value = "$${String.format("%,.0f", overview.totalProfit)}",
                                color = if (overview.totalProfit >= 0) Color(0xFF00C2A0) else Color(0xFFFF453A),
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Profit Margin",
                                value = "${String.format("%.1f", overview.profitMargin)}%",
                                color = if (overview.profitMargin >= 0) Color(0xFF00C2A0) else Color(0xFFFF453A),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // TabRow
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = Color(0xFF121212),
                    contentColor = Color(0xFF00C2A0),
                    indicator = { tabPositions ->
                        // selectedTab will be 1 or 2. index 0 corresponds to 1, index 1 corresponds to 2.
                        val indicatorIndex = if (uiState.selectedTab == 1) 0 else 1
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[indicatorIndex]),
                            color = Color(0xFF00C2A0)
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.onTabChange(1) },
                        text = { Text("By Lot", fontSize = 14.sp) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.onTabChange(2) },
                        text = { Text("By Platform", fontSize = 14.sp) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Tab Content
                when (uiState.selectedTab) {
                    1 -> LotTab(uiState.lotProfits)
                    2 -> PlatformTab(uiState.platformSales)
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color(0xFFA1A1A6), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
private fun LotTab(lotProfits: List<LotProfitData>) {
    if (lotProfits.isEmpty()) {
        Text("No lots in this period", color = Color(0xFFA1A1A6), modifier = Modifier.padding(16.dp))
        return
    }

    var sortBy by remember { mutableStateOf("date") }
    var ascending by remember { mutableStateOf(false) }

    val sortedLots = remember(lotProfits, sortBy, ascending) {
        when (sortBy) {
            "lot" -> if (ascending) lotProfits.sortedBy { it.lotNumber } else lotProfits.sortedByDescending { it.lotNumber }
            "cost" -> if (ascending) lotProfits.sortedBy { it.cost } else lotProfits.sortedByDescending { it.cost }
            "revenue" -> if (ascending) lotProfits.sortedBy { it.revenue } else lotProfits.sortedByDescending { it.revenue }
            "profit" -> if (ascending) lotProfits.sortedBy { it.profit } else lotProfits.sortedByDescending { it.profit }
            "roi" -> if (ascending) lotProfits.sortedBy { it.roi } else lotProfits.sortedByDescending { it.roi }
            else -> if (ascending) lotProfits.sortedBy { it.date } else lotProfits.sortedByDescending { it.date }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderItem("Lot", Modifier.weight(1.2f), sortBy == "lot") { sortBy = "lot"; ascending = !ascending }
            HeaderItem("Cost", Modifier.weight(0.9f), sortBy == "cost") { sortBy = "cost"; ascending = !ascending }
            HeaderItem("Rev", Modifier.weight(0.9f), sortBy == "revenue") { sortBy = "revenue"; ascending = !ascending }
            HeaderItem("Profit", Modifier.weight(0.9f), sortBy == "profit") { sortBy = "profit"; ascending = !ascending }
            HeaderItem("ROI", Modifier.weight(0.7f), sortBy == "roi") { sortBy = "roi"; ascending = !ascending }
        }

        // Rows
        sortedLots.forEach { lot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(lot.lotNumber, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("$${String.format("%.0f", lot.cost)}", color = Color(0xFFA1A1A6), fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text("$${String.format("%.0f", lot.revenue)}", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text(
                        "$${String.format("%.0f", lot.profit)}",
                        color = if (lot.profit >= 0) Color(0xFF00C2A0) else Color(0xFFFF453A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.9f)
                    )
                    Text(
                        "${String.format("%.0f", lot.roi)}%",
                        color = if (lot.roi >= 0) Color(0xFF00C2A0) else Color(0xFFFF453A),
                        fontSize = 12.sp,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatformTab(platformSales: List<PlatformSalesData>) {
    if (platformSales.isEmpty()) {
        Text("No sales in this period", color = Color(0xFFA1A1A6), modifier = Modifier.padding(16.dp))
        return
    }

    var sortBy by remember { mutableStateOf("rev") }
    var ascending by remember { mutableStateOf(false) }

    val sortedPlatforms = remember(platformSales, sortBy, ascending) {
        when (sortBy) {
            "platform" -> if (ascending) platformSales.sortedBy { it.platform } else platformSales.sortedByDescending { it.platform }
            "sales" -> if (ascending) platformSales.sortedBy { it.salesCount } else platformSales.sortedByDescending { it.salesCount }
            "rev" -> if (ascending) platformSales.sortedBy { it.revenue } else platformSales.sortedByDescending { it.revenue }
            "avg" -> if (ascending) platformSales.sortedBy { it.avgPrice } else platformSales.sortedByDescending { it.avgPrice }
            "profit" -> if (ascending) platformSales.sortedBy { it.profit } else platformSales.sortedByDescending { it.profit }
            else -> platformSales
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderItem("Platform", Modifier.weight(1.2f), sortBy == "platform") { sortBy = "platform"; ascending = !ascending }
            HeaderItem("Sales", Modifier.weight(0.7f), sortBy == "sales") { sortBy = "sales"; ascending = !ascending }
            HeaderItem("Rev", Modifier.weight(0.9f), sortBy == "rev") { sortBy = "rev"; ascending = !ascending }
            HeaderItem("Avg", Modifier.weight(0.9f), sortBy == "avg") { sortBy = "avg"; ascending = !ascending }
            HeaderItem("Profit", Modifier.weight(0.9f), sortBy == "profit") { sortBy = "profit"; ascending = !ascending }
        }

        // Rows
        sortedPlatforms.forEach { platform ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(platform.platform, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text(platform.salesCount.toString(), color = Color(0xFFA1A1A6), fontSize = 12.sp, modifier = Modifier.weight(0.7f))
                    Text("$${String.format("%.0f", platform.revenue)}", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text("$${String.format("%.0f", platform.avgPrice)}", color = Color(0xFFA1A1A6), fontSize = 12.sp, modifier = Modifier.weight(0.9f))
                    Text(
                        "$${String.format("%.0f", platform.profit)}",
                        color = if (platform.profit >= 0) Color(0xFF00C2A0) else Color(0xFFFF453A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderItem(
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Text(
        text = label + if (isSelected) " ↓" else "",
        color = if (isSelected) Color(0xFF00C2A0) else Color(0xFFA1A1A6),
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier.clickable { onClick() }
    )
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
