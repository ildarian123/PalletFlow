package com.palletflow.presentation.screens.lot

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.GetLotDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class LotDetailViewModel @Inject constructor(
    private val getLotDetailUseCase: GetLotDetailUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LotDetailUiState())
    val uiState = _uiState.asStateFlow()
}

data class LotDetailUiState(val lotId: Int = 0)