package com.palletflow.presentation.screens.warehouse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseListScreen(
    onBackClick: () -> Unit = {},
    onWarehouseClick: (String) -> Unit = {},
    onAddWarehouseClick: () -> Unit = {},
    viewModel: WarehouseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    WarehouseListScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onWarehouseClick = onWarehouseClick,
        onAddWarehouseClick = onAddWarehouseClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseListScreenContent(
    uiState: WarehouseListUiState,
    onBackClick: () -> Unit = {},
    onWarehouseClick: (String) -> Unit = {},
    onAddWarehouseClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredWarehouses = uiState.warehouses.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Warehouses", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddWarehouseClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Warehouse", tint = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWarehouseClick,
                containerColor = AccentTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Warehouse")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentTeal)
            }
        } else if (uiState.warehouses.isEmpty()) {
            EmptyState(onAddWarehouseClick)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Summary row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Warehouses",
                        value = uiState.warehouses.size.toString(),
                        valueColor = TextPrimary
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Value",
                        value = uiState.totalValue,
                        valueColor = AccentTeal
                    )
                }

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor),
                    placeholder = { Text("Search warehouses...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AccentTeal,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(filteredWarehouses) { warehouse ->
                        WarehouseCard(warehouse) {
                            onWarehouseClick(warehouse.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, label: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WarehouseCard(warehouse: WarehouseItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center info
            Column(modifier = Modifier.weight(1f)) {
                Text(warehouse.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(warehouse.location, color = TextSecondary, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = PlaceholderColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${warehouse.itemsCount} items",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = AccentTeal,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            warehouse.totalValue,
                            color = DarkBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Right status and chevron
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (warehouse.isActive) AccentTeal else PlaceholderColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (warehouse.isActive) "Active" else "Inactive",
                        color = if (warehouse.isActive) DarkBackground else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyState(onAddWarehouseClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Warehouse,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = PlaceholderColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No warehouses yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Tap + to add your first warehouse", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddWarehouseClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Warehouse", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WarehouseListScreenPreview() {
    WarehouseListScreenContent(
        uiState = WarehouseListUiState(
            warehouses = listOf(
                WarehouseItem("1", "Main Warehouse", "Chicago, IL", true, "1,240", "$45,000"),
                WarehouseItem("2", "Storage Unit 2", "Chicago, IL", true, "380", "$12,500")
            ),
            totalValue = "$57,500"
        )
    )
}
