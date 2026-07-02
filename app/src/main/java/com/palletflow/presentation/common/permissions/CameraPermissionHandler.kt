package com.palletflow.presentation.common.permissions

import android.Manifest
import androidx.compose.runtime.*

@Composable
fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onGranted = onPermissionGranted,
        onDenied = { showRationaleDialog = true },
        onPermanentlyDenied = { showPermanentlyDeniedDialog = true }
    )

    content(permissionState.requestPermission)

    if (showRationaleDialog) {
        PermissionRationaleDialog(
            title = "Camera Permission Required",
            message = "PalletFlow needs camera access to take photos of your items. Please grant camera permission.",
            onConfirm = {
                showRationaleDialog = false
                permissionState.requestPermission()
            },
            onDismiss = { showRationaleDialog = false }
        )
    }

    if (showPermanentlyDeniedDialog) {
        PermissionPermanentlyDeniedDialog(
            title = "Camera Permission Denied",
            message = "Camera permission was permanently denied. Please enable it in app settings to take photos.",
            onOpenSettings = {
                showPermanentlyDeniedDialog = false
            },
            onDismiss = { showPermanentlyDeniedDialog = false }
        )
    }
}
