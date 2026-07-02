package com.palletflow.presentation.common.permissions

import android.Manifest
import android.os.Build
import androidx.compose.runtime.*

@Composable
fun GalleryPermissionHandler(
    onPermissionGranted: () -> Unit,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }

    // Use correct permission based on Android version
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(
        permission = galleryPermission,
        onGranted = onPermissionGranted,
        onDenied = { showRationaleDialog = true },
        onPermanentlyDenied = { showPermanentlyDeniedDialog = true }
    )

    content(permissionState.requestPermission)

    if (showRationaleDialog) {
        PermissionRationaleDialog(
            title = "Gallery Permission Required",
            message = "PalletFlow needs access to your photos to select item images. Please grant permission.",
            onConfirm = {
                showRationaleDialog = false
                permissionState.requestPermission()
            },
            onDismiss = { showRationaleDialog = false }
        )
    }

    if (showPermanentlyDeniedDialog) {
        PermissionPermanentlyDeniedDialog(
            title = "Gallery Permission Denied",
            message = "Gallery permission was permanently denied. Please enable it in app settings.",
            onOpenSettings = {
                showPermanentlyDeniedDialog = false
            },
            onDismiss = { showPermanentlyDeniedDialog = false }
        )
    }
}
