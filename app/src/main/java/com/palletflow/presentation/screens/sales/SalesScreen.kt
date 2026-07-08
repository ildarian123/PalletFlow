package com.palletflow.presentation.screens.sales

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionStatus
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.presentation.common.TransactionContextMenu
import java.text.SimpleDateFormat
import java.util.*

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val BorderColor = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onAddLotClick: () -> Unit,
    onEditLotClick: (String) -> Unit,
    onAddSaleClick: () -> Unit,
    onAddTransferClick: () -> Unit,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.deleteEvent.collect { event ->
            when (event) {
                is SalesViewModel.DeleteEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                SalesViewModel.DeleteEvent.Success -> {
                    // List updates automatically via Flow
                }
            }
        }
    }

    SalesScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAddLotClick = onAddLotClick,
        onEditLotClick = onEditLotClick,
        onAddSaleClick = onAddSaleClick,
        onAddTransferClick = onAddTransferClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDateFilterChange = viewModel::onDateFilterChange,
        onTabChange = viewModel::onTabChange,
        onDeleteTransaction = viewModel::deleteTransaction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreenContent(
    uiState: SalesUiState,
    snackbarHostState: SnackbarHostState,
    onAddLotClick: () -> Unit,
    onEditLotClick: (String) -> Unit,
    onAddSaleClick: () -> Unit,
    onAddTransferClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDateFilterChange: (DateFilter) -> Unit,
    onTabChange: (Int) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    var expandedFab by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (expandedFab) {
                    FabSubmenuItem("Receive Lot", Icons.Default.CallReceived, onAddLotClick)
                    Spacer(modifier = Modifier.height(12.dp))
                    FabSubmenuItem("Sell Item", Icons.Default.Sell, onAddSaleClick)
                    Spacer(modifier = Modifier.height(12.dp))
                    FabSubmenuItem("Move Inventory", Icons.Default.LocalShipping, onAddTransferClick)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                FloatingActionButton(
                    onClick = { expandedFab = !expandedFab },
                    containerColor = AccentTeal,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        if (expandedFab) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add"
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
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search transactions...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor,
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = BorderColor
                ),
                singleLine = true
            )

            // Date Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterChip("Today", DateFilter.TODAY, uiState.selectedDateFilter, onDateFilterChange)
                DateFilterChip("This Week", DateFilter.WEEK, uiState.selectedDateFilter, onDateFilterChange)
                DateFilterChip("This Month", DateFilter.MONTH, uiState.selectedDateFilter, onDateFilterChange)
                DateFilterChip("All Time", DateFilter.CUSTOM, uiState.selectedDateFilter, onDateFilterChange)
            }

            // TabRow
            SecondaryTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = DarkBackground,
                contentColor = AccentTeal,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(uiState.selectedTab),
                        color = AccentTeal
                    )
                },
                divider = { HorizontalDivider(color = BorderColor) }
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { onTabChange(0) },
                    text = { Text("Incoming") },
                    icon = { Icon(Icons.Default.CallReceived, null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { onTabChange(1) },
                    text = { Text("Sales") },
                    icon = { Icon(Icons.Default.Sell, null) }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { onTabChange(2) },
                    text = { Text("Transfers") },
                    icon = { Icon(Icons.Default.SwapHoriz, null) }
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentTeal)
                }
            } else if (uiState.filteredTransactions.isEmpty()) {
                EmptyState(uiState.selectedTab)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredTransactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onClick = {
                                if (transaction.type == TransactionType.INCOMING && transaction.lotId != null) {
                                    onEditLotClick(transaction.lotId)
                                }
                            },
                            onLongClick = {
                                selectedTransaction = transaction
                                showContextMenu = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showContextMenu && selectedTransaction != null) {
        TransactionContextMenu(
            transaction = selectedTransaction!!,
            onDismiss = { showContextMenu = false; selectedTransaction = null },
            onViewDetailsClick = { /* TODO */ },
            onEditClick = {
                val transaction = selectedTransaction!!
                if (transaction.type == TransactionType.INCOMING && transaction.lotId != null) {
                    onEditLotClick(transaction.lotId)
                }
                showContextMenu = false
                selectedTransaction = null
            },
            onDeleteClick = onDeleteTransaction
        )
    }
}

@Composable
fun DateFilterChip(
    label: String,
    filter: DateFilter,
    selectedFilter: DateFilter,
    onClick: (DateFilter) -> Unit
) {
    FilterChip(
        selected = filter == selectedFilter,
        onClick = { onClick(filter) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentTeal,
            selectedLabelColor = Color.White,
            containerColor = SurfaceColor,
            labelColor = TextSecondary
        ),
        border = null,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun FabSubmenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            containerColor = SurfaceColor,
            contentColor = AccentTeal,
            shape = CircleShape
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionCard(transaction: Transaction, onClick: () -> Unit = {}, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (transaction.type) {
                TransactionType.INCOMING -> Icons.Default.CallReceived
                TransactionType.SALE -> Icons.Default.Sell
                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentTeal.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AccentTeal, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.notes ?: "No description",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(transaction.createdAt)),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (transaction.type == TransactionType.SALE) "+$${String.format("%.2f", transaction.amount)}" else "${transaction.quantity} units",
                    color = if (transaction.type == TransactionType.SALE) AccentTeal else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = transaction.status.name,
                    color = when(transaction.status) {
                        TransactionStatus.COMPLETED -> AccentTeal
                        TransactionStatus.DRAFT -> Color.Gray
                        else -> TextSecondary
                    },
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun EmptyState(selectedTab: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (icon, text) = when (selectedTab) {
            0 -> Icons.Default.CallReceived to "No incoming lots yet. Tap + to receive a lot."
            1 -> Icons.Default.Sell to "No sales yet. Tap + to record a sale."
            else -> Icons.Default.SwapHoriz to "No transfers yet. Tap + to move items."
        }
        
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = TextSecondary.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SalesScreenPreview() {
    SalesScreenContent(
        uiState = SalesUiState(
            filteredTransactions = listOf(
                Transaction(
                    userId = "1",
                    type = TransactionType.SALE,
                    amount = 150.0,
                    notes = "Sold 5 items",
                    createdAt = System.currentTimeMillis()
                ),
                Transaction(
                    userId = "1",
                    type = TransactionType.INCOMING,
                    quantity = 20,
                    notes = "Received new shipment",
                    createdAt = System.currentTimeMillis() - 86400000
                )
            ),
            isLoading = false
        ),
        snackbarHostState = remember { SnackbarHostState() },
        onAddLotClick = {},
        onAddSaleClick = {},
        onAddTransferClick = {},
        onEditLotClick = {},
        onSearchQueryChange = {},
        onDateFilterChange = {},
        onTabChange = {},
        onDeleteTransaction = {}
    )
}
