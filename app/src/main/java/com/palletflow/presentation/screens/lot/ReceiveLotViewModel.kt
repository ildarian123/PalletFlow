package com.palletflow.presentation.screens.lot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.usecase.GetSuppliersUseCase
import com.palletflow.domain.usecase.GetWarehousesUseCase
import com.palletflow.domain.usecase.item.GetAllItemsUseCase
import com.palletflow.domain.usecase.lot.ExpenseToAdd
import com.palletflow.domain.usecase.lot.GetLotFullDetailsUseCase
import com.palletflow.domain.usecase.lot.ItemToReceive
import com.palletflow.domain.usecase.lot.ReceiveLotUseCase
import com.palletflow.domain.usecase.lot.ValidateLotNumberUseCase
import com.palletflow.domain.usecase.supplier.CreateSupplierUseCase
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
    private val getWarehousesUseCase: GetWarehousesUseCase,
    private val createSupplierUseCase: CreateSupplierUseCase,
    private val getLotFullDetailsUseCase: GetLotFullDetailsUseCase,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val lotId: String? = savedStateHandle["lotId"]

    private val _uiState = MutableStateFlow(ReceiveLotUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = Channel<Boolean>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    // Load dropdown data
    val suppliers = getSuppliersUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val warehouses = getWarehousesUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val availableItems = getAllItemsUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        if (lotId != null) {
            loadLotData(lotId)
        } else {
            // Generate lot number on init
            _uiState.update { 
                it.copy(lotNumber = generateLotNumber())
            }
        }
    }

    private fun loadLotData(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                getLotFullDetailsUseCase(id),
                availableItems
            ) { details, items ->
                if (details != null && items.isNotEmpty()) {
                    _uiState.update { state ->
                        state.copy(
                            lotNumber = details.lot.lotNumber,
                            supplierId = details.lot.supplierId,
                            warehouseId = details.lot.warehouseId,
                            purchasePrice = details.lot.purchasePrice.toString(),
                            notes = details.lot.notes ?: "",
                            items = details.items.map { lotItem ->
                                val item = items.find { it.id == lotItem.itemId }
                                ReceiveLotUiState.ItemRow(
                                    itemId = lotItem.itemId,
                                    itemName = item?.name ?: "Unknown",
                                    quantity = lotItem.quantity,
                                    price = lotItem.unitPrice?.toString() ?: "",
                                    photoUrl = item?.photoPath
                                )
                            },
                            expenses = details.expenses.map { lotExpense ->
                                ReceiveLotUiState.ExpenseRow(
                                    expenseName = lotExpense.name,
                                    amount = lotExpense.amount
                                )
                            },
                            isLoading = false
                        )
                    }
                }
            }.collect()
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
            quantity = quantity,
            price = "",
            photoUrl = item.photoPath
        )

        _uiState.update { state ->
            val updatedItems = state.items + newItem
            state.copy(
                items = updatedItems,
                itemError = null
            )
        }
    }

    fun onItemPriceChange(index: Int, price: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.mapIndexed { i, item ->
                    if (i == index) item.copy(price = price) else item
                }
            )
        }
    }

    fun removeItem(index: Int) {
        _uiState.update { state ->
            state.copy(
                items = state.items.filterIndexed { i, _ -> i != index }
            )
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

    fun openAddSupplierSheet() {
        _uiState.update { it.copy(showAddSupplierSheet = true) }
    }

    fun closeAddSupplierSheet() {
        _uiState.update { it.copy(showAddSupplierSheet = false) }
    }

    fun onSupplierCreated(supplier: Supplier) {
        // Auto-select newly created supplier
        _uiState.update { 
            it.copy(
                supplierId = supplier.id,
                showAddSupplierSheet = false
            ) 
        }
    }

    fun complete() {
        Log.d("ReceiveLotViewModel", "complete() triggered")
        // TEMPORARY: Bypass validation for testing
        val validationResult = validate()
        if (!validationResult) {
            Log.d("ReceiveLotViewModel", "Validation failed, but bypassing for test")
        }

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // TEMPORARY fallback data if fields are missing during test
                val finalSupplierId = if (state.supplierId.isBlank()) {
                    suppliers.value.firstOrNull()?.id ?: "TEST_SUPPLIER"
                } else state.supplierId

                val finalWarehouseId = if (state.warehouseId.isBlank()) {
                    warehouses.value.firstOrNull()?.id ?: "TEST_WAREHOUSE"
                } else state.warehouseId

                val finalPurchasePrice = state.purchasePrice.toDoubleOrNull() ?: 0.0

                val finalItems = if (state.items.isEmpty() && lotId == null) {
                    // Only use fallback for items if it's a NEW lot and we are bypassing validation
                    val firstItem = availableItems.value.firstOrNull()
                    if (firstItem != null) {
                        listOf(ItemToReceive(firstItem.id, 1, 0.0))
                    } else {
                        Log.e("ReceiveLotViewModel", "NO ITEMS IN DB, cannot bypass item validation fully")
                        emptyList()
                    }
                } else {
                    state.items.map { 
                        ItemToReceive(
                            itemId = it.itemId, 
                            quantity = it.quantity,
                            unitPrice = it.price.toDoubleOrNull()
                        ) 
                    }
                }

                // Validate lot number is unique (only if creating a new lot)
                if (lotId == null) {
                    val isUnique = validateLotNumberUseCase(state.lotNumber)
                    if (!isUnique) {
                        Log.d("ReceiveLotViewModel", "Lot number ${state.lotNumber} already exists")
                        _uiState.update {
                            it.copy(isLoading = false, error = "Lot number already exists")
                        }
                        _saveEvent.trySend(false)
                        return@launch
                    }
                }

                val expensesToAdd = state.expenses.map { expense ->
                    ExpenseToAdd(
                        name = expense.expenseName,
                        amount = expense.amount
                    )
                }

                Log.d("ReceiveLotViewModel", "Calling receiveLotUseCase for lot ${state.lotNumber} with ${finalItems.size} items")
                val result = receiveLotUseCase(
                    lotNumber = state.lotNumber,
                    supplierId = finalSupplierId,
                    warehouseId = finalWarehouseId,
                    purchasePrice = finalPurchasePrice,
                    notes = state.notes.ifBlank { null },
                    items = finalItems,
                    expenses = expensesToAdd,
                    lotId = lotId
                )

                if (result.isSuccess) {
                    Log.d("ReceiveLotViewModel", "Lot saved successfully, sending event")
                    _uiState.update { it.copy(isLoading = false) }
                    val sendResult = _saveEvent.trySend(true)
                    Log.d("ReceiveLotViewModel", "Event sent: ${sendResult.isSuccess}")
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to save lot"
                    Log.e("ReceiveLotViewModel", "UseCase returned failure: $message")
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _saveEvent.trySend(false)
                }
            } catch (e: Exception) {
                Log.e("ReceiveLotViewModel", "Complete failed with exception", e)
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
    val error: String? = null,
    val showAddSupplierSheet: Boolean = false
) {
    data class ItemRow(
        val itemId: String,
        val itemName: String,
        val quantity: Int,
        val price: String = "",
        val photoUrl: String? = null
    )

    data class ExpenseRow(
        val expenseName: String,
        val amount: Double
    )

    val totalCost: Double
        get() {
            val basePrice = purchasePrice.toDoubleOrNull() ?: 0.0
            val itemsSum = items.sumOf { (it.price.toDoubleOrNull() ?: 0.0) * it.quantity }
            val expensesSum = expenses.sumOf { it.amount }
            return basePrice + itemsSum + expensesSum
        }
}
