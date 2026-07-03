package com.palletflow.presentation.screens.warehouse

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.domain.usecase.warehouse.CreateWarehouseUseCase
import com.palletflow.domain.usecase.warehouse.DeleteWarehouseUseCase
import com.palletflow.domain.usecase.warehouse.GetWarehouseUseCase
import com.palletflow.domain.usecase.warehouse.UpdateWarehouseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WarehouseAction {
    SAVE, DELETE
}

@HiltViewModel
class AddWarehouseViewModel @Inject constructor(
    private val createWarehouseUseCase: CreateWarehouseUseCase,
    private val updateWarehouseUseCase: UpdateWarehouseUseCase,
    private val deleteWarehouseUseCase: DeleteWarehouseUseCase,
    private val getWarehouseUseCase: GetWarehouseUseCase,
    private val authPreferences: AuthPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val warehouseId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(AddWarehouseUiState())
    val uiState = _uiState.asStateFlow()

    private val _actionEvent = Channel<WarehouseAction>(Channel.BUFFERED)
    val actionEvent = _actionEvent.receiveAsFlow()

    init {
        warehouseId?.let { loadWarehouse(it) }
    }

    private fun loadWarehouse(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val warehouse = getWarehouseUseCase(id)
            if (warehouse != null) {
                _uiState.update {
                    it.copy(
                        name = warehouse.name,
                        address = warehouse.address ?: "",
                        description = warehouse.description ?: "",
                        isActive = warehouse.isActive,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, nameError = "Warehouse not found") }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
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
                val result = if (warehouseId != null) {
                    updateWarehouseUseCase(
                        id = warehouseId,
                        name = state.name,
                        address = state.address.ifBlank { null },
                        description = state.description.ifBlank { null },
                        isActive = state.isActive
                    )
                } else {
                    createWarehouseUseCase(
                        name = state.name,
                        address = state.address.ifBlank { null },
                        description = state.description.ifBlank { null },
                        isActive = state.isActive
                    )
                }

                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false) }
                    _actionEvent.trySend(WarehouseAction.SAVE)
                } else {
                    val message = result.exceptionOrNull()?.message
                        ?: "Failed to save warehouse"
                    _uiState.update {
                        it.copy(isLoading = false, nameError = message)
                    }
                }
            } catch (e: Exception) {
                Log.e("AddWarehouseViewModel", "Save failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nameError = "An error occurred. Please try again."
                    )
                }
            }
        }
    }

    fun delete() {
        val id = warehouseId ?: return
        val userId = authPreferences.getUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = deleteWarehouseUseCase(id, userId)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false) }
                    _actionEvent.trySend(WarehouseAction.DELETE)
                } else {
                    val message = result.exceptionOrNull()?.message
                        ?: "Failed to delete warehouse"
                    _uiState.update {
                        it.copy(isLoading = false, nameError = message)
                    }
                }
            } catch (e: Exception) {
                Log.e("AddWarehouseViewModel", "Delete failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nameError = "An error occurred. Please try again."
                    )
                }
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
    val isActive: Boolean = true,
    val isLoading: Boolean = false
)
