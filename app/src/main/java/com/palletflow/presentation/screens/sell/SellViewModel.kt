package com.palletflow.presentation.screens.sell

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.data.util.SaleNumberGenerator
import com.palletflow.domain.usecase.item.GetAllItemsUseCase
import com.palletflow.domain.usecase.sale.*
import com.palletflow.domain.usecase.warehouse.GetAllWarehousesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellViewModel @Inject constructor(
    private val createSaleUseCase: CreateSaleUseCase,
    private val validateSaleNumberUseCase: ValidateSaleNumberUseCase,
    private val getAllItemsUseCase: GetAllItemsUseCase,
    private val getAllWarehousesUseCase: GetAllWarehousesUseCase,
    private val getSaleDetailsUseCase: GetSaleDetailsUseCase,
    private val stockRepository: com.palletflow.domain.repository.StockRepository,
    private val authPreferences: AuthPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val saleId: String? = savedStateHandle["saleId"]

    private val _uiState = MutableStateFlow(SellUiState())
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
                it.itemId == item.id && it.warehouseId == state.warehouseId 
            }?.quantity ?: 0
            ItemWithStock(item, stockInWarehouse)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        if (saleId != null) {
            loadSaleData(saleId)
        } else {
            generateUniqueSaleNumber()
        }
    }

    private fun loadSaleData(id: String) {
        val userId = authPreferences.getUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val details = getSaleDetailsUseCase(id, userId)
                if (details != null) {
                    // We need to match ItemRow with real item names
                    // This is tricky if items were deleted, but let's assume they exist
                    _uiState.update { state ->
                        state.copy(
                            saleNumber = details.transaction.documentNumber ?: "",
                            platform = details.transaction.platform ?: "",
                            warehouseId = details.transaction.warehouseFromId ?: "",
                            items = details.items.map { saleItem ->
                                SellUiState.ItemRow(
                                    itemId = saleItem.itemId,
                                    itemName = "Loading...", // Will update once availableItems load
                                    lotId = saleItem.lotId,
                                    quantity = saleItem.quantity,
                                    sellingPrice = saleItem.sellingPrice
                                )
                            },
                            expenses = details.expenses.map { expense ->
                                SellUiState.ExpenseRow(expense.name, expense.amount)
                            },
                            isLoading = false
                        )
                    }
                    
                    // Update item names when availableItems are ready
                    availableItems.collectLatest { items ->
                        if (items.isNotEmpty()) {
                            _uiState.update { state ->
                                state.copy(
                                    items = state.items.map { row ->
                                        val item = items.find { it.id == row.itemId }
                                        row.copy(itemName = item?.name ?: "Unknown Item")
                                    }
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Sale not found") }
                }
            } catch (e: Exception) {
                Log.e("SellViewModel", "Failed to load sale data", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to load sale data") }
            }
        }
    }

    private fun generateUniqueSaleNumber() {
        viewModelScope.launch {
            try {
                val saleNumber = SaleNumberGenerator.generateUniqueSaleNumber(validateSaleNumberUseCase)
                _uiState.update { it.copy(saleNumber = saleNumber) }
            } catch (e: Exception) {
                Log.e("SellViewModel", "Failed to generate sale number", e)
                _uiState.update { it.copy(error = "Failed to generate sale number") }
            }
        }
    }

    fun onPlatformChange(platform: String) {
        _uiState.update { it.copy(platform = platform, platformError = null) }
    }

    fun onWarehouseChange(warehouseId: String) {
        _uiState.update { it.copy(warehouseId = warehouseId, warehouseError = null) }
    }

    fun addItem(item: Item, quantity: Int) {
        if (quantity <= 0) {
            _uiState.update { it.copy(itemError = "Quantity must be greater than 0") }
            return
        }

        val newItem = SellUiState.ItemRow(
            itemId = item.id,
            itemName = item.name,
            lotId = item.lotId ?: "",
            quantity = quantity,
            sellingPrice = 0.0  // User will enter this
        )

        _uiState.update { 
            it.copy(
                items = it.items + newItem,
                itemError = null
            )
        }
    }

    fun updateItemPrice(index: Int, price: Double) {
        _uiState.update { state ->
            val updated = state.items.toMutableList()
            if (index >= 0 && index < updated.size) {
                updated[index] = updated[index].copy(sellingPrice = price)
            }
            state.copy(items = updated)
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

        val newExpense = SellUiState.ExpenseRow(name, amount)
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
                val itemsToSell = state.items.map { item ->
                    ItemToSell(
                        itemId = item.itemId,
                        itemName = item.itemName,
                        lotId = item.lotId,
                        quantity = item.quantity,
                        sellingPrice = item.sellingPrice
                    )
                }

                val result = createSaleUseCase(
                    saleNumber = state.saleNumber,
                    platform = state.platform,
                    warehouseId = state.warehouseId,
                    items = itemsToSell,
                    expenses = state.expenses.map { ExpenseToAdd(it.name, it.amount) },
                    saleId = saleId
                )

                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false) }
                    _saveEvent.trySend(true)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to save sale"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _saveEvent.trySend(false)
                }
            } catch (e: Exception) {
                Log.e("SellViewModel", "Complete failed", e)
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

        if (state.platform.isBlank()) {
            _uiState.update { it.copy(platformError = "Platform is required") }
            isValid = false
        }

        if (state.warehouseId.isBlank()) {
            _uiState.update { it.copy(warehouseError = "Warehouse is required") }
            isValid = false
        }

        if (state.items.isEmpty()) {
            _uiState.update { it.copy(itemError = "At least one item is required") }
            isValid = false
        }

        state.items.forEach { item ->
            if (item.sellingPrice <= 0) {
                _uiState.update { it.copy(itemError = "All items must have valid selling price") }
                isValid = false
            }
        }

        return isValid
    }
}

data class SellUiState(
    val saleNumber: String = "",
    val platform: String = "",
    val platformError: String? = null,
    val warehouseId: String = "",
    val warehouseError: String? = null,
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
        val lotId: String,
        val quantity: Int,
        val sellingPrice: Double
    )

    data class ExpenseRow(
        val name: String,
        val amount: Double
    )

    val totalRevenue: Double
        get() = items.sumOf { it.sellingPrice * it.quantity }

    val totalExpenses: Double
        get() = expenses.sumOf { it.amount }

    val netRevenue: Double
        get() = totalRevenue - totalExpenses
}

data class ItemWithStock(
    val item: Item,
    val stockQuantity: Int
)
