package com.palletflow.presentation.screens.sell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.Warehouse
import java.util.*

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)
private val ErrorRed = Color(0xFFFF453A)
private val InfoBlue = Color(0xFF4A90E2)
private val PlatformPurple = Color(0xFF9B59B6)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SellScreen(
    onBackClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    viewModel: SellViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itemsWithStock by viewModel.itemsWithStock.collectAsStateWithLifecycle()
    val warehouses by viewModel.warehouses.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var isHeaderExpanded by remember { mutableStateOf(true) }
    var showItemSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { success ->
            if (success) onCompleteClick()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("New Sale", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        TextButton(
                            onClick = { viewModel.complete() },
                            enabled = !uiState.isLoading
                        ) {
                            Text("Complete", color = AccentTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showItemSheet = true else showExpenseSheet = true
                },
                containerColor = AccentTeal,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (selectedTab == 0) "Add Item" else "Add Expense") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Error Messages
                val activeError = uiState.error ?: uiState.itemError ?: uiState.platformError ?: uiState.warehouseError
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

                // Header Section
                item {
                    SaleInfoHeader(
                        uiState = uiState,
                        warehouses = warehouses,
                        isExpanded = isHeaderExpanded,
                        onToggleExpand = { isHeaderExpanded = !isHeaderExpanded },
                        onPlatformSelect = viewModel::onPlatformChange,
                        onWarehouseSelect = viewModel::onWarehouseChange
                    )
                }

                // Sticky Tabs
                stickyHeader {
                    SecondaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkBackground,
                        contentColor = AccentTeal,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(selectedTab),
                                color = AccentTeal
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Items",
                                    color = if (selectedTab == 0) AccentTeal else TextSecondary,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Expenses",
                                    color = if (selectedTab == 1) AccentTeal else TextSecondary,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Tab Content
                if (selectedTab == 0) {
                    if (uiState.items.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.ShoppingCart,
                                title = "No items added",
                                subtitle = "Tap + to add items from inventory"
                            )
                        }
                    } else {
                        itemsIndexed(uiState.items) { index, item ->
                            SaleItemRow(
                                item = item,
                                onPriceChange = { viewModel.updateItemPrice(index, it) },
                                onRemove = { viewModel.removeItem(index) }
                            )
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Expenses:", color = TextSecondary)
                            Text("-$${String.format(Locale.US, "%.2f", uiState.totalExpenses)}", color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (uiState.expenses.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.Receipt,
                                title = "No expenses added",
                                subtitle = "Tap + to add shipping, fees or other costs"
                            )
                        }
                    } else {
                        itemsIndexed(uiState.expenses) { index, expense ->
                            SaleExpenseRow(
                                expense = expense,
                                onRemove = { viewModel.removeExpense(index) }
                            )
                        }
                    }
                }
            }

            // Bottom Sheets
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

            if (showExpenseSheet) {
                AddExpenseSheet(
                    onDismiss = { showExpenseSheet = false },
                    onAddExpense = { name, amount ->
                        viewModel.addExpense(name, amount)
                        showExpenseSheet = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleInfoHeader(
    uiState: SellUiState,
    warehouses: List<Warehouse>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPlatformSelect: (String) -> Unit,
    onWarehouseSelect: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var warehouseExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sale Info", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Sale Number
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    clipboardManager.setText(AnnotatedString(uiState.saleNumber))
                },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sale Number", color = TextSecondary, fontSize = 12.sp)
                    Text(uiState.saleNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Surface(color = AccentTeal, shape = RoundedCornerShape(4.dp)) {
                    Text("Auto", color = DarkBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Platform Selector
                    Text("Platform", color = TextSecondary, fontSize = 12.sp)
                    uiState.platformError?.let { Text(it, color = ErrorRed, fontSize = 10.sp) }
                    Spacer(modifier = Modifier.height(8.dp))
                    val platforms = listOf("eBay", "Facebook", "Cash", "Bin Store")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(platforms) { platform ->
                            FilterChip(
                                selected = uiState.platform == platform,
                                onClick = { onPlatformSelect(platform) },
                                label = { Text(platform) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentTeal,
                                    selectedLabelColor = DarkBackground,
                                    containerColor = PlaceholderColor,
                                    labelColor = TextSecondary
                                ),
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Warehouse Selector
                    Text("Warehouse", color = TextSecondary, fontSize = 12.sp)
                    uiState.warehouseError?.let { Text(it, color = ErrorRed, fontSize = 10.sp) }
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = warehouseExpanded,
                        onExpandedChange = { warehouseExpanded = it }
                    ) {
                        val selectedWarehouseName = warehouses.find { it.id == uiState.warehouseId }?.name ?: "Select Warehouse"
                        OutlinedTextField(
                            value = selectedWarehouseName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                unfocusedBorderColor = PlaceholderColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = warehouseExpanded,
                            onDismissRequest = { warehouseExpanded = false },
                            modifier = Modifier.background(SurfaceColor)
                        ) {
                            warehouses.forEach { warehouse ->
                                DropdownMenuItem(
                                    text = { Text(warehouse.name, color = TextPrimary) },
                                    onClick = {
                                        onWarehouseSelect(warehouse.id)
                                        warehouseExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = PlaceholderColor, modifier = Modifier.padding(vertical = 12.dp))

                    SummaryMetricRow("Items Total:", "$${String.format(Locale.US, "%.2f", uiState.totalRevenue)}", TextPrimary)
                    SummaryMetricRow("Expenses:", "-$${String.format(Locale.US, "%.2f", uiState.totalExpenses)}", ErrorRed)
                    SummaryMetricRow("Net Revenue:", "$${String.format(Locale.US, "%.2f", uiState.netRevenue)}", AccentTeal, isLarge = true)
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(onClick = onToggleExpand, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Header",
                        tint = AccentTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricRow(label: String, value: String, valueColor: Color, isLarge: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = if (isLarge) 14.sp else 12.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = if (isLarge) 18.sp else 14.sp)
    }
}

@Composable
private fun SaleItemRow(
    item: SellUiState.ItemRow,
    onPriceChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
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
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                OutlinedTextField(
                    value = if (item.sellingPrice == 0.0) "" else item.sellingPrice.toString(),
                    onValueChange = { 
                        val price = it.toDoubleOrNull() ?: 0.0
                        onPriceChange(price)
                    },
                    modifier = Modifier.width(100.dp).height(50.dp),
                    prefix = { Text("$", fontSize = 12.sp) },
                    placeholder = { Text("0.00", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = PlaceholderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SaleExpenseRow(expense: SellUiState.ExpenseRow, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(expense.name, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("-$${String.format(Locale.US, "%.2f", expense.amount)}", color = ErrorRed, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp).padding(start = 8.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = PlaceholderColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(subtitle, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemSelectorSheet(
    itemsWithStock: List<com.palletflow.presentation.screens.sell.ItemWithStock>,
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
            modifier = Modifier.fillMaxHeight(0.9f).padding(16.dp)
        ) {
            Text("Add Item", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            Column(modifier = Modifier.weight(1f)) {
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
                                    onClick = { onAddItem(item, qty.toIntOrNull() ?: 0) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseSheet(onDismiss: () -> Unit, onAddExpense: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.5f).padding(16.dp)
        ) {
            Text("Add Expense", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expense Name") },
                placeholder = { Text("e.g. Shipping, Fee") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = PlaceholderColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                prefix = { Text("$") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = PlaceholderColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { onAddExpense(name, amount.toDoubleOrNull() ?: 0.0) },
                enabled = name.isNotBlank() && amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, disabledContainerColor = PlaceholderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Expense", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}
