package com.palletflow.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palletflow.data.local.entity.Supplier
import com.palletflow.presentation.common.AddSupplierSheetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplierSheet(
    onDismiss: () -> Unit,
    onSupplierCreated: (Supplier) -> Unit,
    viewModel: AddSupplierSheetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { supplier ->
            onSupplierCreated(supplier)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "New Supplier",
                color = Color(0xFFF5F5F7),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Supplier name field
            OutlinedTextField(
                value = uiState.supplierName,
                onValueChange = viewModel::onSupplierNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Supplier Name") },
                placeholder = { Text("Enter supplier name") },
                isError = uiState.error != null,
                supportingText = {
                    if (uiState.error != null) {
                        Text(
                            uiState.error!!,
                            color = Color(0xFFFF453A),
                            fontSize = 12.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C2A0),
                    focusedLabelColor = Color(0xFF00C2A0)
                )
            )

            Spacer(Modifier.height(16.dp))

            // Save button
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C2A0)),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Cancel button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text("Cancel", color = Color(0xFFA1A1A6))
            }
        }
    }
}
