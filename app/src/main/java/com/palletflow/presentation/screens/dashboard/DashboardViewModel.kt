package com.palletflow.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.UserDao
import com.palletflow.data.local.entity.TransactionType
import com.palletflow.domain.repository.TransactionRepository
import com.palletflow.domain.usecase.report.GetFinancialOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao,
    private val transactionRepository: TransactionRepository,
    private val getFinancialOverviewUseCase: GetFinancialOverviewUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = authPreferences.getUserId() ?: return

        viewModelScope.launch {
            // Get Month-to-date range
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis

            // 1. Load Overview Metrics (This Month)
            val overviewResult = getFinancialOverviewUseCase(startOfMonth, now)
            if (overviewResult.isSuccess) {
                val overview = overviewResult.getOrNull()
                _uiState.update { state ->
                    state.copy(
                        totalProfit = String.format("$%,.2f", overview?.totalProfit ?: 0.0),
                        itemsSold = overview?.transactionCount?.toString() ?: "0"
                    )
                }
            }

            // 2. Load Recent Activities (Transactions)
            transactionRepository.getAllTransactions(userId).map { transactions ->
                transactions.sortedByDescending { it.createdAt }.take(10).map { tx ->
                    val sign = if (tx.type == TransactionType.SALE) "+" else "-"
                    RecentActivityUiState(
                        title = tx.notes ?: "${tx.type.name} Transaction",
                        detail = String.format("%s$%,.2f", sign, tx.amount),
                        detailColorType = if (tx.type == TransactionType.SALE) ActivityDetailColorType.SUCCESS else ActivityDetailColorType.NORMAL
                    )
                }
            }.onEach { activities ->
                _uiState.update { it.copy(recentActivities = activities) }
            }.launchIn(viewModelScope)
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userId = authPreferences.getUserId()
            if (userId != null) {
                val user = userDao.getUserById(userId)
                if (user != null) {
                    _uiState.update { it.copy(userName = user.businessName) }
                }
            }
        }
    }

    fun logout() {
        authPreferences.clearCredentials()
    }
}

data class DashboardUiState(
    val userName: String = "",
    val totalProfit: String = "$0.00",
    val itemsSold: String = "0",
    val recentActivities: List<RecentActivityUiState> = emptyList()
)

data class RecentActivityUiState(
    val title: String,
    val detail: String,
    val detailColorType: ActivityDetailColorType
)

enum class ActivityDetailColorType {
    SUCCESS, NORMAL
}
