package com.palletflow.presentation.screens.supplier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val BorderColor = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSupplierScreen(
    onBackClick: () -> Unit,
    onSupplierSaved: () -> Unit,
    viewModel: NewSupplierViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { result ->
            if (result.isSuccess) {
                onSupplierSaved()
            } else {
                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to save supplier")
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add New Supplier", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                        TextButton(onClick = viewModel::saveSupplier) {
                            Text("Save", color = AccentTeal, fontWeight = FontWeight.Bold)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supplier Name
            SupplierInputField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Supplier Name",
                placeholder = "e.g. Acme Corp",
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError
            )

            // Contact Person
            SupplierInputField(
                value = uiState.contactPerson,
                onValueChange = viewModel::onContactPersonChange,
                label = "Contact Person",
                placeholder = "e.g. John Doe"
            )

            // Email
            SupplierInputField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email Address",
                placeholder = "e.g. contact@acme.com",
                icon = Icons.Default.Email
            )

            // Phone
            SupplierInputField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone Number",
                placeholder = "e.g. +1 234 567 890",
                icon = Icons.Default.Phone
            )

            // Address
            SupplierInputField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = "Address",
                placeholder = "e.g. 123 Business St",
                icon = Icons.Default.LocationOn,
                singleLine = false,
                minLines = 2
            )

            // Notes
            SupplierInputField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Notes",
                placeholder = "Additional information...",
                icon = Icons.Default.Notes,
                singleLine = false,
                minLines = 3
            )
        }
    }
}

@Composable
private fun SupplierInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = label,
            color = if (isError) Color(0xFFFF453A) else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary) },
            leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = TextSecondary) } },
            isError = isError,
            supportingText = errorMessage?.let { { Text(it) } },
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = AccentTeal,
                unfocusedBorderColor = BorderColor,
                errorBorderColor = Color(0xFFFF453A)
            )
        )
    }
}
