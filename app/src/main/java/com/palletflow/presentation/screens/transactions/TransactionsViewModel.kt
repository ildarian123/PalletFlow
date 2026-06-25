package com.palletflow.presentation.screens.transactions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class TransactionsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState = _uiState.asStateFlow()
}

data class TransactionsUiState(val transactions: List<String> = emptyList())