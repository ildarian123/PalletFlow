package com.palletflow.presentation.screens.warehouse

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palletflow.data.local.entity.WarehouseType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val BorderColor = Color(0xFF2C2C2E)
private val ErrorRed = Color(0xFFFF453A)
private val WarningOrange = Color(0xFFFF9500)

@Composable
fun EditWarehouseScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    viewModel: AddWarehouseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.actionEvent.collect { action ->
            when (action) {
                WarehouseAction.SAVE -> onSaveClick()
                WarehouseAction.DELETE -> onDeleteClick()
            }
        }
    }

    EditWarehouseContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSaveClick = viewModel::save,
        onDeleteConfirm = viewModel::delete,
        onNameChange = viewModel::onNameChange,
        onAddressChange = viewModel::onAddressChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTypeChange = viewModel::onTypeChange,
        onActiveChange = viewModel::onActiveChange
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditWarehouseContent(
    uiState: AddWarehouseUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (WarehouseType) -> Unit,
    onActiveChange: (Boolean) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val warehouseTypes = WarehouseType.entries

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceColor,
            title = { Text("Delete Warehouse?", color = TextPrimary) },
            text = { Text("Items inside will become unassigned. This cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteConfirm()
                }) {
                    Text("Delete", color = ErrorRed)
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
                title = { Text("Edit Warehouse", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                        TextButton(onClick = onSaveClick) {
                            Text("Save", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
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
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Picker Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AccentTeal
                    )
                    // Edit badge overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SurfaceColor)
                            .border(1.dp, AccentTeal, CircleShape)
                            .clickable { /* Change icon */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Icon",
                            tint = AccentTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Card 1 — Basic Info
            WarehouseSectionCard(label = "Basic Info") {
                WarehouseTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    placeholder = "e.g. Main Warehouse",
                    label = "Warehouse Name"
                )
                if (uiState.nameError != null) {
                    Text(
                        text = uiState.nameError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                WarehouseTextField(
                    value = uiState.address,
                    onValueChange = onAddressChange,
                    placeholder = "e.g. Chicago, IL",
                    label = "Location / Address"
                )
                WarehouseTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    placeholder = "Optional notes about this warehouse",
                    label = "Description",
                    singleLine = false,
                    minLines = 2
                )
            }

            // Card 2 — Type
            WarehouseSectionCard(label = "Warehouse Type") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    warehouseTypes.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedType == type,
                            onClick = { onTypeChange(type) },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = DarkBackground,
                                containerColor = BorderColor,
                                labelColor = TextSecondary
                            ),
                            border = null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Card 3 — Status
            WarehouseSectionCard(label = "Status") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active", color = TextPrimary, fontSize = 16.sp)
                    Switch(
                        checked = uiState.isActive,
                        onCheckedChange = onActiveChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentTeal,
                            checkedTrackColor = AccentTeal.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BorderColor
                        )
                    )
                }
                if (!uiState.isActive) {
                    Text(
                        text = "Inactive warehouses won't appear in new lot or sale operations",
                        color = WarningOrange,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Danger Zone Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Danger Zone", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        "Deleting a warehouse will not delete its items. Items will become unassigned.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete Warehouse", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditWarehouseScreenPreview() {
    EditWarehouseContent(
        uiState = AddWarehouseUiState(
            name = "Main Warehouse",
            address = "Chicago, IL",
            description = "Main distribution center",
            selectedType = WarehouseType.MAIN,
            isActive = true
        ),
        onBackClick = {},
        onSaveClick = {},
        onDeleteConfirm = {},
        onNameChange = {},
        onAddressChange = {},
        onDescriptionChange = {},
        onTypeChange = {},
        onActiveChange = {}
    )
}
