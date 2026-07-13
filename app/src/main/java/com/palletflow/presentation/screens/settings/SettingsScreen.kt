package com.palletflow.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Backup & Restore", fontWeight = FontWeight.Bold, color = Color(0xFFF5F5F7))
            
            // Last backup info
            val lastBackupTime = viewModel.getLastBackupTime()
            Text(
                "Last backup: ${viewModel.formatDate(lastBackupTime)}",
                color = Color(0xFFA1A1A6),
                fontSize = 12.sp
            )
            
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color(0xFF00C2A0), modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.message?.let {
                Text(it, color = Color(0xFF00C2A0), fontSize = 14.sp)
            }
            uiState.error?.let {
                Text(it, color = Color(0xFFFF453A), fontSize = 14.sp)
            }

            // Manual backup button
            Button(
                onClick = { viewModel.manualBackup() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C2A0)),
                enabled = !uiState.isLoading
            ) {
                Text("Backup Now", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            
            // Manual restore button
            Button(
                onClick = { showRestoreDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                enabled = !uiState.isLoading
            ) {
                Text("Restore from Google Drive", color = Color.White)
            }
            
            // Auto-backup toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-backup daily", color = Color(0xFFF5F5F7))
                Switch(
                    checked = viewModel.isAutoBackupEnabled(),
                    onCheckedChange = { viewModel.setAutoBackupEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00C2A0))
                )
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Data") },
            text = { Text("This will overwrite your current local data with the latest backup from Google Drive. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    viewModel.manualRestore()
                }) {
                    Text("Restore", color = Color(0xFF00C2A0))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFA1A1A6)
        )
    }
}
