package com.palletflow.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import com.palletflow.data.datastore.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authPreferences: AuthPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    fun logout() {
        authPreferences.clearCredentials()
    }
}

data class DashboardUiState(val data: String = "")