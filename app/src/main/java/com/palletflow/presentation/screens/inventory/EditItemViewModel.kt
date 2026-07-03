package com.palletflow.presentation.screens.inventory

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.data.local.entity.ItemType
import com.palletflow.domain.repository.ItemRepository
import com.palletflow.domain.usecase.item.DeleteItemUseCase
import com.palletflow.domain.usecase.item.UpdateItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditItemEvent {
    object SaveSuccess : EditItemEvent()
    object DeleteSuccess : EditItemEvent()
}

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val authPreferences: AuthPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"]) {
        "itemId is required for EditItemScreen"
    }

    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<EditItemEvent>()
    val event = _event.receiveAsFlow()

    private var originalItem: Item? = null

    init {
        loadItem()
    }

    private fun loadItem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authPreferences.getUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                return@launch
            }

            itemRepository.getItem(itemId, userId).collect { item ->
                if (item != null) {
                    originalItem = item
                    _uiState.update {
                        it.copy(
                            name = item.name,
                            type = item.type,
                            sku = item.sku ?: "",
                            notes = item.notes ?: "",
                            photoPath = item.photoPath,
                            isLoading = false,
                            isLoaded = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load item") }
                }
            }
        }
    }

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
        val original = originalItem ?: return
        val state = _uiState.value
        
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val updatedItem = original.copy(
                    name = state.name.trim(),
                    type = state.type,
                    sku = state.sku.trim().takeIf { it.isNotBlank() },
                    notes = state.notes.trim().takeIf { it.isNotBlank() },
                    photoPath = state.photoPath
                )
                
                val result = updateItemUseCase(updatedItem)
                if (result.isSuccess) {
                    _event.send(EditItemEvent.SaveSuccess)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            nameError = result.exceptionOrNull()?.message ?: "Failed to update item"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Log.e("EditItemViewModel", "Failed to save item", e)
                _uiState.update { it.copy(isLoading = false, nameError = "An unexpected error occurred") }
            }
        }
    }

    fun delete() {
        val item = originalItem ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = deleteItemUseCase(item)
                if (result.isSuccess) {
                    _event.send(EditItemEvent.DeleteSuccess)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = result.exceptionOrNull()?.message ?: "Failed to delete item"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Log.e("EditItemViewModel", "Failed to delete item", e)
                _uiState.update { it.copy(isLoading = false, error = "An unexpected error occurred") }
            }
        }
    }
}

data class EditItemUiState(
    val name: String = "",
    val nameError: String? = null,
    val type: ItemType = ItemType.SINGLE_PRODUCT,
    val sku: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val error: String? = null
)
