package com.palletflow.presentation.screens.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.ItemType
import com.palletflow.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val authPreferences: AuthPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["id"]) {
        "itemId is required for EditItemScreen"
    }

    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState = _uiState.asStateFlow()

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
}

data class EditItemUiState(
    val name: String = "",
    val type: ItemType = ItemType.SINGLE_PRODUCT,
    val sku: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val error: String? = null
)
