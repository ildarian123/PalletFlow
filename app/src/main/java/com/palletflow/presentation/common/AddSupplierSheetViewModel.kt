package com.palletflow.presentation.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.local.entity.Supplier
import com.palletflow.domain.usecase.supplier.CreateSupplierUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSupplierSheetViewModel @Inject constructor(
    private val createSupplierUseCase: CreateSupplierUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSupplierSheetUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = Channel<Supplier>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    fun onSupplierNameChange(name: String) {
        _uiState.update { it.copy(supplierName = name, error = null) }
    }

    fun save() {
        val state = _uiState.value

        if (state.supplierName.isBlank()) {
            _uiState.update { it.copy(error = "Supplier name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = createSupplierUseCase(state.supplierName)
                if (result.isSuccess) {
                    val supplier = result.getOrNull()!!
                    _uiState.update { it.copy(isLoading = false) }
                    _saveEvent.trySend(supplier)
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to create supplier"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                }
            } catch (e: Exception) {
                Log.e("AddSupplierSheetViewModel", "Save failed", e)
                _uiState.update {
                    it.copy(isLoading = false, error = "An error occurred. Please try again.")
                }
            }
        }
    }
}

data class AddSupplierSheetUiState(
    val supplierName: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)
