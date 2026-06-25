package com.palletflow.presentation.screens.reports

import androidx.lifecycle.ViewModel
import com.palletflow.domain.usecase.GetSalesReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SalesReportViewModel @Inject constructor(
    private val getSalesReportUseCase: GetSalesReportUseCase
) : ViewModel() {
    fun fetchReport() {}
}