package com.palletflow.presentation.screens.lot

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class LotListViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(LotListUiState())
    val uiState = _uiState.asStateFlow()
}

data class LotListUiState(val lots: List<String> = emptyList())