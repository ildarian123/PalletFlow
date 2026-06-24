package com.palletflow.presentation.screens.warehouse

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class WarehouseDetailViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(WarehouseDetailUiState())
    val uiState = _uiState.asStateFlow()
}

data class WarehouseDetailUiState(val name: String = "")