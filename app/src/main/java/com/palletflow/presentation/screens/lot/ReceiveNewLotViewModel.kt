package com.palletflow.presentation.screens.lot

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.ReceiveNewLotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReceiveNewLotViewModel @Inject constructor(
    private val receiveNewLotUseCase: ReceiveNewLotUseCase
) : ViewModel() {
    fun onReceiveLot() {}
}