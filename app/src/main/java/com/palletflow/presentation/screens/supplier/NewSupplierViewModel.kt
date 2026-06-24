package com.palletflow.presentation.screens.supplier

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.CreateSupplierUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewSupplierViewModel @Inject constructor(
    private val createSupplierUseCase: CreateSupplierUseCase
) : ViewModel() {
    fun onSaveSupplier() {}
}