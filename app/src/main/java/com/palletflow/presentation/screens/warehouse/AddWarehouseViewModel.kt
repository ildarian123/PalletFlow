package com.palletflow.presentation.screens.warehouse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.local.entity.WarehouseType
import com.palletflow.domain.usecase.warehouse.CreateWarehouseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWarehouseViewModel @Inject constructor(
    private val createWarehouseUseCase: CreateWarehouseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWarehouseUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = Channel<Boolean>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onTypeChange(type: WarehouseType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onActiveChange(isActive: Boolean) {
        _uiState.update { it.copy(isActive = isActive) }
    }

    fun save() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = createWarehouseUseCase(
                    name = state.name,
                    address = state.address.ifBlank { null },
                    description = state.description.ifBlank { null },
                    type = state.selectedType,
                    isActive = state.isActive
                )
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false) }
                    _saveEvent.trySend(true)
                } else {
                    val message = result.exceptionOrNull()?.message
                        ?: "Failed to save warehouse"
                    _uiState.update {
                        it.copy(isLoading = false, nameError = message)
                    }
                    _saveEvent.trySend(false)
                }
            } catch (e: Exception) {
                Log.e("AddWarehouseViewModel", "Save failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nameError = "An error occurred. Please try again."
                    )
                }
                _saveEvent.trySend(false)
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Warehouse name cannot be empty") }
            isValid = false
        } else if (state.name.trim().length < 2) {
            _uiState.update { it.copy(nameError = "Name must be at least 2 characters") }
            isValid = false
        }

        return isValid
    }
}

data class AddWarehouseUiState(
    val name: String = "",
    val nameError: String? = null,
    val address: String = "",
    val description: String = "",
    val selectedType: WarehouseType = WarehouseType.MAIN,
    val isActive: Boolean = true,
    val isLoading: Boolean = false
)
