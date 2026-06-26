package com.palletflow.presentation.screens.inventory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager // Added
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val ImagePlaceholder = Color(0xFF2C2C2E)
private val ErrorColor = Color(0xFFFF453A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceColor,
            title = { Text("Delete Item?", color = TextPrimary) },
            text = { Text("This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteClick()
                }) {
                    Text("Delete", color = ErrorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Edit Item", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            // Photo area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ImagePlaceholder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TextSecondary
                )
                
                // Edit overlay button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentTeal)
                        .clickable { /* Handle photo edit */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Card 1 — Basic Info
            EditSectionCard(title = "Basic Info") {
                PalletTextField(label = "Item Name", value = "Nike Air Max 90")
                PalletTextField(label = "Category", value = "Footwear")
                PalletTextField(label = "SKU", value = "SKU-10042")
            }

            // Card 2 — Pricing
            EditSectionCard(title = "Pricing") {
                PalletTextField(label = "Cost Price", value = "12.00")
                PalletTextField(label = "Selling Price", value = "45.00")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Profit preview", color = TextSecondary, fontSize = 14.sp)
                    Text("$33.00", color = AccentTeal, fontWeight = FontWeight.Bold)
                }
            }

            // Card 3 — Stock
            EditSectionCard(title = "Stock") {
                PalletTextField(label = "Quantity", value = "3")
                
                Text("Condition", color = TextSecondary, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConditionChip("New", false)
                    ConditionChip("Used", true)
                    ConditionChip("Damaged", false)
                }
                
                PalletTextField(label = "Warehouse", value = "Main Warehouse")
                PalletTextField(label = "Lot", value = "Lot #1042")
            }

            // Card 4 — Notes
            EditSectionCard(title = "Notes") {
                PalletTextField(
                    label = "Notes",
                    value = "Good condition, minor scuffs",
                    singleLine = false,
                    modifier = Modifier.heightIn(min = 80.dp)
                )
            }

            // Delete section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(color = ImagePlaceholder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                    border = BorderStroke(1.dp, ErrorColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Item", fontWeight = FontWeight.Bold)
                }
                Text(
                    "This will permanently remove the item",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun EditSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            content()
        }
    }
}

@Composable
fun PalletTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    var textState by remember { mutableStateOf(value) }
    
    OutlinedTextField(
        value = textState,
        onValueChange = { textState = it },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
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
fun ConditionChip(label: String, selected: Boolean) {
    Surface(
        color = if (selected) AccentTeal else ImagePlaceholder,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { /* Handle select */ }
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditItemScreenPreview() {
    EditItemScreen()
}
