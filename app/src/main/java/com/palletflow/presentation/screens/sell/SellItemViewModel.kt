package com.palletflow.presentation.screens.sell

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.SellItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SellItemViewModel @Inject constructor(
    private val sellItemUseCase: SellItemUseCase
) : ViewModel() {
    fun onSellItem(quantity: Int) {}
}