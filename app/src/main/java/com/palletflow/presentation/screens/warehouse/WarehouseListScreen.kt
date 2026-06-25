package com.palletflow.presentation.screens.warehouse

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WarehouseListScreen(
    onWarehouseClick: (Int) -> Unit,
    onAddWarehouseClick: () -> Unit,
    viewModel: WarehouseListViewModel = hiltViewModel()
) {
}