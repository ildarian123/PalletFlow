package com.palletflow.presentation.screens.lot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.domain.usecase.GetSuppliersUseCase
import com.palletflow.domain.usecase.GetWarehousesUseCase
import com.palletflow.domain.usecase.item.GetAllItemsUseCase
import com.palletflow.domain.usecase.lot.ExpenseToAdd
import com.palletflow.domain.usecase.lot.ItemToReceive
import com.palletflow.domain.usecase.lot.ReceiveLotUseCase
import com.palletflow.domain.usecase.lot.ValidateLotNumberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReceiveLotViewModel @Inject constructor(
    private val receiveLotUseCase: ReceiveLotUseCase,
    private val validateLotNumberUseCase: ValidateLotNumberUseCase,
    private val getAllItemsUseCase: GetAllItemsUseCase,
    private val getSuppliersUseCase: GetSuppliersUseCase,
    private val getWarehousesUseCase: GetWarehousesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiveLotUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = Channel<Boolean>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    // Load dropdown data
    val suppliers = getSuppliersUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val warehouses = getWarehousesUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val availableItems = getAllItemsUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Generate lot number on init
        _uiState.update { 
            it.copy(lotNumber = generateLotNumber())
        }
    }

    fun onSupplierChange(supplierId: String) {
        _uiState.update { it.copy(supplierId = supplierId, supplierError = null) }
    }

    fun onWarehouseChange(warehouseId: String) {
        _uiState.update { it.copy(warehouseId = warehouseId, warehouseError = null) }
    }

    fun onPurchasePriceChange(price: String) {
        _uiState.update { it.copy(purchasePrice = price, priceError = null) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun addItem(itemId: String, quantity: Int) {
        if (quantity <= 0) {
            _uiState.update { it.copy(itemError = "Quantity must be greater than 0") }
            return
        }

        val item = availableItems.value.find { it.id == itemId }
        if (item == null) {
            _uiState.update { it.copy(itemError = "Item not found") }
            return
        }

        val newItem = ReceiveLotUiState.ItemRow(
            itemId = itemId,
            itemName = item.name,
            quantity = quantity
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

    fun addExpense(name: String, amount: Double) {
        if (name.isBlank()) {
            _uiState.update { it.copy(expenseError = "Expense name required") }
            return
        }
        if (amount <= 0) {
            _uiState.update { it.copy(expenseError = "Amount must be greater than 0") }
            return
        }

        val newExpense = ReceiveLotUiState.ExpenseRow(
            expenseName = name,
            amount = amount
        )

        _uiState.update { 
            it.copy(
                expenses = it.expenses + newExpense,
                expenseError = null
            )
        }
    }

    fun removeExpense(index: Int) {
        _uiState.update { 
            it.copy(expenses = it.expenses.filterIndexed { i, _ -> i != index })
        }
    }

    fun complete() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Validate lot number is unique
                val isUnique = validateLotNumberUseCase(state.lotNumber)
                if (!isUnique) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Lot number already exists")
                    }
                    _saveEvent.trySend(false)
                    return@launch
                }

                val itemsToReceive = state.items.map { item ->
                    ItemToReceive(
                        itemId = item.itemId,
                        quantity = item.quantity
                    )
                }

                val expensesToAdd = state.expenses.map { expense ->
                    ExpenseToAdd(
                        name = expense.expenseName,
                        amount = expense.amount
                    )
                }

                val result = receiveLotUseCase(
                    supplierId = state.supplierId,
                    warehouseId = state.warehouseId,
                    purchasePrice = state.purchasePrice.toDoubleOrNull() ?: 0.0,
                    notes = state.notes.ifBlank { null },
                    items = itemsToReceive,
                    expenses = expensesToAdd
                )

                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false) }
                    _saveEvent.trySend(true)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to save lot"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _saveEvent.trySend(false)
                }
            } catch (e: Exception) {
                Log.e("ReceiveLotViewModel", "Complete failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "An error occurred. Please try again."
                    )
                }
                _saveEvent.trySend(false)
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.supplierId.isBlank()) {
            _uiState.update { it.copy(supplierError = "Supplier is required") }
            isValid = false
        }

        if (state.warehouseId.isBlank()) {
            _uiState.update { it.copy(warehouseError = "Warehouse is required") }
            isValid = false
        }

        if (state.purchasePrice.isBlank() || state.purchasePrice.toDoubleOrNull() == null) {
            _uiState.update { it.copy(priceError = "Valid purchase price required") }
            isValid = false
        }

        if (state.items.isEmpty()) {
            _uiState.update { it.copy(itemError = "At least one item is required") }
            isValid = false
        }

        return isValid
    }

    private fun generateLotNumber(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "LOT-$date-$random"
    }
}

data class ReceiveLotUiState(
    val lotNumber: String = "",
    val supplierId: String = "",
    val supplierError: String? = null,
    val warehouseId: String = "",
    val warehouseError: String? = null,
    val purchasePrice: String = "",
    val priceError: String? = null,
    val notes: String = "",
    val items: List<ItemRow> = emptyList(),
    val itemError: String? = null,
    val expenses: List<ExpenseRow> = emptyList(),
    val expenseError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    data class ItemRow(
        val itemId: String,
        val itemName: String,
        val quantity: Int
    )

    data class ExpenseRow(
        val expenseName: String,
        val amount: Double
    )

    val totalCost: Double
        get() = (purchasePrice.toDoubleOrNull() ?: 0.0) + expenses.sumOf { it.amount }
}
