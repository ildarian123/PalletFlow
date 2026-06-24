package com.palletflow.presentation.screens.supplier

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SupplierListScreen(
    onSupplierClick: (Int) -> Unit,
    onAddSupplierClick: () -> Unit,
    viewModel: SupplierListViewModel = hiltViewModel()
) {
}