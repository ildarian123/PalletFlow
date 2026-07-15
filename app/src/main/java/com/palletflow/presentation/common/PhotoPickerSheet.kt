package com.palletflow.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palletflow.presentation.common.permissions.CameraPermissionHandler
import com.palletflow.presentation.common.permissions.GalleryPermissionHandler
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    // Use a state to handle the action after dismissal to avoid race conditions with system permissions
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(pendingAction) {
        pendingAction?.let { action ->
            // Small delay to allow permission dialog/system UI to settle
            delay(100)
            sheetState.hide()
            onDismiss()
            action()
            pendingAction = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E)
    ) {
        CameraPermissionHandler(
            onPermissionGranted = {
                pendingAction = onCameraClick
            }
        ) { requestCameraPermission ->
            GalleryPermissionHandler(
                onPermissionGranted = {
                    pendingAction = onGalleryClick
                }
            ) { requestGalleryPermission ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        "Add Photo",
                        color = Color(0xFFF5F5F7),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Camera row — calls requestCameraPermission
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2E))
                            .clickable { requestCameraPermission() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF00C2A0), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Take Photo", color = Color(0xFFF5F5F7), fontWeight = FontWeight.Medium)
                            Text("Use camera", color = Color(0xFFA1A1A6), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Gallery row — calls requestGalleryPermission
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2E))
                            .clickable { requestGalleryPermission() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = Color(0xFF00C2A0), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Choose from Gallery", color = Color(0xFFF5F5F7), fontWeight = FontWeight.Medium)
                            Text("Select existing photo", color = Color(0xFFA1A1A6), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel", color = Color(0xFFA1A1A6))
                    }
                }
            }
        }
    }
}
