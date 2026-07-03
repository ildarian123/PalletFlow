package com.palletflow.presentation.screens.inventory

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.presentation.common.ItemContextMenu
import com.palletflow.presentation.common.WarehouseContextMenu
import com.palletflow.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val ImagePlaceholderColor = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onItemClick: (itemId: String) -> Unit,
    onAddClick: () -> Unit,
    navController: NavHostController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var showItemContextMenu by remember { mutableStateOf(false) }
    var selectedWarehouse by remember { mutableStateOf<Warehouse?>(null) }
    var showWarehouseContextMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.deleteEvent.collect { event ->
            when (event) {
                is DeleteEvent.Error -> snackbarHostState.showSnackbar(event.message)
                DeleteEvent.Success -> { /* List updates via Flow */ }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.warehouseDeleteEvent.collect { event ->
            when (event) {
                is DeleteEvent.Error -> snackbarHostState.showSnackbar(event.message)
                DeleteEvent.Success -> { /* List updates via Flow */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        floatingActionButton = {
            var fabExpanded by remember { mutableStateOf(false) }
            Box {
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = AccentColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
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
                        text = { Text("Warehouse", color = TextPrimary) },
                        onClick = {
                            fabExpanded = false
                            navController.navigate(Screen.NewWarehouse.route)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar (common)
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceColor),
                placeholder = { Text("Search...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = AccentColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            // TabRow
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = DarkBackground,
                contentColor = AccentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = AccentColor
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabChange(0) },
                    text = { Text("Items", fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabChange(1) },
                    text = { Text("Warehouses", fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            // Tab content
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.selectedTab) {
                    0 -> ItemsTab(
                        uiState = uiState,
                        onItemClick = onItemClick,
                        onLongClick = {
                            selectedItem = it
                            showItemContextMenu = true
                        },
                        onRetryClick = { viewModel.loadItems() },
                        onTypeFilterChange = viewModel::onTypeFilterChange
                    )
                    1 -> WarehousesTab(
                        uiState = uiState,
                        navController = navController,
                        onLongClick = {
                            selectedWarehouse = it
                            showWarehouseContextMenu = true
                        }
                    )
                }
            }
        }

        // Context menus
        if (showItemContextMenu && selectedItem != null) {
            ItemContextMenu(
                item = selectedItem!!,
                onDismiss = { showItemContextMenu = false; selectedItem = null },
                onEditClick = { navController.navigate(Screen.EditItem.createRoute(it)) },
                onDeleteClick = { viewModel.deleteItem(it) }
            )
        }

        if (showWarehouseContextMenu && selectedWarehouse != null) {
            WarehouseContextMenu(
                warehouse = selectedWarehouse!!,
                onDismiss = { showWarehouseContextMenu = false; selectedWarehouse = null },
                onEditClick = { navController.navigate(Screen.EditWarehouse.createRoute(it)) },
                onDeleteClick = { id, _ -> viewModel.deleteWarehouse(id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemsTab(
    uiState: InventoryUiState,
    onItemClick: (itemId: String) -> Unit,
    onLongClick: (item: Item) -> Unit,
    onRetryClick: () -> Unit,
    onTypeFilterChange: (ItemType?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Type Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = uiState.selectedTypeFilter == null,
                    onClick = { onTypeFilterChange(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceColor,
                        labelColor = TextSecondary,
                        selectedContainerColor = AccentColor,
                        selectedLabelColor = DarkBackground
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.selectedTypeFilter == ItemType.SINGLE_PRODUCT,
                    onClick = { onTypeFilterChange(ItemType.SINGLE_PRODUCT) },
                    label = { Text("Single Product") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceColor,
                        labelColor = TextSecondary,
                        selectedContainerColor = AccentColor,
                        selectedLabelColor = DarkBackground
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.selectedTypeFilter == ItemType.PALLET,
                    onClick = { onTypeFilterChange(ItemType.PALLET) },
                    label = { Text("Pallet") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceColor,
                        labelColor = TextSecondary,
                        selectedContainerColor = AccentColor,
                        selectedLabelColor = DarkBackground
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        when {
            uiState.isLoading && uiState.items.isEmpty() -> {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentColor)
                    }
                }
            }
            uiState.error != null && uiState.items.isEmpty() -> {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = Color(0xFFFF453A))
                        Button(onClick = onRetryClick, colors = ButtonDefaults.buttonColors(containerColor = AccentColor)) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }
            uiState.filteredItems.isEmpty() -> {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = ImagePlaceholderColor)
                        Text("No items match your search", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> {
                items(uiState.filteredItems, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onLongClick = { onLongClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WarehousesTab(
    uiState: InventoryUiState,
    navController: NavHostController,
    onLongClick: (warehouse: Warehouse) -> Unit
) {
    if (uiState.isLoading && uiState.warehouses.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentColor)
        }
        return
    }

    if (uiState.filteredWarehouses.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warehouse, null, tint = ImagePlaceholderColor, modifier = Modifier.size(64.dp))
                Text(
                    if (uiState.searchQuery.isBlank()) "No warehouses yet" else "No matching warehouses",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.searchQuery.isBlank()) {
                    Text("Tap + to add your first warehouse", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(uiState.filteredWarehouses, key = { it.id }) { warehouse ->
            WarehouseRow(
                warehouse = warehouse,
                onClick = { navController.navigate(Screen.WarehouseDetail.createRoute(warehouse.id)) },
                onLongClick = { onLongClick(warehouse) }
            )
        }
    }
}

@Composable
private fun WarehouseRow(
    warehouse: Warehouse,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ImagePlaceholderColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warehouse, null, tint = AccentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(warehouse.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                if (warehouse.address != null) {
                    Text(warehouse.address!!, color = TextSecondary, fontSize = 12.sp)
                }
            }
            val statusColor = if (warehouse.isActive) AccentColor else TextSecondary
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    if (warehouse.isActive) "Active" else "Inactive",
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: Item,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateAdded = remember(item.createdAt) {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        sdf.format(Date(item.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ImagePlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                if (item.photoPath != null) {
                    AsyncImage(
                        model = item.photoPath,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, null, tint = TextSecondary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = if (item.type == ItemType.PALLET) AccentColor else ImagePlaceholderColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (item.type == ItemType.PALLET) "Pallet" else "Product",
                            color = if (item.type == ItemType.PALLET) DarkBackground else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (!item.sku.isNullOrBlank()) {
                        Text(item.sku!!, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(dateAdded, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
}
