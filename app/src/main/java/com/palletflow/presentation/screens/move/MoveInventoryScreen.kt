package com.palletflow.presentation.screens.move

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)
private val ErrorRed = Color(0xFFFF453A)
private val WarningAmber = Color(0xFFFF9500)

data class MoveItemData(
    val name: String,
    val lotRef: String,
    val condition: String,
    val available: Int,
    val selectedQty: String = "",
    val conditionColor: Color
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MoveInventoryScreen(
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    var sourceWarehouse by remember { mutableStateOf<String?>(null) }
    var destWarehouse by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    
    var showFromSheet by remember { mutableStateOf(false) }
    var showToSheet by remember { mutableStateOf(false) }
    var showItemSheet by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val selectedItems = remember {
        mutableStateListOf<MoveItemData>()
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
                    TextButton(onClick = { showConfirmDialog = true }) {
                        Text("Confirm", color = AccentTeal, fontWeight = FontWeight.Bold)
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
                item {
                    MoveHeaderSection(
                        sourceWarehouse = sourceWarehouse,
                        destWarehouse = destWarehouse,
                        notes = notes,
                        onSourceClick = { showFromSheet = true },
                        onDestClick = { showToSheet = true },
                        onNotesChange = { notes = it },
                        itemCount = selectedItems.size,
                        totalUnits = selectedItems.sumOf { it.selectedQty.toIntOrNull() ?: 0 }
                    )
                }

                if (selectedItems.isNotEmpty()) {
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
                            Text("${selectedItems.size} items", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    items(selectedItems) { item ->
                        MoveItemRow(
                            item = item,
                            onRemove = { selectedItems.remove(item) }
                        )
                    }
                } else {
                    item {
                        MoveEmptyState(onAddClick = { showItemSheet = true })
                    }
                }
            }

            if (showFromSheet) {
                WarehouseSelectorSheet(
                    title = "Select Source",
                    subtitle = "Where are items moving from?",
                    iconColor = ErrorRed,
                    onDismiss = { showFromSheet = false },
                    onSelect = {
                        sourceWarehouse = it
                        showFromSheet = false
                    }
                )
            }

            if (showToSheet) {
                WarehouseSelectorSheet(
                    title = "Select Destination",
                    subtitle = "Where are items moving to?",
                    iconColor = AccentTeal,
                    onDismiss = { showToSheet = false },
                    onSelect = {
                        destWarehouse = it
                        showToSheet = false
                    }
                )
            }

            if (showItemSheet) {
                ItemSelectorSheet(
                    onDismiss = { showItemSheet = false },
                    onAddSelected = { items ->
                        selectedItems.addAll(items)
                        showItemSheet = false
                    }
                )
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    containerColor = SurfaceColor,
                    title = { Text("Confirm Transfer?", color = TextPrimary) },
                    text = {
                        Text(
                            "You are moving ${selectedItems.size} items from ${sourceWarehouse ?: "Source"} to ${destWarehouse ?: "Destination"}.",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showConfirmDialog = false
                            onConfirmClick()
                        }) {
                            Text("Move", color = AccentTeal)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MoveHeaderSection(
    sourceWarehouse: String?,
    destWarehouse: String?,
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
            Text("Move Details", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            SelectorRow(
                label = "From",
                value = sourceWarehouse ?: "Select Source Warehouse",
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

            SelectorRow(
                label = "To",
                value = destWarehouse ?: "Select Destination Warehouse",
                icon = Icons.Default.Warehouse,
                iconColor = AccentTeal,
                onClick = onDestClick
            )

            if (sourceWarehouse != null && destWarehouse != null && sourceWarehouse == destWarehouse) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                    Text("Source and destination cannot be the same", color = ErrorRed, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SelectorRow(
                label = "Date",
                value = "Jun 27, 2024",
                icon = Icons.Default.CalendarToday,
                iconColor = TextSecondary,
                onClick = { }
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
                    unfocusedContainerColor = Color.Transparent
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
private fun MoveItemRow(item: MoveItemData, onRemove: () -> Unit) {
    var qtyToMove by remember { mutableStateOf(TextFieldValue(item.selectedQty)) }

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
                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("From ${item.lotRef}", color = TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = item.conditionColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(item.condition, color = item.conditionColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                    Text("Available: ${item.available}", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                OutlinedTextField(
                    value = qtyToMove,
                    onValueChange = { qtyToMove = it },
                    modifier = Modifier.width(80.dp).height(64.dp).onFocusChanged {
                        if (it.isFocused) qtyToMove = qtyToMove.copy(selection = TextRange(0, qtyToMove.text.length))
                    },
                    placeholder = { Text("Qty", fontSize = 14.sp) },
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = PlaceholderColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
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
            Text("Add Items", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WarehouseSelectorSheet(
    title: String,
    subtitle: String,
    iconColor: Color,
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
            
            val warehouses = listOf("Main Warehouse", "Storage Unit 2", "Retail Location")
            warehouses.forEach { name ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(name) },
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
                            Text(name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Chicago, IL", color = TextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemSelectorSheet(onDismiss: () -> Unit, onAddSelected: (List<MoveItemData>) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery
    }

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
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val allItems = listOf(
                MoveItemData(name = "Nike Air Max 90", lotRef = "Lot #1042", condition = "Used", available = 5, conditionColor = WarningAmber),
                MoveItemData(name = "Samsung Tablet", lotRef = "Lot #1041", condition = "New", available = 2, conditionColor = AccentTeal),
                MoveItemData(name = "Bluetooth Speaker", lotRef = "Lot #1042", condition = "Used", available = 8, conditionColor = WarningAmber),
                MoveItemData(name = "Levi's Jeans", lotRef = "Lot #1043", condition = "Used", available = 3, conditionColor = WarningAmber),
                MoveItemData(name = "Coffee Maker", lotRef = "Lot #1040", condition = "New", available = 1, conditionColor = AccentTeal)
            )
            
            val filteredItems = allItems.filter { it.name.contains(debouncedQuery, ignoreCase = true) }
            
            val itemQuantities = remember { mutableStateMapOf<String, TextFieldValue>() }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredItems) { item ->
                    val qtyValue = itemQuantities[item.name] ?: TextFieldValue("")
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
                                Text(item.lotRef, color = TextSecondary, fontSize = 11.sp)
                                Text("Available: ${item.available}", color = AccentTeal, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                OutlinedTextField(
                                    value = qtyValue,
                                    onValueChange = { itemQuantities[item.name] = it },
                                    modifier = Modifier.width(80.dp).height(64.dp).onFocusChanged {
                                        if (it.isFocused) itemQuantities[item.name] = qtyValue.copy(selection = TextRange(0, qtyValue.text.length))
                                    },
                                    placeholder = { Text("Qty", fontSize = 14.sp) },
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        color = TextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTeal,
                                        unfocusedBorderColor = SurfaceColor,
                                        focusedContainerColor = SurfaceColor,
                                        unfocusedContainerColor = SurfaceColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = PlaceholderColor)
            Spacer(modifier = Modifier.height(16.dp))
            
            val isButtonEnabled = itemQuantities.values.any { it.text.isNotEmpty() && (it.text.toIntOrNull() ?: 0) > 0 }
            
            Button(
                onClick = { 
                    val selected = filteredItems
                        .filter { itemQuantities[it.name]?.text?.isNotEmpty() == true }
                        .map { it.copy(selectedQty = itemQuantities[it.name]?.text ?: "") }
                    onAddSelected(selected) 
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentTeal,
                    disabledContainerColor = PlaceholderColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Selected", color = if (isButtonEnabled) DarkBackground else TextSecondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoveInventoryScreenPreview() {
    MoveInventoryScreen()
}
