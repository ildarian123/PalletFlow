package com.palletflow.presentation.screens.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val ImagePlaceholderColor = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onItemClick: (Int) -> Unit = {},
    onAddClick: () -> Unit = {},
    onWarehouseListClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    InventoryScreenContent(
        uiState = uiState,
        onItemClick = onItemClick,
        onAddClick = onAddClick,
        onWarehouseListClick = onWarehouseListClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreenContent(
    uiState: InventoryUiState,
    onItemClick: (Int) -> Unit = {},
    onAddClick: () -> Unit = {},
    onWarehouseListClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedWarehouseIndex by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current
    val warehouses = uiState.warehouses

    // Debounce logic
    LaunchedEffect(searchQuery) {
        delay(1000)
        debouncedQuery = searchQuery
    }

    val filteredItems = remember(debouncedQuery, selectedWarehouseIndex, uiState.items) {
        uiState.items.filter { item ->
            val matchesQuery = item.name.contains(debouncedQuery, ignoreCase = true)
            val matchesWarehouse = if (selectedWarehouseIndex == 0) true 
                                   else item.warehouse == warehouses.getOrNull(selectedWarehouseIndex)
            matchesQuery && matchesWarehouse
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor),
                    placeholder = { Text("Search items...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AccentColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // 2. Warehouse Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        modifier = Modifier
                            .size(height = 32.dp, width = 48.dp)
                            .clickable { onWarehouseListClick() },
                        color = SurfaceColor,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccentColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = "Warehouses",
                                tint = AccentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
//                    LazyRow(
//                        contentPadding = PaddingValues(start = 8.dp, end = 16.dp),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        items(warehouses.size) { index ->
//                            val isSelected = selectedWarehouseIndex == index
//                            FilterChip(
//                                selected = isSelected,
//                                onClick = { selectedWarehouseIndex = index },
//                                label = { Text(warehouses[index]) },
//                                colors = FilterChipDefaults.filterChipColors(
//                                    containerColor = SurfaceColor,
//                                    labelColor = TextSecondary,
//                                    selectedContainerColor = AccentColor,
//                                    selectedLabelColor = DarkBackground
//                                ),
//                                border = null,
//                                shape = RoundedCornerShape(12.dp)
//                            )
//                        }
//                    }
                }
            }

            // 3. Loading State
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentColor)
                    }
                }
            }

            // 4. Empty State
            if (!uiState.isLoading && filteredItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No items found", color = TextSecondary)
                    }
                }
            }

            // 5. Items List
            items(filteredItems) { item ->
                InventoryItemCard(item) {
                    onItemClick(item.id)
                }
            }
        }

        // 6. FAB with Drop-up Menu
        var fabExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                containerColor = AccentColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }

            DropdownMenu(
                expanded = fabExpanded,
                onDismissRequest = { fabExpanded = false },
                modifier = Modifier.background(SurfaceColor)
            ) {
                DropdownMenuItem(
                    text = { Text("Item", color = TextPrimary) },
                    onClick = {
                        fabExpanded = false
                        onAddClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Folder", color = TextPrimary) },
                    onClick = {
                        fabExpanded = false
                        // Handle folder creation
                    }
                )
                DropdownMenuItem(
                    text = { Text("Warehouse", color = TextPrimary) },
                    onClick = {
                        fabExpanded = false
                        // Handle warehouse creation
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryItemCard(item: InventoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Square image placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ImagePlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center: Details
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Lot ${item.lotNumber}", color = TextSecondary, fontSize = 12.sp)
                Text(item.warehouse, color = TextSecondary, fontSize = 12.sp)
            }

            // Right: Qty and Price
            Column(horizontalAlignment = Alignment.End) {
                Text(item.quantity, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(item.price, color = TextSecondary, fontSize = 12.sp)
                Text(item.profit, color = AccentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    InventoryScreenContent(
        uiState = InventoryUiState(
            items = listOf(
                InventoryItem(1, "Wireless Headphones Z1", "#1042", "Warehouse 1", "x24", "$12.50", "+ $8.00"),
                InventoryItem(2, "Mechanical Keyboard", "#1038", "Warehouse 1", "x12", "$8.00", "+ $5.50")
            ),
            warehouses = listOf("All Warehouses", "Warehouse 1")
        )
    )
}
