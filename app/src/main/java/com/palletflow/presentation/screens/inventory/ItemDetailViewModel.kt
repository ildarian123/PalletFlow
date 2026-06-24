package com.palletflow.presentation.screens.inventory

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ItemDetailViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState = _uiState.asStateFlow()
}

data class ItemDetailUiState(val itemName: String = "")