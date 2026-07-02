package com.palletflow.presentation.screens.warehouse

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
fun AddWarehouseScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    viewModel: AddWarehouseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.actionEvent.collect { action ->
            if (action == WarehouseAction.SAVE) onSaveClick()
        }
    }

    AddWarehouseContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSaveClick = viewModel::save,
        onNameChange = viewModel::onNameChange,
        onAddressChange = viewModel::onAddressChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onActiveChange = viewModel::onActiveChange
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddWarehouseContent(
    uiState: AddWarehouseUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit
) {
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("New Warehouse", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                    .background(SurfaceColor)
                    .clickable { /* Handle icon change */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AccentTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to change icon", color = TextSecondary, fontSize = 12.sp)
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
                    label = "Location / Address",
                    trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary) }
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

            // Card 2 — Status
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddWarehouseScreenPreview() {
    AddWarehouseContent(
        uiState = AddWarehouseUiState(),
        onBackClick = {},
        onSaveClick = {},
        onNameChange = {},
        onAddressChange = {},
        onDescriptionChange = {},
        onActiveChange = {}
    )
}
