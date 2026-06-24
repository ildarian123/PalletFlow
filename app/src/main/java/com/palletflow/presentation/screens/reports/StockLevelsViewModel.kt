package com.palletflow.presentation.screens.reports

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.GetStockLevelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StockLevelsViewModel @Inject constructor(
    private val getStockLevelsUseCase: GetStockLevelsUseCase
) : ViewModel() {
    fun fetchStock() {}
}