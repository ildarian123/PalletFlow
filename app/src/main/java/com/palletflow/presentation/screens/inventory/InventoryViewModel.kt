package com.palletflow.presentation.screens.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.data.local.entity.Warehouse
import com.palletflow.domain.usecase.item.DeleteItemUseCase
import com.palletflow.domain.usecase.item.GetAllItemsUseCase
import com.palletflow.domain.usecase.warehouse.DeleteWarehouseUseCase
import com.palletflow.domain.usecase.warehouse.GetAllWarehousesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DeleteEvent {
    data class Error(val message: String) : DeleteEvent()
    object Success : DeleteEvent()
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getAllItemsUseCase: GetAllItemsUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val getAllWarehousesUseCase: GetAllWarehousesUseCase,
    private val deleteWarehouseUseCase: DeleteWarehouseUseCase,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _deleteEvent = Channel<DeleteEvent>(Channel.BUFFERED)
    val deleteEvent = _deleteEvent.receiveAsFlow()

    private val _warehouseDeleteEvent = Channel<DeleteEvent>(Channel.BUFFERED)
    val warehouseDeleteEvent = _warehouseDeleteEvent.receiveAsFlow()

    init {
        loadItems()
        loadWarehouses()
    }

    fun onTabChange(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getAllItemsUseCase()
                    .catch { e ->
                        Log.e("InventoryViewModel", "Failed to load items", e)
                        _uiState.update {
                            it.copy(isLoading = false, error = "Failed to load items")
                        }
                    }
                    .collect { items ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                items = items,
                                filteredItems = filterItems(items, _searchQuery.value)
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Failed to load items", e)
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load items")
                }
            }
        }
    }

    fun loadWarehouses() {
        viewModelScope.launch {
            try {
                getAllWarehousesUseCase()
                    .catch { e ->
                        Log.e("InventoryViewModel", "Failed to load warehouses", e)
                        _uiState.update {
                            it.copy(error = "Failed to load warehouses")
                        }
                    }
                    .collect { warehouses ->
                        _uiState.update {
                            it.copy(
                                warehouses = warehouses,
                                filteredWarehouses = filterWarehouses(warehouses, _searchQuery.value)
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Failed to load warehouses", e)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredItems = filterItems(it.items, query, it.selectedTypeFilter),
                filteredWarehouses = filterWarehouses(it.warehouses, query)
            )
        }
    }

    fun onTypeFilterChange(type: ItemType?) {
        _uiState.update {
            it.copy(
                selectedTypeFilter = type,
                filteredItems = filterItems(it.items, _searchQuery.value, type)
            )
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            try {
                val result = deleteItemUseCase(item)
                if (result.isSuccess) {
                    _deleteEvent.trySend(DeleteEvent.Success)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to delete item"
                    _deleteEvent.trySend(DeleteEvent.Error(message))
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Delete failed", e)
                _deleteEvent.trySend(DeleteEvent.Error("An error occurred. Please try again."))
            }
        }
    }

    fun deleteWarehouse(warehouseId: String) {
        val userId = authPreferences.getUserId() ?: return
        viewModelScope.launch {
            try {
                val result = deleteWarehouseUseCase(warehouseId, userId)
                if (result.isSuccess) {
                    _warehouseDeleteEvent.trySend(DeleteEvent.Success)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to delete warehouse"
                    _warehouseDeleteEvent.trySend(DeleteEvent.Error(message))
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Delete warehouse failed", e)
                _warehouseDeleteEvent.trySend(DeleteEvent.Error("An error occurred. Please try again."))
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun filterItems(
        items: List<Item>,
        query: String,
        typeFilter: ItemType? = _uiState.value.selectedTypeFilter
    ): List<Item> {
        return items.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.sku?.contains(query, ignoreCase = true) == true

            val matchesType = typeFilter == null || item.type == typeFilter

            matchesQuery && matchesType
        }
    }

    private fun filterWarehouses(
        warehouses: List<Warehouse>,
        query: String
    ): List<Warehouse> {
        return warehouses.filter { warehouse ->
            query.isBlank() ||
            warehouse.name.contains(query, ignoreCase = true) ||
            warehouse.address?.contains(query, ignoreCase = true) == true
        }
    }
}

data class InventoryUiState(
    val items: List<Item> = emptyList(),
    val filteredItems: List<Item> = emptyList(),
    val warehouses: List<Warehouse> = emptyList(),
    val filteredWarehouses: List<Warehouse> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: ItemType? = null,
    val selectedTab: Int = 0, // 0 = Items, 1 = Warehouses
    val isLoading: Boolean = false,
    val error: String? = null
)
