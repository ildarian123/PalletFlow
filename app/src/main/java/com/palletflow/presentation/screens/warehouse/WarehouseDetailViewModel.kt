package com.palletflow.presentation.screens.warehouse

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WarehouseDetailViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(WarehouseDetailUiState())
    val uiState = _uiState.asStateFlow()
}

data class WarehouseDetailUiState(
    val name: String = "",
    val location: String = "",
    val isActive: Boolean = true,
    val itemsCount: String = "0",
    val totalValue: String = "$0.00",
    val activeLots: String = "0",
    val soldThisMonth: String = "$0.00"
)
