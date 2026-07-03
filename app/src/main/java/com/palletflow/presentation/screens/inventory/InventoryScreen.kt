package com.palletflow.presentation.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.presentation.common.ItemContextMenu
import com.palletflow.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

private val DarkBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val AccentColor = Color(0xFF00C2A0)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xFFA1A1A6)
private val ImagePlaceholderColor = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onItemClick: (itemId: String) -> Unit,
    onAddClick: () -> Unit,
    navController: NavHostController,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.deleteEvent.collect { event ->
            when (event) {
                is DeleteEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                DeleteEvent.Success -> {
                    // List updates automatically via Flow
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        InventoryScreenContent(
            uiState = uiState,
            onItemClick = onItemClick,
            onAddClick = onAddClick,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onTypeFilterChange = viewModel::onTypeFilterChange,
            onRetryClick = { viewModel.loadItems() },
            onDeleteClick = viewModel::deleteItem,
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreenContent(
    uiState: InventoryUiState,
    onItemClick: (itemId: String) -> Unit,
    onAddClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (ItemType?) -> Unit,
    onRetryClick: () -> Unit,
    onDeleteClick: (Item) -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor),
                    placeholder = { Text("Search items...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onSearchQueryChange("")
                                focusManager.clearFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AccentColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // 2. Type Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    FilterChip(
                        selected = uiState.selectedTypeFilter == null,
                        onClick = { onTypeFilterChange(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceColor,
                            labelColor = TextSecondary,
                            selectedContainerColor = AccentColor,
                            selectedLabelColor = DarkBackground
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = uiState.selectedTypeFilter == ItemType.SINGLE_PRODUCT,
                        onClick = { onTypeFilterChange(ItemType.SINGLE_PRODUCT) },
                        label = { Text("Single Product") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceColor,
                            labelColor = TextSecondary,
                            selectedContainerColor = AccentColor,
                            selectedLabelColor = DarkBackground
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterChip(
                        selected = uiState.selectedTypeFilter == ItemType.PALLET,
                        onClick = { onTypeFilterChange(ItemType.PALLET) },
                        label = { Text("Pallet") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceColor,
                            labelColor = TextSecondary,
                            selectedContainerColor = AccentColor,
                            selectedLabelColor = DarkBackground
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 3. States: Loading, Error, Empty, or List
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AccentColor)
                        }
                    }
                }
                uiState.error != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(uiState.error, color = Color(0xFFFF453A))
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onRetryClick,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                uiState.filteredItems.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = ImagePlaceholderColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (uiState.searchQuery.isBlank()) "No items yet" else "No items match your search",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.searchQuery.isBlank()) {
                                Text(
                                    "Tap + to add your first item",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                else -> {
                    items(uiState.filteredItems, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onLongClick = {
                                selectedItem = item
                                showContextMenu = true
                            }
                        )
                    }
                }
            }
        }

        // 6. FAB with Drop-up Menu
        var fabExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                containerColor = AccentColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }

            DropdownMenu(
                expanded = fabExpanded,
                onDismissRequest = { fabExpanded = false },
                modifier = Modifier.background(SurfaceColor)
            ) {
                DropdownMenuItem(
                    text = { Text("Item", color = TextPrimary) },
                    onClick = {
                        fabExpanded = false
                        onAddClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Warehouse", color = TextPrimary) },
                    onClick = {
                        fabExpanded = false
                        // Handle warehouse creation
                    }
                )
            }
        }

        if (showContextMenu && selectedItem != null) {
            ItemContextMenu(
                item = selectedItem!!,
                onDismiss = {
                    showContextMenu = false
                    selectedItem = null
                },
                onEditClick = { itemId ->
                    navController.navigate(Screen.EditItem.createRoute(itemId))
                },
                onDeleteClick = { item ->
                    onDeleteClick(item)
                }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: Item,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val dateAdded = remember(item.createdAt) {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        sdf.format(Date(item.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Photo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ImagePlaceholderColor),
                contentAlignment = Alignment.Center
            ) {
                if (item.photoPath != null) {
                    AsyncImage(
                        model = item.photoPath,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center: Details
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Type Badge
                    Surface(
                        color = if (item.type == ItemType.PALLET) AccentColor else ImagePlaceholderColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (item.type == ItemType.PALLET) "Pallet" else "Product",
                            color = if (item.type == ItemType.PALLET) DarkBackground else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    if (!item.sku.isNullOrBlank()) {
                        Text(item.sku, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            // Right: Date Added
            Column(horizontalAlignment = Alignment.End) {
                Text(dateAdded, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    // Note: This preview might need mocks for navController and uiState
}
