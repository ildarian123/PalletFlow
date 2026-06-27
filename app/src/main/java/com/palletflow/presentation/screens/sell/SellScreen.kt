package com.palletflow.presentation.screens.sell

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
private val PlatformPurple = Color(0xFF9B59B6)

data class SaleItemData(
    val name: String,
    val lotRef: String,
    val condition: String,
    val available: Int,
    val qty: Int,
    val price: String,
    val cost: String,
    val profit: String,
    val conditionColor: Color
)

data class SaleExpenseData(
    val type: String,
    val notes: String,
    val amount: String,
    val color: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SellScreen(
    onBackClick: () -> Unit = {},
    onSaveDraftClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPlatform by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    
    var showItemSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }
    var showCustomerSheet by remember { mutableStateOf(false) }

    val selectedItems = remember {
        mutableStateListOf(
            SaleItemData("Nike Air Max 90", "Lot #1042", "Used", 5, 2, "$45.00", "$12.00", "+$33.00", WarningAmber),
            SaleItemData("Samsung Tablet", "Lot #1041", "New", 2, 1, "$120.00", "$45.00", "+$75.00", AccentTeal)
        )
    }

    val selectedExpenses = remember {
        mutableStateListOf(
            SaleExpenseData("Shipping", "FedEx to buyer", "-$12.00", InfoBlue, Icons.Default.LocalShipping),
            SaleExpenseData("Platform Fee", "eBay 13%", "-$30.00", PlatformPurple, Icons.Default.Percent)
        )
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
                    SaleInfoHeader(
                        selectedPlatform = selectedPlatform,
                        onPlatformSelect = { selectedPlatform = it },
                        onCustomerClick = { showCustomerSheet = true },
                        notes = notes,
                        onNotesChange = { notes = it }
                    )
                }

                // Sticky Tabs
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
                    if (selectedItems.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.ShoppingCart,
                                title = "No items added",
                                subtitle = "Tap + to add items from inventory"
                            )
                        }
                    } else {
                        itemsIndexed(selectedItems) { _, item ->
                            SaleItemRow(item, onRemove = { selectedItems.remove(item) })
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
                            Text("-$42.00", color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (selectedExpenses.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.Receipt,
                                title = "No expenses added",
                                subtitle = "Tap + to add shipping, fees or other costs"
                            )
                        }
                    } else {
                        items(selectedExpenses) { expense ->
                            SaleExpenseRow(expense, onRemove = { selectedExpenses.remove(expense) })
                        }
                    }
                }
            }

            // Bottom Sheets
            if (showItemSheet) {
                ItemSelectorSheet(
                    onDismiss = { showItemSheet = false },
                    onAddSelectedItems = { showItemSheet = false }
                )
            }

            if (showExpenseSheet) {
                AddExpenseSheet(
                    onDismiss = { showExpenseSheet = false },
                    onAddExpense = { showExpenseSheet = false }
                )
            }

            if (showCustomerSheet) {
                CustomerSelectorSheet(
                    onDismiss = { showCustomerSheet = false }
                )
            }
        }
    }
}

@Composable
private fun SaleInfoHeader(
    selectedPlatform: Int,
    onPlatformSelect: (Int) -> Unit,
    onCustomerClick: () -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sale Number", color = TextSecondary, fontSize = 12.sp)
                    Text("SALE-2024-001", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Surface(color = AccentTeal, shape = RoundedCornerShape(4.dp)) {
                    Text("Auto", color = DarkBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Platform Selector
            Text("Platform", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val platforms = listOf(
                "eBay" to Icons.Default.ShoppingCart,
                "Facebook" to Icons.Default.People,
                "Cash" to Icons.Default.AttachMoney,
                "Bin Store" to Icons.Default.Store,
                "Other" to Icons.Default.MoreHoriz
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(platforms) { index, (name, icon) ->
                    FilterChip(
                        selected = selectedPlatform == index,
                        onClick = { onPlatformSelect(index) },
                        label = { Text(name) },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentTeal,
                            selectedLabelColor = DarkBackground,
                            selectedLeadingIconColor = DarkBackground,
                            containerColor = PlaceholderColor,
                            labelColor = TextSecondary,
                            iconColor = TextSecondary
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Row
            SelectorRow(
                label = "Date",
                value = "Jun 27, 2024",
                icon = Icons.Default.CalendarToday,
                onClick = { /* Open DatePicker */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Row
            Text("Customer", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlaceholderColor)
                    .clickable { onCustomerClick() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add Customer (Optional)", color = TextSecondary, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
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

            Divider(color = PlaceholderColor, modifier = Modifier.padding(vertical = 12.dp))

            SummaryMetricRow("Items Total:", "$165.00", TextPrimary)
            SummaryMetricRow("Expenses:", "-$42.00", ErrorRed)
            SummaryMetricRow("Net Revenue:", "$123.00", AccentTeal, isLarge = true)
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
private fun SelectorRow(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
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
            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun SaleItemRow(item: SaleItemData, onRemove: () -> Unit) {
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
                Text(item.lotRef, color = TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = item.conditionColor, shape = RoundedCornerShape(4.dp)) {
                        Text(item.condition, color = DarkBackground, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                    Text("Available: ${item.available}", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("x${item.qty}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(item.price, color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Cost: ${item.cost}", color = TextSecondary, fontSize = 10.sp)
                Text(item.profit, color = AccentTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SaleExpenseRow(expense: SaleExpenseData, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(expense.color), contentAlignment = Alignment.Center) {
                Icon(expense.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(expense.type, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(expense.notes, color = TextSecondary, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(expense.amount, color = ErrorRed, fontWeight = FontWeight.Bold)
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
private fun ItemSelectorSheet(onDismiss: () -> Unit, onAddSelectedItems: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    val items = remember {
        listOf(
            "Nike Air Max 90" to ("Lot #1042" to 5),
            "Samsung Tablet" to ("Lot #1041" to 2),
            "Bluetooth Speaker" to ("Lot #1042" to 8),
            "Levi's Jeans" to ("Lot #1043" to 3),
            "Coffee Maker" to ("Lot #1040" to 1)
        )
    }

    LaunchedEffect(searchQuery) {
        delay(300)
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
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(items.filter { it.first.contains(debouncedQuery, ignoreCase = true) }) { (name, info) ->
                    var price by remember { mutableStateOf("") }
                    var qty by remember { mutableStateOf(TextFieldValue("")) }
                    
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
                                Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(info.first, color = TextSecondary, fontSize = 11.sp)
                                Text("Available: ${info.second}", color = AccentTeal, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    modifier = Modifier.width(90.dp).height(50.dp),
                                    placeholder = { Text("0.00", fontSize = 12.sp) },
                                    prefix = { Text("$", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = SurfaceColor, focusedContainerColor = SurfaceColor, unfocusedContainerColor = SurfaceColor)
                                )
                                OutlinedTextField(
                                    value = qty,
                                    onValueChange = { qty = it },
                                    modifier = Modifier.width(70.dp).height(50.dp).onFocusChanged { 
                                        if (it.isFocused) qty = qty.copy(selection = TextRange(0, qty.text.length)) 
                                    },
                                    placeholder = { Text("Qty", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = SurfaceColor, focusedContainerColor = SurfaceColor, unfocusedContainerColor = SurfaceColor)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = PlaceholderColor)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddSelectedItems,
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Selected Items", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseSheet(onDismiss: () -> Unit, onAddExpense: () -> Unit) {
    var selectedType by remember { mutableIntStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val types = listOf("Shipping", "Platform Fee", "Packaging", "Other")

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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(types) { index, type ->
                    FilterChip(
                        selected = selectedType == index,
                        onClick = { selectedType = index },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentTeal, selectedLabelColor = DarkBackground, containerColor = PlaceholderColor, labelColor = TextSecondary),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Amount", color = TextSecondary, fontSize = 12.sp)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = PlaceholderColor, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("Notes", color = TextSecondary, fontSize = 12.sp)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optional notes...") },
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = PlaceholderColor, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddExpense,
                enabled = amount.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, disabledContainerColor = PlaceholderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Expense", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerSelectorSheet(onDismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val customers = listOf("John Smith", "Maria Garcia", "Bob Johnson")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Add Customer", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Optional — for record keeping", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search customers...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentTeal, unfocusedBorderColor = PlaceholderColor, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
            )
            Spacer(modifier = Modifier.height(16.dp))
            customers.forEach { customer ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onDismiss() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceColor), contentAlignment = Alignment.Center) {
                            Text(customer.take(1), color = AccentTeal, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(customer, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = PlaceholderColor)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = BorderStroke(1.dp, TextSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Skip — No Customer")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SellScreenPreview() {
    SellScreen()
}
