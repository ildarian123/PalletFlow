package com.palletflow.presentation.screens.lot

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LotListScreen(
    onLotClick: (Int) -> Unit,
    onReceiveNewLotClick: () -> Unit,
    viewModel: LotListViewModel = hiltViewModel()
) {
}