package com.palletflow.presentation.screens.inventory

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class InventoryViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()
}

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val warehouses: List<String> = listOf("All Warehouses"),
    val isLoading: Boolean = false
)

data class InventoryItem(
    val id: Int,
    val name: String,
    val lotNumber: String,
    val warehouse: String,
    val quantity: String,
    val price: String,
    val profit: String
)
