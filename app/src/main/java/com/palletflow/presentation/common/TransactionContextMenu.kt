package com.palletflow.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionStatus
import com.palletflow.data.local.entity.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionContextMenu(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onViewDetailsClick: (transactionId: String) -> Unit,
    onEditClick: (transactionId: String) -> Unit,
    onDeleteClick: (transactionId: String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

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
                when (transaction.type) {
                    TransactionType.INCOMING -> "Incoming"
                    TransactionType.SALE -> "Sale"
                    TransactionType.TRANSFER -> "Transfer"
                },
                color = Color(0xFFF5F5F7),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // View Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .clickable {
                        onViewDetailsClick(transaction.id)
                        onDismiss()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Visibility, null, tint = Color(0xFF00C2A0), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text("View Details", color = Color(0xFFF5F5F7), fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(8.dp))

            // Edit (only if DRAFT)
            if (transaction.status == TransactionStatus.DRAFT) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2E))
                        .clickable {
                            onEditClick(transaction.id)
                            onDismiss()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFF00C2A0), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Edit", color = Color(0xFFF5F5F7), fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
            }

            // Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .clickable { showDeleteConfirm = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFFF453A), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color(0xFFA1A1A6))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1E1E1E),
            title = {
                Text("Delete transaction?", color = Color(0xFFF5F5F7), fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This action cannot be undone.", color = Color(0xFFA1A1A6))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick(transaction.id)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color(0xFFA1A1A6))
                }
            }
        )
    }
}
