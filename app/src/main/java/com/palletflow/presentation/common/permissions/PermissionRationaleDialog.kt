package com.palletflow.presentation.common.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PermissionRationaleDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = title,
                color = Color(0xFFF5F5F7),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = Color(0xFFA1A1A6)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C2A0)
                )
            ) {
                Text("Grant Permission", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFA1A1A6))
            }
        }
    )
}

@Composable
fun PermissionPermanentlyDeniedDialog(
    title: String,
    message: String,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = title,
                color = Color(0xFFF5F5F7),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                color = Color(0xFFA1A1A6)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    // Open app settings so user can manually grant permission
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    onOpenSettings()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C2A0)
                )
            ) {
                Text("Open Settings", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFA1A1A6))
            }
        }
    )
}
