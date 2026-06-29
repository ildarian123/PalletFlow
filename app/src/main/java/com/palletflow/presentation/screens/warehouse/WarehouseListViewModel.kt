package com.palletflow.presentation.screens.warehouse

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.GetWarehousesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class WarehouseListViewModel @Inject constructor(
    private val getWarehousesUseCase: GetWarehousesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(WarehouseListUiState())
    val uiState = _uiState.asStateFlow()
}

data class WarehouseListUiState(
    val warehouses: List<WarehouseItem> = emptyList(),
    val totalValue: String = "$0.00",
    val isLoading: Boolean = false
)

data class WarehouseItem(
    val id: Int,
    val name: String,
    val location: String,
    val isActive: Boolean,
    val itemsCount: String,
    val totalValue: String
)
