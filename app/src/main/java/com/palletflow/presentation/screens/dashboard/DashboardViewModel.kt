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
    val userName: String = ""
)
