package com.palletflow.presentation.screens.warehouse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.domain.usecase.warehouse.GetWarehouseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WarehouseDetailViewModel @Inject constructor(
    private val getWarehouseUseCase: GetWarehouseUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val warehouseId: String? = savedStateHandle["id"]
    
    private val _uiState = MutableStateFlow(WarehouseDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        warehouseId?.let { loadWarehouse(it) }
    }

    private fun loadWarehouse(id: String) {
        viewModelScope.launch {
            val warehouse = getWarehouseUseCase(id)
            if (warehouse != null) {
                _uiState.update {
                    it.copy(
                        name = warehouse.name,
                        location = warehouse.address ?: "No address",
                        isActive = warehouse.isActive,
                        // TODO: Load real metrics
                        itemsCount = "0",
                        totalValue = "$0.00",
                        activeLots = "0",
                        soldThisMonth = "$0.00"
                    )
                }
            }
        }
    }
}

data class WarehouseDetailUiState(
    val name: String = "",
    val location: String = "",
    val isActive: Boolean = true,
    val itemsCount: String = "0",
    val totalValue: String = "$0.00",
    val activeLots: String = "0",
    val soldThisMonth: String = "$0.00"
)
