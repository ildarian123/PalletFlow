package com.palletflow.presentation.screens.supplier

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.GetSuppliersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SupplierListViewModel @Inject constructor(
    private val getSuppliersUseCase: GetSuppliersUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupplierListUiState())
    val uiState = _uiState.asStateFlow()
}

data class SupplierListUiState(val suppliers: List<String> = emptyList())