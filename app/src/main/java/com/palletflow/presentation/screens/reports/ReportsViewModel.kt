package com.palletflow.presentation.screens.reports

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ReportsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()
}

data class ReportsUiState(val reports: List<String> = emptyList())