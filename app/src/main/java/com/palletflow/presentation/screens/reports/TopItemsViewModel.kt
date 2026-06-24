package com.palletflow.presentation.screens.reports

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TopItemsViewModel @Inject constructor() : ViewModel() {
    fun fetchTopItems() {}
}