package com.palletflow.presentation.screens.reports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.domain.usecase.report.*
import com.palletflow.domain.usecase.warehouse.GetAllWarehousesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getFinancialOverviewUseCase: GetFinancialOverviewUseCase,
    private val getLotProfitabilityUseCase: GetLotProfitabilityUseCase,
    private val getPlatformSalesUseCase: GetPlatformSalesUseCase,
    private val getAllWarehousesUseCase: GetAllWarehousesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilterReport.MONTH)
    private val _selectedWarehouse = MutableStateFlow<String?>(null)

    val warehouses = getAllWarehousesUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Observe filters and reload data
        combine(_selectedDateFilter, _selectedWarehouse) { dateFilter, warehouseId ->
            Pair(dateFilter, warehouseId)
        }.onEach { (dateFilter, warehouseId) ->
            loadReportData(dateFilter, warehouseId)
        }.launchIn(viewModelScope)
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

    private fun loadReportData(dateFilter: DateFilterReport, warehouseId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dateRange = getDateRange(dateFilter)
                
                val overviewResult = getFinancialOverviewUseCase(
                    dateRange.first,
                    dateRange.second,
                    warehouseId
                )
                
                val lotsResult = getLotProfitabilityUseCase(dateRange.first, dateRange.second, warehouseId)
                val platformResult = getPlatformSalesUseCase(dateRange.first, dateRange.second, warehouseId)

                if (overviewResult.isSuccess && lotsResult.isSuccess && platformResult.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            overview = overviewResult.getOrNull(),
                            lotProfits = lotsResult.getOrNull() ?: emptyList(),
                            platformSales = platformResult.getOrNull() ?: emptyList(),
                            error = null
                        )
                    }
                } else {
                    val error = listOfNotNull(
                        overviewResult.exceptionOrNull()?.message,
                        lotsResult.exceptionOrNull()?.message,
                        platformResult.exceptionOrNull()?.message
                    ).firstOrNull() ?: "Failed to load reports"
                    _uiState.update { it.copy(isLoading = false, error = error) }
                }
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Failed to load report data", e)
                _uiState.update { it.copy(isLoading = false, error = "An error occurred") }
            }
        }
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
            DateFilterReport.CUSTOM -> 0L // Handle custom if needed
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
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
