package com.palletflow.presentation.screens.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)
private val NegativeRed = Color(0xFFFF453A)
private val LotBlue = Color(0xFF4A90E2)

enum class TransactionType {
    Sale, Lot, Expense
}

data class TransactionItem(
    val title: String,
    val subtitle: String,
    val amount: String,
    val time: String,
    val type: TransactionType,
    val date: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SalesScreen(
    onTransactionClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDateIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("All", "Sales", "Lots", "Expenses")
    val dateFilters = listOf("Today", "This Week", "This Month", "Custom")

    val transactions = remember {
        listOf(
            // Today
            TransactionItem("Sold Samsung Tablet", "Facebook Marketplace • Storage 2", "+$120.00", "2:15 PM", TransactionType.Sale, "Today"),
            TransactionItem("Sold Nike Air Max", "eBay • Main Warehouse", "+$45.00", "10:30 AM", TransactionType.Sale, "Today"),
            TransactionItem("Shipping Cost", "FedEx delivery", "-$12.00", "9:00 AM", TransactionType.Expense, "Today"),
            
            // Yesterday
            TransactionItem("Received Lot #1043", "Amazon Returns • Main Warehouse", "-$850.00", "3:00 PM", TransactionType.Lot, "Yesterday"),
            TransactionItem("Sold Bluetooth Speaker", "Cash • Main Warehouse", "+$35.00", "1:20 PM", TransactionType.Sale, "Yesterday"),
            TransactionItem("Storage Rental", "Monthly fee", "-$200.00", "8:00 AM", TransactionType.Expense, "Yesterday"),
            
            // Jun 20
            TransactionItem("Sold iPad Mini", "eBay • Main Warehouse", "+$180.00", "4:45 PM", TransactionType.Sale, "Jun 20"),
            TransactionItem("Received Lot #1042", "BULQ • Main Warehouse", "-$620.00", "11:00 AM", TransactionType.Lot, "Jun 20")
        )
    }

    val groupedTransactions = transactions.groupBy { it.date }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Summary cards row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(Modifier.weight(1f), "Sales", "+$8,500", AccentTeal)
                    SummaryCard(Modifier.weight(1f), "Lots", "-$5,200", LotBlue)
                    SummaryCard(Modifier.weight(1f), "Expenses", "-$980", NegativeRed)
                }
            }

            // 2. Filter tabs
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs.size) { index ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { Text(tabs[index]) },
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
            }

            // 3. Date range selector row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dateFilters.size) { index ->
                        FilterChip(
                            selected = selectedDateIndex == index,
                            onClick = { selectedDateIndex = index },
                            label = { Text(dateFilters[index]) },
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
            }

            // 4. Transactions list
            groupedTransactions.forEach { (date, items) ->
                stickyHeader {
                    Text(
                        text = date,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground)
                            .padding(16.dp)
                    )
                }

                items(items) { transaction ->
                    TransactionRow(transaction, onTransactionClick)
                }
            }
        }

        // 5. FAB
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = AccentTeal,
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier, label: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionRow(transaction: TransactionItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
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
            val (icon, iconBg, amountColor) = when (transaction.type) {
                TransactionType.Sale -> Triple(Icons.Default.TrendingUp, AccentTeal, AccentTeal)
                TransactionType.Lot -> Triple(Icons.Default.Inventory, LotBlue, LotBlue)
                TransactionType.Expense -> Triple(Icons.Default.Receipt, NegativeRed, NegativeRed)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(transaction.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(transaction.subtitle, color = TextSecondary, fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(transaction.amount, color = amountColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(transaction.time, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SalesScreenPreview() {
    SalesScreen()
}
