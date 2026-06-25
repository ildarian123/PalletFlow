package com.palletflow.presentation.screens.warehouse

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.CreateWarehouseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewWarehouseViewModel @Inject constructor(
    private val createWarehouseUseCase: CreateWarehouseUseCase
) : ViewModel() {
    fun onSaveWarehouse() {}
}