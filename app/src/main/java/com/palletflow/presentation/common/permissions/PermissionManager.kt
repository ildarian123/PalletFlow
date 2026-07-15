package com.palletflow.presentation.common.permissions

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionState(
    permission: String,
    onGranted: () -> Unit,
    onDenied: () -> Unit = {},
    onPermanentlyDenied: () -> Unit = {}
): PermissionState {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    var shouldShowRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onGranted()
        } else {
            val activity = context as? Activity
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
            } ?: false

            if (showRationale) {
                onDenied()
            } else {
                onPermanentlyDenied()
            }
        }
    }

    return remember(hasPermission, shouldShowRationale) {
        PermissionState(
            hasPermission = hasPermission,
            shouldShowRationale = shouldShowRationale,
            requestPermission = {
                if (hasPermission) {
                    onGranted()
                } else {
                    launcher.launch(permission)
                }
            }
        )
    }
}

data class PermissionState(
    val hasPermission: Boolean,
    val shouldShowRationale: Boolean,
    val requestPermission: () -> Unit
)
