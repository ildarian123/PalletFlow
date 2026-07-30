package com.palletflow.presentation.screens.reports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.domain.usecase.report.*
import com.palletflow.domain.usecase.warehouse.GetAllWarehousesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val observeFinancialOverviewUseCase: ObserveFinancialOverviewUseCase,
    private val observeLotProfitabilityUseCase: ObserveLotProfitabilityUseCase,
    private val observePlatformSalesUseCase: ObservePlatformSalesUseCase,
    private val getAllWarehousesUseCase: GetAllWarehousesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilterReport.MONTH)
    private val _selectedWarehouse = MutableStateFlow<String?>(null)
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val warehouses = getAllWarehousesUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        _refreshTrigger.tryEmit(Unit)

        // Observe filters and reload data automatically
        combine(
            _selectedDateFilter,
            _selectedWarehouse,
            _refreshTrigger
        ) { dateFilter, warehouseId, _ ->
            dateFilter to warehouseId
        }.flatMapLatest { (dateFilter, warehouseId) ->
            val range = getDateRange(dateFilter)
            
            combine(
                observeFinancialOverviewUseCase(range.first, range.second),
                observeLotProfitabilityUseCase(range.first, range.second, warehouseId),
                observePlatformSalesUseCase(range.first, range.second, warehouseId)
            ) { overview, lots, platforms ->
                Triple(overview, lots, platforms)
            }.onStart {
                _uiState.update { it.copy(isLoading = true) }
            }
        }.onEach { (overview, lots, platforms) ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    overview = overview,
                    lotProfits = lots,
                    platformSales = platforms,
                    error = null
                )
            }
        }.catch { e ->
            Log.e("ReportsViewModel", "Error loading reports", e)
            _uiState.update { it.copy(isLoading = false, error = "Failed to load reports") }
        }.launchIn(viewModelScope)
    }

    fun refresh() {
        _refreshTrigger.tryEmit(Unit)
    }

    fun onTabChange(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun onDateFilterChange(filter: DateFilterReport) {
        _selectedDateFilter.value = filter
    }

    fun onWarehouseChange(warehouseId: String?) {
        _selectedWarehouse.value = warehouseId
    }

    private fun getDateRange(filter: DateFilterReport): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val start = when (filter) {
            DateFilterReport.TODAY -> calendar.timeInMillis
            DateFilterReport.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            DateFilterReport.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            DateFilterReport.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            DateFilterReport.CUSTOM -> 0L
        }
        return start to now
    }
}

enum class DateFilterReport {
    TODAY, WEEK, MONTH, YEAR, CUSTOM
}

data class ReportsUiState(
    val overview: FinancialOverview? = null,
    val lotProfits: List<LotProfitData> = emptyList(),
    val platformSales: List<PlatformSalesData> = emptyList(),
    val selectedTab: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)
