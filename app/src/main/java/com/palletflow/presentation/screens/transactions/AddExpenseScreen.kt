package com.palletflow.presentation.screens.transactions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onAttachReceiptClick: () -> Unit = {}
) {
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(-1) }
    var description by remember { mutableStateOf("") }
    var selectedRecurrence by remember { mutableIntStateOf(0) }
    var selectedPayment by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }

    val categories = listOf(
        Icons.Default.Home to "Rent",
        Icons.Default.Person to "Salary",
        Icons.Default.Wifi to "Internet",
        Icons.Default.LocalShipping to "Shipping",
        Icons.Default.Build to "Repairs",
        Icons.Default.DirectionsCar to "Vehicle",
        Icons.Default.ElectricBolt to "Utilities",
        Icons.Default.Storefront to "Marketing",
        Icons.Default.MoreHoriz to "Other"
    )

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveClick,
                        enabled = amountInput.isNotEmpty() && selectedCategory != -1
                    ) {
                        Text(
                            "Save",
                            color = if (amountInput.isNotEmpty() && selectedCategory != -1) AccentTeal else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = DarkBackground
            ) {
                Column {
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = amountInput.isNotEmpty() && selectedCategory != -1,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentTeal,
                                disabledContainerColor = PlaceholderColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Save Expense",
                                color = if (amountInput.isNotEmpty() && selectedCategory != -1) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AMOUNT CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$", color = AccentTeal, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = amountInput.ifEmpty { "0.00" },
                                color = if (amountInput.isEmpty()) TextSecondary else TextPrimary,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Tap to enter amount", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { newValue -> 
                                if (newValue.isEmpty() || newValue.replace(".", "").all { it.isDigit() }) {
                                    amountInput = newValue 
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0.00", color = TextSecondary) },
                            prefix = { Text("$", color = AccentTeal) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // CATEGORY CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Category", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 3
                        ) {
                            categories.forEachIndexed { index, (icon, label) ->
                                CategoryChip(
                                    icon = icon,
                                    label = label,
                                    isSelected = selectedCategory == index,
                                    onClick = { selectedCategory = index },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // DETAILS CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Details", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        Column {
                            Text("Description", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("e.g. Monthly office rent", color = TextSecondary) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentTeal,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }

                        Column {
                            Text("Date", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BorderColor)
                                    .clickable { /* onDateClick */ }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Jun 27, 2024", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                            }
                        }

                        Column {
                            Text("Recurrence", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val recurrences = listOf("One-time", "Weekly", "Monthly", "Yearly")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(recurrences) { index, type ->
                                    FilterChip(
                                        selected = selectedRecurrence == index,
                                        onClick = { selectedRecurrence = index },
                                        label = { Text(type) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentTeal,
                                            selectedLabelColor = DarkBackground,
                                            containerColor = BorderColor,
                                            labelColor = TextSecondary
                                        ),
                                        border = null,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        Column {
                            Text("Payment Method", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val payments = listOf("Cash", "Card", "Bank Transfer", "Other")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(payments) { index, type ->
                                    FilterChip(
                                        selected = selectedPayment == index,
                                        onClick = { selectedPayment = index },
                                        label = { Text(type) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentTeal,
                                            selectedLabelColor = DarkBackground,
                                            containerColor = BorderColor,
                                            labelColor = TextSecondary
                                        ),
                                        border = null,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // NOTES CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notes", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Any additional notes...", color = TextSecondary) },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // RECEIPT CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Receipt Photo", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceColor)
                                .drawBehind {
                                    drawRoundRect(
                                        color = TextSecondary,
                                        style = Stroke(
                                            width = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        ),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                                    )
                                }
                                .clickable { onAttachReceiptClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                                Text("Tap to attach receipt", color = TextSecondary, fontSize = 12.sp)
                                Text("Optional", color = TextSecondary.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) AccentTeal else BorderColor,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddExpenseScreenPreview() {
    AddExpenseScreen()
}
