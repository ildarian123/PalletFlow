package com.palletflow.presentation.screens.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.domain.usecase.item.GetAllItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getAllItemsUseCase: GetAllItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadItems()
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredItems = filterItems(it.items, query)
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
}

data class InventoryUiState(
    val items: List<Item> = emptyList(),
    val filteredItems: List<Item> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: ItemType? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
