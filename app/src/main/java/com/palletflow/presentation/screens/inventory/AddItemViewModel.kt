package com.palletflow.presentation.screens.inventory

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.local.entity.ItemType
import com.palletflow.domain.usecase.item.CreateItemUseCase
import com.palletflow.util.LocalFileHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val createItemUseCase: CreateItemUseCase,
    private val localFileHelper: LocalFileHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = Channel<Boolean>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onTypeChange(type: ItemType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onSkuChange(sku: String) {
        _uiState.update { it.copy(sku = sku) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onPhotoPathChange(path: String?) {
        _uiState.update { it.copy(photoPath = path) }
    }

    fun save() {
        if (!validate()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Save photo to internal storage if it's a Uri string
                val finalPhotoPath = state.photoPath?.let { path ->
                    if (path.startsWith("content://") || path.startsWith("file://")) {
                        localFileHelper.saveImageToInternalStorage(Uri.parse(path))
                    } else {
                        path
                    }
                }

                val result = createItemUseCase(
                    name = state.name,
                    type = state.type,
                    sku = state.sku.ifBlank { null },
                    notes = state.notes.ifBlank { null },
                    photoPath = finalPhotoPath
                )
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false) }
                    _saveEvent.trySend(true)
                } else {
                    val message = result.exceptionOrNull()?.message
                        ?: "Failed to save item"
                    _uiState.update {
                        it.copy(isLoading = false, nameError = message)
                    }
                    _saveEvent.trySend(false)
                }
            } catch (e: Exception) {
                Log.e("AddItemViewModel", "Save failed", e)
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
            _uiState.update { it.copy(nameError = "Item name cannot be empty") }
            isValid = false
        }

        return isValid
    }
}

data class AddItemUiState(
    val name: String = "",
    val nameError: String? = null,
    val type: ItemType = ItemType.SINGLE_PRODUCT,
    val sku: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val isLoading: Boolean = false
)
