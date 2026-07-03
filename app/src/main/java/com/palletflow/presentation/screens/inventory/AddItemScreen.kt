package com.palletflow.presentation.screens.inventory

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.palletflow.data.local.entity.ItemType
import com.palletflow.presentation.common.PhotoPickerSheet
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentTeal = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val ImagePlaceholder = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    viewModel: AddItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect { success ->
            if (success) onSaveClick()
        }
    }

    var typeExpanded by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { viewModel.onPhotoPathChange(it.toString()) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoPathChange(it.toString()) }
    }

    if (showPhotoPicker) {
        PhotoPickerSheet(
            onDismiss = { showPhotoPicker = false },
            onCameraClick = {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add New Item", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                        TextButton(
                            onClick = viewModel::save,
                            enabled = !uiState.isLoading
                        ) {
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Upload Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceColor)
                    .drawBehind {
                        if (uiState.photoPath == null) {
                            drawRoundRect(
                                color = TextSecondary,
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                            )
                        }
                    }
                    .clickable(enabled = !uiState.isLoading) { showPhotoPicker = true },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.photoPath != null) {
                    AsyncImage(
                        model = uiState.photoPath,
                        contentDescription = "Item Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Optional: Add a "change photo" overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to add photo", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }

            // Card 1 — Basic Info
            AddSectionCard(label = "Basic Info") {
                PalletOutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Item Name",
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { if (!uiState.isLoading) typeExpanded = !typeExpanded }
                ) {
                    PalletOutlinedTextField(
                        value = when (uiState.type) {
                            ItemType.SINGLE_PRODUCT -> "Single Product"
                            ItemType.PALLET -> "Pallet"
                            else -> "Single Product"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = "Type",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.background(SurfaceColor)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Single Product", color = TextPrimary) },
                            onClick = {
                                viewModel.onTypeChange(ItemType.SINGLE_PRODUCT)
                                typeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pallet", color = TextPrimary) },
                            onClick = {
                                viewModel.onTypeChange(ItemType.PALLET)
                                typeExpanded = false
                            }
                        )
                    }
                }

                PalletOutlinedTextField(
                    value = uiState.sku,
                    onValueChange = viewModel::onSkuChange,
                    label = "SKU / Barcode",
                    trailingIcon = { 
                        IconButton(onClick = onScanClick, enabled = !uiState.isLoading) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = TextSecondary)
                        }
                    }
                )
            }

            // Card 4 — Notes
            AddSectionCard(label = "Notes") {
                PalletOutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = "Notes",
                    minLines = 3,
                    maxLines = 6,
                    singleLine = false
                )
            }
        }
    }
}

@Composable
fun AddSectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            content()
        }
    }
}

@Composable
fun PalletOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = trailingIcon,
        prefix = prefix,
        keyboardOptions = keyboardOptions,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        supportingText = supportingText,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = AccentTeal,
            unfocusedBorderColor = ImagePlaceholder,
            focusedLabelColor = AccentTeal,
            unfocusedLabelColor = TextSecondary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AddItemScreenPreview() {
    AddItemScreen()
}
