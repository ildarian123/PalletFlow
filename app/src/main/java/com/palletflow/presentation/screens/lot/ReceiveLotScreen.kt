package com.palletflow.presentation.screens.lot

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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
private val InfoBlue = Color(0xFF4A90E2)
private val WarningAmber = Color(0xFFFF9500)
private val FuelPurple = Color(0xFF9B59B6)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReceiveLotScreen(
    onBackClick: () -> Unit = {},
    onSaveDraftClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    onAddNewSupplierClick: () -> Unit = {},
    onCreateNewItemClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var purchasePrice by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showSupplierSheet by remember { mutableStateOf(false) }
    var showWarehouseSheet by remember { mutableStateOf(false) }
    var showItemSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }

    var selectedSupplier by remember { mutableStateOf<String?>(null) }
    var selectedWarehouse by remember { mutableStateOf<String?>(null) }

    val items = remember {
        mutableStateListOf(
            LotItemData("Nike Air Max 90", "Footwear", "Used", "x3", "$45.00", WarningAmber),
            LotItemData("Samsung Tablet", "Electronics", "New", "x1", "$120.00", AccentTeal),
            LotItemData("Bluetooth Speaker", "Electronics", "Damaged", "x2", "$15.00", ErrorRed)
        )
    }

    val expenses = remember {
        mutableStateListOf(
            ExpenseData("Shipping", "FedEx delivery", "-$80.00", InfoBlue, Icons.Default.LocalShipping),
            ExpenseData("Labor", "Unloading help", "-$50.00", WarningAmber, Icons.Default.Person)
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Receive New Lot", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = onSaveDraftClick) {
                        Text("Save Draft", color = TextSecondary)
                    }
                    TextButton(onClick = onCompleteClick) {
                        Text("Complete", color = AccentTeal, fontWeight = FontWeight.Bold)
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
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header Section
                item {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Lot Info", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Lot Number
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Lot Number", color = TextSecondary, fontSize = 12.sp)
                                    Text("LOT-2024-001", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Surface(
                                    color = AccentTeal,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "Auto",
                                        color = DarkBackground,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            SelectorRow(
                                label = "Supplier",
                                value = selectedSupplier ?: "Select Supplier",
                                icon = Icons.Default.Business,
                                onClick = { showSupplierSheet = true }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            SelectorRow(
                                label = "Warehouse",
                                value = selectedWarehouse ?: "Select Warehouse",
                                icon = Icons.Default.Warehouse,
                                onClick = { showWarehouseSheet = true }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            SelectorRow(
                                label = "Date",
                                value = "Jun 27, 2024",
                                icon = Icons.Default.CalendarToday,
                                onClick = { /* Show DatePicker */ }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Purchase Price", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = purchasePrice,
                                onValueChange = { purchasePrice = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("0.00", color = TextSecondary) },
                                prefix = { Text("$", color = AccentTeal) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentTeal,
                                    unfocusedBorderColor = PlaceholderColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Optional notes...", color = TextSecondary) },
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentTeal,
                                    unfocusedBorderColor = PlaceholderColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                // Tabs Section - Sticky Header
                stickyHeader {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkBackground,
                        contentColor = AccentTeal,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
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
                    if (items.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(48.dp), tint = PlaceholderColor)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No items added yet", color = TextSecondary)
                                Text("Tap + to add items from your inventory", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(items) { item ->
                            LotItemRow(item, onRemove = { items.remove(item) })
                        }
                    }
                } else {
                    item {
                        val totalExpenses = expenses.sumOf {
                            it.amount.replace("$", "").replace("-", "").replace("+", "").toDoubleOrNull() ?: 0.0
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Expenses:", color = TextSecondary)
                            Text("+$${String.format("%.2f", totalExpenses)}", color = AccentTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (expenses.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(48.dp), tint = PlaceholderColor)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No extra expenses", color = TextSecondary)
                                Text("Tap + to add shipping, labor or other costs", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(expenses) { expense ->
                            ExpenseRow(expense, onRemove = { expenses.remove(expense) })
                        }
                    }
                }
            }

            // Bottom Sheets
            if (showSupplierSheet) {
                SupplierSelectorSheet(
                    onDismiss = { showSupplierSheet = false },
                    onSupplierSelected = {
                        selectedSupplier = it
                        showSupplierSheet = false
                    },
                    onAddNewSupplierClick = onAddNewSupplierClick
                )
            }

            if (showWarehouseSheet) {
                WarehouseSelectorSheet(
                    onDismiss = { showWarehouseSheet = false },
                    onWarehouseSelected = {
                        selectedWarehouse = it
                        showWarehouseSheet = false
                    }
                )
            }

            if (showItemSheet) {
                ItemSelectorSheet(
                    onDismiss = { showItemSheet = false },
                    onCreateNewItemClick = onCreateNewItemClick,
                    onAddSelectedItems = { showItemSheet = false }
                )
            }

            if (showExpenseSheet) {
                AddExpenseSheet(
                    onDismiss = { showExpenseSheet = false },
                    onAddExpense = { showExpenseSheet = false }
                )
            }
        }
    }
}

@Composable
private fun LotItemRow(item: LotItemData, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(PlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(color = PlaceholderColor, shape = RoundedCornerShape(4.dp)) {
                        Text(item.category, color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                    Surface(color = item.conditionColor, shape = RoundedCornerShape(4.dp)) {
                        Text(item.condition, color = DarkBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.qty, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(item.price, color = TextSecondary, fontSize = 11.sp)
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: ExpenseData, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(expense.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(expense.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(expense.type, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(expense.notes, color = TextSecondary, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(expense.amount, color = TextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp).padding(start = 8.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SelectorRow(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
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
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierSelectorSheet(
    onDismiss: () -> Unit,
    onSupplierSelected: (String) -> Unit,
    onAddNewSupplierClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        delay(1000)
        debouncedQuery = searchQuery
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Select Supplier", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search suppliers...", color = TextSecondary) },
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
            val allSuppliers = listOf("Amazon Returns", "BULQ", "Direct Liquidation", "B-Stock")
            val filteredSuppliers = remember(debouncedQuery) {
                allSuppliers.filter { it.contains(debouncedQuery, ignoreCase = true) }
            }
            filteredSuppliers.forEach { supplier ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSupplierSelected(supplier) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = AccentTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(supplier, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = PlaceholderColor)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onAddNewSupplierClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal),
                border = BorderStroke(1.dp, AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add New Supplier")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseSelectorSheet(
    onDismiss: () -> Unit,
    onWarehouseSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Select Warehouse", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            val warehouses = listOf(
                "Main Warehouse" to "Chicago, IL",
                "Storage Unit 2" to "Chicago, IL",
                "Retail Location" to "Evanston, IL"
            )
            warehouses.forEach { (name, location) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onWarehouseSelected(name) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, tint = AccentTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(location, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectorSheet(
    onDismiss: () -> Unit,
    onCreateNewItemClick: () -> Unit,
    onAddSelectedItems: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        delay(1000)
        debouncedQuery = searchQuery
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Add Item", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search items...", color = TextSecondary) },
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
                "Nike Air Max 90" to "Footwear",
                "Samsung Tablet" to "Electronics",
                "Bluetooth Speaker" to "Electronics",
                "Levi's Jeans" to "Clothing",
                "Coffee Maker" to "Appliances"
            )
            val filteredItems = remember(debouncedQuery) {
                allItems.filter { it.first.contains(debouncedQuery, ignoreCase = true) }
            }

            // Map to track quantity per item name
            val quantities = remember { mutableStateMapOf<String, TextFieldValue>() }

            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(filteredItems) { (name, cat) ->
                    val textFieldValue = quantities[name] ?: TextFieldValue("")
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceColor), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(cat, color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                OutlinedTextField(
                                    value = textFieldValue,
                                    onValueChange = { newValue ->
                                        if (newValue.text.isEmpty() || newValue.text.all { it.isDigit() }) {
                                            quantities[name] = newValue
                                        }
                                    },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(60.dp)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                val text = quantities[name]?.text ?: ""
                                                quantities[name] = TextFieldValue(
                                                    text = text,
                                                    selection = TextRange(0, text.length)
                                                )
                                            }
                                        },
                                    placeholder = {
                                        Text(
                                            "0",
                                            color = TextSecondary,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        color = TextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
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
            OutlinedButton(
                onClick = onCreateNewItemClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal),
                border = BorderStroke(1.dp, AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create New Item")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddSelectedItems,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Selected", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onDismiss: () -> Unit,
    onAddExpense: () -> Unit
) {
    var selectedType by remember { mutableIntStateOf(0) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Add Expense", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Type", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val types = listOf("Shipping", "Labor", "Fuel", "Other")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEachIndexed { index, type ->
                    FilterChip(
                        selected = selectedType == index,
                        onClick = { selectedType = index },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentTeal,
                            selectedLabelColor = DarkBackground,
                            containerColor = PlaceholderColor,
                            labelColor = TextSecondary
                        ),
                        border = null,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    if (newValue.text.isEmpty() || newValue.text.replace(".", "").all { it.isDigit() }) {
                        amount = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            amount = amount.copy(selection = TextRange(0, amount.text.length))
                        }
                    },
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                prefix = { Text("$", color = AccentTeal) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = PlaceholderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optional notes...", color = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = PlaceholderColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Expense", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class LotItemData(val name: String, val category: String, val condition: String, val qty: String, val price: String, val conditionColor: Color)
private data class ExpenseData(val type: String, val notes: String, val amount: String, val color: Color, val icon: ImageVector)

@Preview(showBackground = true)
@Composable
fun ReceiveLotScreenPreview() {
    ReceiveLotScreen()
}
