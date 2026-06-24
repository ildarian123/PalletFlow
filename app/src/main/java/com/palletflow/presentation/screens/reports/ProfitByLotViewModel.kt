package com.palletflow.presentation.screens.reports

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.GetProfitByLotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfitByLotViewModel @Inject constructor(
    private val getProfitByLotUseCase: GetProfitByLotUseCase
) : ViewModel() {
    fun fetchProfit() {}
}