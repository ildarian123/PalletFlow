package com.palletflow.presentation.screens.sales

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.local.entity.Transaction
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.usecase.transaction.DeleteTransactionUseCase
import com.palletflow.domain.usecase.transaction.GetTransactionsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val getIncomingTransactionsUseCase: GetTransactionsByTypeUseCase,
    private val getSalesTransactionsUseCase: GetTransactionsByTypeUseCase,
    private val getTransfersTransactionsUseCase: GetTransactionsByTypeUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(0)
    private val _selectedDateFilter = MutableStateFlow(DateFilter.MONTH)
    private val _selectedWarehouse = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _deleteEvent = Channel<DeleteEvent>(Channel.BUFFERED)
    val deleteEvent = _deleteEvent.receiveAsFlow()

    sealed class DeleteEvent {
        data class Error(val message: String) : DeleteEvent()
        object Success : DeleteEvent()
    }

    private val transactionsFlow = combine(
        getIncomingTransactionsUseCase(TransactionType.INCOMING),
        getSalesTransactionsUseCase(TransactionType.SALE),
        getTransfersTransactionsUseCase(TransactionType.TRANSFER)
    ) { incoming, sales, transfers ->
        Triple(incoming, sales, transfers)
    }

    private val filtersFlow = combine(
        _searchQuery,
        _selectedTab,
        _selectedDateFilter,
        _selectedWarehouse
    ) { query, tab, dateFilter, warehouseId ->
        Filters(query, tab, dateFilter, warehouseId)
    }

    val uiState: StateFlow<SalesUiState> = combine(
        transactionsFlow,
        filtersFlow,
        _isLoading,
        _error
    ) { transactions, filters, loading, error ->
        val (incoming, sales, transfers) = transactions
        val currentList = when (filters.tab) {
            0 -> incoming
            1 -> sales
            else -> transfers
        }
        SalesUiState(
            incomingTransactions = incoming,
            salesTransactions = sales,
            transferTransactions = transfers,
            filteredTransactions = filterTransactions(currentList, filters.query, filters.dateFilter, filters.warehouseId),
            selectedTab = filters.tab,
            searchQuery = filters.query,
            selectedDateFilter = filters.dateFilter,
            selectedWarehouse = filters.warehouseId,
            isLoading = loading,
            error = error
        )
    }.onStart {
        _isLoading.value = false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SalesUiState(isLoading = true)
    )

    fun onTabChange(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDateFilterChange(filter: DateFilter) {
        _selectedDateFilter.value = filter
    }

    fun onWarehouseFilterChange(warehouseId: String?) {
        _selectedWarehouse.value = warehouseId
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                val result = deleteTransactionUseCase(transactionId)
                if (result.isSuccess) {
                    _deleteEvent.trySend(DeleteEvent.Success)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to delete"
                    _deleteEvent.trySend(DeleteEvent.Error(message))
                }
            } catch (e: Exception) {
                Log.e("SalesViewModel", "Delete failed", e)
                _deleteEvent.trySend(DeleteEvent.Error("An error occurred. Please try again."))
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun filterTransactions(
        transactions: List<Transaction>,
        query: String,
        dateFilter: DateFilter,
        warehouseId: String?
    ): List<Transaction> {
        return transactions.filter { transaction ->
            val now = System.currentTimeMillis()
            val dateRange = when (dateFilter) {
                DateFilter.TODAY -> {
                    val startOfDay = now - (now % 86400000)
                    startOfDay to now
                }
                DateFilter.WEEK -> {
                    val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
                    weekAgo to now
                }
                DateFilter.MONTH -> {
                    val monthAgo = now - (30 * 24 * 60 * 60 * 1000)
                    monthAgo to now
                }
                DateFilter.CUSTOM -> Long.MIN_VALUE to Long.MAX_VALUE
            }
            val matchesDate = transaction.createdAt >= dateRange.first && 
                            transaction.createdAt <= dateRange.second

            val matchesWarehouse = warehouseId == null ||
                (transaction.warehouseFromId == warehouseId || 
                 transaction.warehouseToId == warehouseId)

            val matchesSearch = query.isBlank() ||
                transaction.notes?.contains(query, ignoreCase = true) == true

            matchesDate && matchesWarehouse && matchesSearch
        }
    }

    private data class Filters(
        val query: String,
        val tab: Int,
        val dateFilter: DateFilter,
        val warehouseId: String?
    )
}

enum class DateFilter {
    TODAY, WEEK, MONTH, CUSTOM
}

data class SalesUiState(
    val incomingTransactions: List<Transaction> = emptyList(),
    val salesTransactions: List<Transaction> = emptyList(),
    val transferTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedDateFilter: DateFilter = DateFilter.MONTH,
    val selectedWarehouse: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
