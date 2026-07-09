package com.palletflow.presentation.screens.move

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.data.util.TransferNumberGenerator
import com.palletflow.domain.repository.StockRepository
import com.palletflow.domain.usecase.item.GetAllItemsUseCase
import com.palletflow.domain.usecase.transfer.CreateTransferUseCase
import com.palletflow.domain.usecase.transfer.ItemToTransfer
import com.palletflow.domain.usecase.transfer.ValidateTransferNumberUseCase
import com.palletflow.domain.usecase.warehouse.GetAllWarehousesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoveInventoryViewModel @Inject constructor(
    private val createTransferUseCase: CreateTransferUseCase,
    private val validateTransferNumberUseCase: ValidateTransferNumberUseCase,
    private val getAllItemsUseCase: GetAllItemsUseCase,
    private val getAllWarehousesUseCase: GetAllWarehousesUseCase,
    private val stockRepository: StockRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoveInventoryUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = Channel<Boolean>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    // Load dropdown data
    val availableItems = getAllItemsUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val warehouses = getAllWarehousesUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _stocks: Flow<List<com.palletflow.data.local.entity.Stock>> = authPreferences.getUserId()?.let { 
        stockRepository.getStocksByUser(it)
    } ?: flowOf(emptyList())

    val itemsWithStock: StateFlow<List<ItemWithStock>> = combine(availableItems, _stocks, _uiState) { items, stocks, state ->
        items.map { item ->
            val stockInWarehouse = stocks.find { 
                it.itemId == item.id && it.warehouseId == state.sourceWarehouseId 
            }?.quantity ?: 0
            ItemWithStock(item, stockInWarehouse)
        }.filter { it.stockQuantity > 0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        generateUniqueTransferNumber()
    }

    private fun generateUniqueTransferNumber() {
        viewModelScope.launch {
            try {
                val transferNumber = TransferNumberGenerator.generateUniqueTransferNumber(validateTransferNumberUseCase)
                _uiState.update { it.copy(transferNumber = transferNumber) }
            } catch (e: Exception) {
                Log.e("MoveInventoryViewModel", "Failed to generate transfer number", e)
                _uiState.update { it.copy(error = "Failed to generate transfer number") }
            }
        }
    }

    fun onSourceWarehouseChange(warehouseId: String) {
        _uiState.update { it.copy(sourceWarehouseId = warehouseId, sourceError = null) }
    }

    fun onDestWarehouseChange(warehouseId: String) {
        _uiState.update { it.copy(destWarehouseId = warehouseId, destError = null) }
    }

    fun addItem(item: Item, quantity: Int) {
        if (quantity <= 0) {
            _uiState.update { it.copy(itemError = "Quantity must be greater than 0") }
            return
        }

        val state = _uiState.value
        if (state.sourceWarehouseId.isBlank()) {
            _uiState.update { it.copy(itemError = "Select source warehouse first") }
            return
        }

        val newItem = MoveInventoryUiState.ItemRow(
            itemId = item.id,
            itemName = item.name,
            lotId = item.lotId ?: "",
            quantity = quantity,
            sourceWarehouseId = state.sourceWarehouseId
        )

        _uiState.update { 
            it.copy(
                items = it.items + newItem,
                itemError = null
            )
        }
    }

    fun removeItem(index: Int) {
        _uiState.update { 
            it.copy(items = it.items.filterIndexed { i, _ -> i != index })
        }
    }

    fun addNote(note: String) {
        _uiState.update { it.copy(notes = note) }
    }

    fun confirm() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sourceWarehouse = warehouses.value.find { it.id == state.sourceWarehouseId }
                
                val itemsToTransfer = state.items.map { item ->
                    ItemToTransfer(
                        itemId = item.itemId,
                        itemName = item.itemName,
                        lotId = item.lotId,
                        quantity = item.quantity,
                        sourceWarehouseId = item.sourceWarehouseId,
                        sourceWarehouseName = sourceWarehouse?.name ?: "Unknown"
                    )
                }

                val result = createTransferUseCase(
                    transferNumber = state.transferNumber,
                    sourceWarehouseId = state.sourceWarehouseId,
                    destWarehouseId = state.destWarehouseId,
                    items = itemsToTransfer,
                    notes = state.notes.ifBlank { null }
                )

                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false) }
                    _saveEvent.trySend(true)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to save transfer"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _saveEvent.trySend(false)
                }
            } catch (e: Exception) {
                Log.e("MoveInventoryViewModel", "Confirm failed", e)
                _uiState.update {
                    it.copy(isLoading = false, error = "An error occurred. Please try again.")
                }
                _saveEvent.trySend(false)
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.sourceWarehouseId.isBlank()) {
            _uiState.update { it.copy(sourceError = "Source warehouse is required") }
            isValid = false
        }

        if (state.destWarehouseId.isBlank()) {
            _uiState.update { it.copy(destError = "Destination warehouse is required") }
            isValid = false
        }

        if (state.sourceWarehouseId == state.destWarehouseId) {
            _uiState.update { it.copy(destError = "Source and destination must be different") }
            isValid = false
        }

        if (state.items.isEmpty()) {
            _uiState.update { it.copy(itemError = "At least one item is required") }
            isValid = false
        }

        return isValid
    }
}

data class MoveInventoryUiState(
    val transferNumber: String = "",
    val sourceWarehouseId: String = "",
    val sourceError: String? = null,
    val destWarehouseId: String = "",
    val destError: String? = null,
    val items: List<ItemRow> = emptyList(),
    val itemError: String? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    data class ItemRow(
        val itemId: String,
        val itemName: String,
        val lotId: String,
        val quantity: Int,
        val sourceWarehouseId: String
    )

    val totalItems: Int
        get() = items.sumOf { it.quantity }
}

data class ItemWithStock(
    val item: Item,
    val stockQuantity: Int
)
