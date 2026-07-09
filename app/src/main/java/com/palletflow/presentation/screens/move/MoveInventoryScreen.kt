package com.palletflow.presentation.screens.move

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.Warehouse

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)
private val ErrorRed = Color(0xFFFF453A)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MoveInventoryScreen(
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    viewModel: MoveInventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itemsWithStock by viewModel.itemsWithStock.collectAsStateWithLifecycle()
    val warehouses by viewModel.warehouses.collectAsStateWithLifecycle()

    var showSourceSheet by remember { mutableStateOf(false) }
    var showDestSheet by remember { mutableStateOf(false) }
    var showItemSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { success ->
            if (success) onConfirmClick()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Move Inventory", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            color = AccentTeal,
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(onClick = viewModel::confirm) {
                            Text("Confirm", color = AccentTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showItemSheet = true },
                containerColor = AccentTeal,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Item") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Error Banner
                val activeError = uiState.error ?: uiState.itemError ?: uiState.sourceError ?: uiState.destError
                activeError?.let { error ->
                    item {
                        Surface(
                            color = ErrorRed.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = error,
                                color = ErrorRed,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item {
                    MoveHeaderSection(
                        transferNumber = uiState.transferNumber,
                        sourceWarehouseId = uiState.sourceWarehouseId,
                        destWarehouseId = uiState.destWarehouseId,
                        warehouses = warehouses,
                        notes = uiState.notes,
                        onSourceClick = { showSourceSheet = true },
                        onDestClick = { showDestSheet = true },
                        onNotesChange = viewModel::addNote,
                        itemCount = uiState.items.size,
                        totalUnits = uiState.totalItems
                    )
                }

                if (uiState.items.isNotEmpty()) {
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Items to Move", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("${uiState.items.size} items", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    itemsIndexed(uiState.items) { index, item ->
                        MoveItemRow(
                            item = item,
                            onRemove = { viewModel.removeItem(index) }
                        )
                    }
                } else {
                    item {
                        MoveEmptyState(onAddClick = { showItemSheet = true })
                    }
                }
            }

            if (showSourceSheet) {
                WarehouseSelectorSheet(
                    title = "Select Source",
                    subtitle = "Where are items moving from?",
                    iconColor = ErrorRed,
                    warehouses = warehouses,
                    onDismiss = { showSourceSheet = false },
                    onSelect = {
                        viewModel.onSourceWarehouseChange(it)
                        showSourceSheet = false
                    }
                )
            }

            if (showDestSheet) {
                WarehouseSelectorSheet(
                    title = "Select Destination",
                    subtitle = "Where are items moving to?",
                    iconColor = AccentTeal,
                    warehouses = warehouses,
                    onDismiss = { showDestSheet = false },
                    onSelect = {
                        viewModel.onDestWarehouseChange(it)
                        showDestSheet = false
                    }
                )
            }

            if (showItemSheet) {
                ItemSelectorSheet(
                    itemsWithStock = itemsWithStock,
                    onDismiss = { showItemSheet = false },
                    onAddItem = { item, qty ->
                        viewModel.addItem(item, qty)
                        showItemSheet = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MoveHeaderSection(
    transferNumber: String,
    sourceWarehouseId: String,
    destWarehouseId: String,
    warehouses: List<Warehouse>,
    notes: String,
    onSourceClick: () -> Unit,
    onDestClick: () -> Unit,
    onNotesChange: (String) -> Unit,
    itemCount: Int,
    totalUnits: Int
) {
    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Transfer Info", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Transfer Number
            Column {
                Text("Transfer Number", color = TextSecondary, fontSize = 12.sp)
                Text(transferNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val sourceName = warehouses.find { it.id == sourceWarehouseId }?.name ?: "Select Source Warehouse"
            SelectorRow(
                label = "From",
                value = sourceName,
                icon = Icons.Default.Warehouse,
                iconColor = ErrorRed,
                onClick = onSourceClick
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = PlaceholderColor)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor)
                        .border(1.dp, AccentTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
                }
                HorizontalDivider(modifier = Modifier.weight(1f), color = PlaceholderColor)
            }

            val destName = warehouses.find { it.id == destWarehouseId }?.name ?: "Select Destination Warehouse"
            SelectorRow(
                label = "To",
                value = destName,
                icon = Icons.Default.Warehouse,
                iconColor = AccentTeal,
                onClick = onDestClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optional notes about this transfer...", color = TextSecondary) },
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = PlaceholderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            HorizontalDivider(color = PlaceholderColor, modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Items to move:", color = TextSecondary, fontSize = 14.sp)
                Text("$itemCount items", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total quantity:", color = TextSecondary, fontSize = 14.sp)
                Text("$totalUnits units", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SelectorRow(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PlaceholderColor)
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value,
                color = if (value.contains("Select")) TextSecondary else TextPrimary,
                fontWeight = if (value.contains("Select")) FontWeight.Normal else FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun MoveItemRow(item: MoveInventoryUiState.ItemRow, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(PlaceholderColor), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.itemName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (item.lotId.isNotEmpty()) {
                    Text("Lot: ${item.lotId}", color = TextSecondary, fontSize = 11.sp)
                }
                Text("Quantity: ${item.quantity}", color = TextSecondary, fontSize = 11.sp)
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun MoveEmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.MoveToInbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = PlaceholderColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No items selected", color = TextPrimary, fontWeight = FontWeight.Bold)
        Text("Tap + to choose items to move", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Items", color = DarkBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WarehouseSelectorSheet(
    title: String,
    subtitle: String,
    iconColor: Color,
    warehouses: List<Warehouse>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(warehouses) { warehouse ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(warehouse.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warehouse, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(warehouse.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(warehouse.address ?: "No address", color = TextSecondary, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemSelectorSheet(
    itemsWithStock: List<com.palletflow.presentation.screens.move.ItemWithStock>,
    onDismiss: () -> Unit,
    onAddItem: (Item, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Text("Select Items", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search inventory...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = PlaceholderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                val filtered = itemsWithStock.filter { it.item.name.contains(searchQuery, ignoreCase = true) }
                items(filtered) { itemWithStock ->
                    val item = itemWithStock.item
                    var qty by remember { mutableStateOf("1") }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceColor), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Available: ${itemWithStock.stockQuantity}", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                if (item.lotId != null) {
                                    Text("Lot: ${item.lotId}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = qty,
                                    onValueChange = { qty = it },
                                    modifier = Modifier.width(60.dp).height(50.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTeal,
                                        unfocusedBorderColor = SurfaceColor,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                Button(
                                    onClick = { 
                                        val q = qty.toIntOrNull() ?: 0
                                        if (q > 0) onAddItem(item, q) 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Add", color = DarkBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
