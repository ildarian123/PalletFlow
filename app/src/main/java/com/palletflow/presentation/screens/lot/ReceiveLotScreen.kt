package com.palletflow.presentation.screens.lot

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.palletflow.data.local.entity.Item
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palletflow.data.local.entity.Supplier
import com.palletflow.data.local.entity.Warehouse
import kotlinx.coroutines.delay
import java.util.*

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)
private val ErrorRed = Color(0xFFFF453A)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReceiveLotScreen(
    onBackClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    onAddNewSupplierClick: () -> Unit = {},
    onCreateNewItemClick: () -> Unit = {},
    viewModel: ReceiveLotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val warehouses by viewModel.warehouses.collectAsStateWithLifecycle()
    val availableItems by viewModel.availableItems.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { success ->
            if (success) onCompleteClick()
        }
    }

    ReceiveLotContent(
        uiState = uiState,
        suppliers = suppliers,
        warehouses = warehouses,
        availableItems = availableItems,
        onBackClick = onBackClick,
        onCompleteClick = viewModel::complete,
        onAddNewSupplierClick = onAddNewSupplierClick,
        onCreateNewItemClick = onCreateNewItemClick,
        onPurchasePriceChange = viewModel::onPurchasePriceChange,
        onNotesChange = viewModel::onNotesChange,
        onSupplierChange = viewModel::onSupplierChange,
        onWarehouseChange = viewModel::onWarehouseChange,
        onAddItem = viewModel::addItem,
        onItemPriceChange = viewModel::onItemPriceChange,
        onRemoveItem = viewModel::removeItem,
        onAddExpense = viewModel::addExpense,
        onRemoveExpense = viewModel::removeExpense,
        onOpenAddSupplierSheet = viewModel::openAddSupplierSheet,
        onCloseAddSupplierSheet = viewModel::closeAddSupplierSheet,
        onSupplierCreated = viewModel::onSupplierCreated
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ReceiveLotContent(
    uiState: ReceiveLotUiState,
    suppliers: List<Supplier>,
    warehouses: List<Warehouse>,
    availableItems: List<Item>,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onAddNewSupplierClick: () -> Unit,
    onCreateNewItemClick: () -> Unit,
    onPurchasePriceChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSupplierChange: (String) -> Unit,
    onWarehouseChange: (String) -> Unit,
    onAddItem: (String, Int) -> Unit,
    onItemPriceChange: (Int, String) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onAddExpense: (String, Double) -> Unit,
    onRemoveExpense: (Int) -> Unit,
    onOpenAddSupplierSheet: () -> Unit,
    onCloseAddSupplierSheet: () -> Unit,
    onSupplierCreated: (Supplier) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isHeaderExpanded by remember { mutableStateOf(true) }
    var showSupplierSheet by remember { mutableStateOf(false) }
    var showWarehouseSheet by remember { mutableStateOf(false) }
    var showItemSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.supplierError, uiState.warehouseError, uiState.priceError, uiState.itemError) {
        val error = uiState.error 
            ?: uiState.supplierError 
            ?: uiState.warehouseError 
            ?: uiState.priceError 
            ?: uiState.itemError
            
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Receive New Lot", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        TextButton(onClick = onCompleteClick) {
                            Text("Complete", color = AccentTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceColor,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Cost", color = TextSecondary, fontSize = 12.sp)
                        Text("$${String.format(Locale.US, "%.2f", uiState.totalCost)}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }

                    Button(
                        onClick = {
                            if (selectedTab == 0) showItemSheet = true else showExpenseSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (selectedTab == 0) "Add Item" else "Add Expense", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Section
                item {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lot Info", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // Lot Number (Always visible)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Lot Number", color = TextSecondary, fontSize = 12.sp)
                                    Text(uiState.lotNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Surface(color = AccentTeal, shape = RoundedCornerShape(4.dp)) {
                                    Text("Auto", color = DarkBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(visible = isHeaderExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    SelectorRow(
                                        label = "Supplier",
                                        value = suppliers.find { it.id == uiState.supplierId }?.name ?: "Select Supplier",
                                        icon = Icons.Default.Business,
                                        isError = uiState.supplierError != null,
                                        onClick = { showSupplierSheet = true }
                                    )

                                    TextButton(
                                        onClick = onOpenAddSupplierSheet,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = Color(0xFF00C2A0), modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add New Supplier", color = Color(0xFF00C2A0))
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    SelectorRow(
                                        label = "Warehouse",
                                        value = warehouses.find { it.id == uiState.warehouseId }?.name ?: "Select Warehouse",
                                        icon = Icons.Default.Warehouse,
                                        isError = uiState.warehouseError != null,
                                        onClick = { showWarehouseSheet = true }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("Purchase Price", color = TextSecondary, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = uiState.purchasePrice,
                                        onValueChange = onPurchasePriceChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = uiState.priceError != null,
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
                                        value = uiState.notes,
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
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                IconButton(onClick = { isHeaderExpanded = !isHeaderExpanded }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = if (isHeaderExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Header",
                                        tint = AccentTeal
                                    )
                                }
                            }
                        }
                    }
                }

                // Tabs Section
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
                            text = { Text("Items", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Expenses", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // Tab Content
                if (selectedTab == 0) {
                    if (uiState.items.isEmpty()) {
                        item { 
                            EmptyTabState(
                                icon = Icons.Default.Inventory, 
                                title = "No items added yet", 
                                subtitle = "Tap + to add items from your inventory",
                                isError = uiState.itemError != null
                            ) 
                        }
                    } else {
                        itemsIndexed(uiState.items) { index, item ->
                            LotItemRow(
                                item = item,
                                onPriceChange = { onItemPriceChange(index, it) },
                                onRemove = { onRemoveItem(index) }
                            )
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Expenses:", color = TextSecondary)
                            Text("$${String.format(Locale.US, "%.2f", uiState.expenses.sumOf { it.amount })}", color = AccentTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (uiState.expenses.isEmpty()) {
                        item { EmptyTabState(Icons.Default.Receipt, "No extra expenses", "Tap + to add shipping, labor or other costs") }
                    } else {
                        itemsIndexed(uiState.expenses) { index, expense ->
                            ExpenseRow(expense, onRemove = { onRemoveExpense(index) })
                        }
                    }
                }
            }

            // Bottom Sheets
            if (showSupplierSheet) {
                SupplierSelectorSheet(
                    suppliers = suppliers,
                    onDismiss = { showSupplierSheet = false },
                    onSupplierSelected = {
                        onSupplierChange(it)
                        showSupplierSheet = false
                    },
                    onAddNewSupplierClick = onAddNewSupplierClick
                )
            }

            if (showWarehouseSheet) {
                WarehouseSelectorSheet(
                    warehouses = warehouses,
                    onDismiss = { showWarehouseSheet = false },
                    onWarehouseSelected = {
                        onWarehouseChange(it)
                        showWarehouseSheet = false
                    }
                )
            }

            if (showItemSheet) {
                ItemSelectorSheet(
                    availableItems = availableItems,
                    onDismiss = { showItemSheet = false },
                    onCreateNewItemClick = onCreateNewItemClick,
                    onItemAdded = { itemId, qty ->
                        onAddItem(itemId, qty)
                    }
                )
            }

            if (showExpenseSheet) {
                AddExpenseSheet(
                    onDismiss = { showExpenseSheet = false },
                    onAddExpense = { name, amount ->
                        onAddExpense(name, amount)
                        showExpenseSheet = false
                    }
                )
            }

            if (uiState.showAddSupplierSheet) {
                com.palletflow.presentation.common.AddSupplierSheet(
                    onDismiss = onCloseAddSupplierSheet,
                    onSupplierCreated = onSupplierCreated
                )
            }
        }
    }
}

@Composable
private fun EmptyTabState(icon: ImageVector, title: String, subtitle: String, isError: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            modifier = Modifier.size(48.dp), 
            tint = if (isError) ErrorRed else PlaceholderColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, color = if (isError) ErrorRed else TextSecondary)
        Text(subtitle, color = if (isError) ErrorRed.copy(alpha = 0.7f) else TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun LotItemRow(
    item: ReceiveLotUiState.ItemRow,
    onPriceChange: (String) -> Unit,
    onRemove: () -> Unit
) {
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
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                if (item.photoUrl != null) {
                    AsyncImage(
                        model = item.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
                }
            }
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(item.itemName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Quantity: ${item.quantity}", color = TextSecondary, fontSize = 12.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                OutlinedTextField(
                    value = item.price,
                    onValueChange = onPriceChange,
                    modifier = Modifier.width(90.dp),
                    placeholder = { Text("Price", color = TextSecondary, fontSize = 12.sp) },
                    prefix = { Text("$", color = AccentTeal, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = PlaceholderColor
                    )
                )
                
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp).padding(top = 4.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: ReceiveLotUiState.ExpenseRow, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentTeal.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(expense.expenseName, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$${String.format(Locale.US, "%.2f", expense.amount)}", color = TextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp).padding(start = 8.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun SelectorRow(label: String, value: String, icon: ImageVector, isError: Boolean = false, onClick: () -> Unit) {
    Column {
        Text(label, color = if (isError) ErrorRed else TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PlaceholderColor)
                .border(1.dp, if (isError) ErrorRed else Color.Transparent, RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isError) ErrorRed else TextSecondary, modifier = Modifier.size(20.dp))
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
    suppliers: List<Supplier>,
    onDismiss: () -> Unit,
    onSupplierSelected: (String) -> Unit,
    onAddNewSupplierClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.8f).padding(16.dp)) {
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
            
            val filteredSuppliers = remember(searchQuery, suppliers) {
                suppliers.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredSuppliers) { supplier ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSupplierSelected(supplier.id.toString()) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = AccentTeal)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(supplier.name, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onAddNewSupplierClick,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
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
    warehouses: List<Warehouse>,
    onDismiss: () -> Unit,
    onWarehouseSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.6f).padding(16.dp)) {
            Text("Select Warehouse", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(warehouses) { warehouse ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onWarehouseSelected(warehouse.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warehouse, contentDescription = null, tint = AccentTeal)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(warehouse.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(warehouse.address ?: "", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectorSheet(
    availableItems: List<Item>,
    onDismiss: () -> Unit,
    onCreateNewItemClick: () -> Unit,
    onItemAdded: (String, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.9f).padding(16.dp)) {
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

            val filteredItems = remember(searchQuery, availableItems) {
                availableItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onItemAdded(item.id, 1)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PlaceholderColor)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceColor), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(item.type.name, color = TextSecondary, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCreateNewItemClick,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal),
                border = BorderStroke(1.dp, AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create New Item")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onDismiss: () -> Unit,
    onAddExpense: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PlaceholderColor) }
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.6f).padding(16.dp)) {
            Text("Add Expense", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expense Name") },
                placeholder = { Text("e.g. Shipping") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.replace(".", "").all { char -> char.isDigit() }) amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                prefix = { Text("$", color = AccentTeal) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && a > 0) {
                        onAddExpense(name, a)
                    }
                },
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Expense", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiveLotScreenPreview() {
    val mockItems = listOf(
        ReceiveLotUiState.ItemRow("1", "Pallet A", 10),
        ReceiveLotUiState.ItemRow("2", "Box B", 5)
    )
    val mockExpenses = listOf(
        ReceiveLotUiState.ExpenseRow("Shipping", 50.0),
        ReceiveLotUiState.ExpenseRow("Handling", 20.0)
    )
    val mockUiState = ReceiveLotUiState(
        lotNumber = "LOT-2024-05-20-1234",
        supplierId = "1",
        warehouseId = "1",
        purchasePrice = "500.00",
        items = mockItems,
        expenses = mockExpenses
    )
    
    val mockSuppliers = listOf(
        Supplier(id = "1", userId = "user123", name = "Global Supplies", contactPerson = "John Doe", email = null, phone = null, address = null)
    )
    val mockWarehouses = listOf(
        Warehouse(id = "1", userId = "user123", name = "Main Warehouse", address = "123 Industrial Way")
    )
    
    MaterialTheme {
        ReceiveLotContent(
            uiState = mockUiState,
            suppliers = mockSuppliers,
            warehouses = mockWarehouses,
            availableItems = emptyList(),
            onBackClick = {},
            onCompleteClick = {},
            onAddNewSupplierClick = {},
            onCreateNewItemClick = {},
            onPurchasePriceChange = {},
            onNotesChange = {},
            onSupplierChange = {},
            onWarehouseChange = {},
            onAddItem = { _, _ -> },
            onItemPriceChange = { _, _ -> },
            onRemoveItem = {},
            onAddExpense = { _, _ -> },
            onRemoveExpense = {},
            onOpenAddSupplierSheet = {},
            onCloseAddSupplierSheet = {},
            onSupplierCreated = {}
        )
    }
}
