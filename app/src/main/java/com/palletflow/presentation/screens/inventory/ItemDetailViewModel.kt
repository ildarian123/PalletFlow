package com.palletflow.presentation.screens.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palletflow.data.datastore.AuthPreferences
import com.palletflow.data.local.entity.Item
import com.palletflow.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val authPreferences: AuthPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    val uiState: StateFlow<ItemDetailUiState> = flow {
        val userId = authPreferences.getUserId()
        emit(userId)
    }.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(ItemDetailUiState(error = "User not logged in", isLoading = false))
        } else {
            itemRepository.getItem(itemId, userId)
                .map { item ->
                    ItemDetailUiState(item = item, isLoading = false)
                }
                .catch { e ->
                    emit(ItemDetailUiState(error = e.message, isLoading = false))
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ItemDetailUiState(isLoading = true)
    )
}

data class ItemDetailUiState(
    val item: Item? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
