package com.palletflow.presentation.screens.supplier

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SupplierDetailViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierDetailUiState())
    val uiState = _uiState.asStateFlow()
}

data class SupplierDetailUiState(val name: String = "")