package com.palletflow.presentation.screens.warehouse

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val PlaceholderColor = Color(0xFF2C2C2E)
private val BorderColor = Color(0xFF2C2C2E)

@Composable
fun WarehouseDetailScreen(
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onAddLotClick: () -> Unit = {},
    onMoveItemsClick: () -> Unit = {},
    viewModel: WarehouseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    WarehouseDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onAddLotClick = onAddLotClick,
        onMoveItemsClick = onMoveItemsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseDetailContent(
    uiState: WarehouseDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onAddLotClick: () -> Unit,
    onMoveItemsClick: () -> Unit
) {
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(uiState.name, color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = AccentTeal
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            uiState.name,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            uiState.location,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = (if (uiState.isActive) AccentTeal else TextSecondary).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, (if (uiState.isActive) AccentTeal else TextSecondary).copy(alpha = 0.5f))
                        ) {
                            Text(
                                if (uiState.isActive) "Active" else "Inactive",
                                color = if (uiState.isActive) AccentTeal else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Metrics Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailMetricCard(Modifier.weight(1f), "Total Items", uiState.itemsCount, TextPrimary)
                        DetailMetricCard(Modifier.weight(1f), "Total Value", uiState.totalValue, AccentTeal)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailMetricCard(Modifier.weight(1f), "Active Lots", uiState.activeLots, TextPrimary)
                        DetailMetricCard(Modifier.weight(1f), "Sold This Month", uiState.soldThisMonth, AccentTeal)
                    }
                }

                // Recent Activity Section
                Text(
                    "Recent Activity",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )

//                ActivityRow(
//                    icon = Icons.Default.AddBox,
//                    iconColor = Color(0xFF4CD964),
//                    title = "Received Lot #1042",
//                    subtitle = "Nike shoes x12",
//                    amount = "+$145.00",
//                    date = "Today"
//                )
//                ActivityRow(
//                    icon = Icons.Default.Sell,
//                    iconColor = Color(0xFFFF3B30),
//                    title = "Sold Item",
//                    subtitle = "Samsung TV",
//                    amount = "-$320.00",
//                    date = "Yesterday"
//                )
//                ActivityRow(
//                    icon = Icons.Default.LocalShipping,
//                    iconColor = Color(0xFF007AFF),
//                    title = "Transfer",
//                    subtitle = "To Storage 2 • x5 items",
//                    amount = "",
//                    date = "Jun 20"
//                )
//                ActivityRow(
//                    icon = Icons.Default.AddBox,
//                    iconColor = Color(0xFF4CD964),
//                    title = "Received Lot #1041",
//                    subtitle = "Mixed electronics x30",
//                    amount = "+$280.00",
//                    date = "Jun 18"
//                )

                // Items in Warehouse Section
                Text(
                    "Items in Stock",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
//                    items(6) { index ->
//                        ItemPreviewCard("Item Name $index", "x3")
//                    }
                }
            }

            // Fixed Bottom Bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = DarkBackground
            ) {
                Column {
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onAddLotClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Add Lot", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onMoveItemsClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal),
                            border = BorderStroke(1.dp, AccentTeal),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Move Items", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal),
                            border = BorderStroke(1.dp, AccentTeal),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Edit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailMetricCard(modifier: Modifier, label: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActivityRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    amount: String,
    date: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (amount.isNotEmpty()) {
                    Text(amount, color = TextSecondary, fontSize = 12.sp)
                }
                Text(date, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ItemPreviewCard(name: String, qty: String) {
    Card(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentTeal.copy(alpha = 0.8f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(qty, color = DarkBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WarehouseDetailScreenPreview() {
    WarehouseDetailContent(
        uiState = WarehouseDetailUiState(
            name = "Main Warehouse",
            location = "Chicago, IL",
            isActive = true,
            itemsCount = "1,240",
            totalValue = "$45,200",
            activeLots = "12",
            soldThisMonth = "$12,450"
        ),
        onBackClick = {},
        onEditClick = {},
        onAddLotClick = {},
        onMoveItemsClick = {}
    )
}
