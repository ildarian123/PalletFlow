package com.palletflow.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadRecentActivities()
    }
    //TODO delete
    private fun loadRecentActivities() {
        _uiState.update { 
            it.copy(
                totalProfit = "+$1,240.00",
                itemsSold = "48",
                activeLots = "12",
                avgProfit = "$25.80",
                recentActivities = listOf(
                    RecentActivityUiState(
                        title = "Sold Samsung Tablet",
                        detail = "+$120.00",
                        detailColorType = ActivityDetailColorType.SUCCESS
                    ),
                    RecentActivityUiState(
                        title = "Sold Nike Air Max",
                        detail = "+$45.00",
                        detailColorType = ActivityDetailColorType.SUCCESS
                    ),
                    RecentActivityUiState(
                        title = "Shipping Cost",
                        detail = "-$12.00",
                        detailColorType = ActivityDetailColorType.NORMAL
                    ),
                    RecentActivityUiState(
                        title = "Received Lot #1043",
                        detail = "-$850.00",
                        detailColorType = ActivityDetailColorType.NORMAL
                    )
                )
            )
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
    val activeLots: String = "0",
    val avgProfit: String = "$0.00",
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
