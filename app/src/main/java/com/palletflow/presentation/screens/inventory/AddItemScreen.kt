package com.palletflow.presentation.screens.inventory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
private val ImagePlaceholder = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    
    var costPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    
    var quantity by remember { mutableIntStateOf(1) }
    var selectedCondition by remember { mutableStateOf("New") }
    
    var warehouseExpanded by remember { mutableStateOf(false) }
    var selectedWarehouse by remember { mutableStateOf("Main Warehouse") }
    
    var lotExpanded by remember { mutableStateOf(false) }
    var selectedLot by remember { mutableStateOf("Lot #1042") }
    
    var notes by remember { mutableStateOf("") }

    val profit = remember(costPrice, sellPrice) {
        val cost = costPrice.toDoubleOrNull() ?: 0.0
        val sell = sellPrice.toDoubleOrNull() ?: 0.0
        sell - cost
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add New Item", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = onSaveClick) {
                        Text("Save", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Upload Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceColor)
                    .drawBehind {
                        drawRoundRect(
                            color = TextSecondary,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                        )
                    }
                    .clickable { /* Handle photo upload */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to add photo", color = TextSecondary, fontSize = 14.sp)
                }
            }

            // Card 1 — Basic Info
            AddSectionCard(label = "Basic Info") {
                PalletOutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = "Item Name"
                )
                PalletOutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = "Category",
                    trailingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = TextSecondary) }
                )
                PalletOutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = "SKU / Barcode",
                    trailingIcon = { 
                        IconButton(onClick = onScanClick) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = TextSecondary)
                        }
                    }
                )
            }

            // Card 2 — Pricing
            AddSectionCard(label = "Pricing") {
                PalletOutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = "Cost Price",
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                PalletOutlinedTextField(
                    value = sellPrice,
                    onValueChange = { sellPrice = it },
                    label = "Selling Price",
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated profit:", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        "$${String.format("%.2f", profit)}",
                        color = AccentTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Card 3 — Stock
            AddSectionCard(label = "Stock") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity", color = TextPrimary, fontSize = 14.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 0) quantity-- },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = ImagePlaceholder)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus", tint = TextPrimary)
                        }
                        Text(
                            text = quantity.toString(),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        IconButton(
                            onClick = { quantity++ },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = ImagePlaceholder)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus", tint = TextPrimary)
                        }
                    }
                }

                Text("Condition", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConditionFilterChip(label = "New", isSelected = selectedCondition == "New") { selectedCondition = "New" }
                    ConditionFilterChip(label = "Used", isSelected = selectedCondition == "Used") { selectedCondition = "Used" }
                    ConditionFilterChip(label = "Damaged", isSelected = selectedCondition == "Damaged") { selectedCondition = "Damaged" }
                }

                ExposedDropdownMenuBox(
                    expanded = warehouseExpanded,
                    onExpandedChange = { warehouseExpanded = !warehouseExpanded }
                ) {
                    PalletOutlinedTextField(
                        value = selectedWarehouse,
                        onValueChange = {},
                        readOnly = true,
                        label = "Warehouse",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = warehouseExpanded,
                        onDismissRequest = { warehouseExpanded = false },
                        modifier = Modifier.background(SurfaceColor)
                    ) {
                        listOf("Main Warehouse", "Warehouse A", "Warehouse B").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = TextPrimary) },
                                onClick = {
                                    selectedWarehouse = option
                                    warehouseExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = lotExpanded,
                    onExpandedChange = { lotExpanded = !lotExpanded }
                ) {
                    PalletOutlinedTextField(
                        value = selectedLot,
                        onValueChange = {},
                        readOnly = true,
                        label = "Lot",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lotExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = lotExpanded,
                        onDismissRequest = { lotExpanded = false },
                        modifier = Modifier.background(SurfaceColor)
                    ) {
                        listOf("Lot #1042", "Lot #1043", "Lot #1044").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = TextPrimary) },
                                onClick = {
                                    selectedLot = option
                                    lotExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Card 4 — Notes
            AddSectionCard(label = "Notes") {
                PalletOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    minLines = 3,
                    maxLines = 6,
                    singleLine = false
                )
            }
        }
    }
}

@Composable
fun AddSectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            content()
        }
    }
}

@Composable
fun PalletOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = trailingIcon,
        prefix = prefix,
        keyboardOptions = keyboardOptions,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = AccentTeal,
            unfocusedBorderColor = ImagePlaceholder,
            focusedLabelColor = AccentTeal,
            unfocusedLabelColor = TextSecondary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
fun ConditionFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AccentTeal,
            selectedLabelColor = Color.Black,
            containerColor = ImagePlaceholder,
            labelColor = TextPrimary
        ),
        border = null,
        shape = RoundedCornerShape(8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun AddItemScreenPreview() {
    AddItemScreen()
}
