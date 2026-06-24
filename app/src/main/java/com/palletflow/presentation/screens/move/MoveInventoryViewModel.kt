package com.palletflow.presentation.screens.move

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.MoveInventoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoveInventoryViewModel @Inject constructor(
    private val moveInventoryUseCase: MoveInventoryUseCase
) : ViewModel() {
    fun onMoveInventory(fromId: Int, toId: Int) {}
}