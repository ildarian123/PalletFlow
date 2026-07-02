package com.palletflow.presentation.screens.warehouse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.domain.usecase.GetWarehousesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WarehouseListViewModel @Inject constructor(
    private val getWarehousesUseCase: GetWarehousesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(WarehouseListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadWarehouses()
    }

    private fun loadWarehouses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getWarehousesUseCase().collect { warehouseList ->
                val items = warehouseList.map { warehouse ->
                    WarehouseItem(
                        id = warehouse.id,
                        name = warehouse.name,
                        location = warehouse.address ?: "No address",
                        isActive = warehouse.isActive,
                        itemsCount = "0", // TODO: Fetch real item count
                        totalValue = "$0.00" // TODO: Fetch real total value
                    )
                }
                _uiState.update { 
                    it.copy(
                        warehouses = items,
                        isLoading = false,
                        totalValue = "$0.00" // TODO: Calculate total value
                    )
                }
            }
        }
    }
}

data class WarehouseListUiState(
    val warehouses: List<WarehouseItem> = emptyList(),
    val totalValue: String = "$0.00",
    val isLoading: Boolean = false
)

data class WarehouseItem(
    val id: String,
    val name: String,
    val location: String,
    val isActive: Boolean,
    val itemsCount: String,
    val totalValue: String
)
