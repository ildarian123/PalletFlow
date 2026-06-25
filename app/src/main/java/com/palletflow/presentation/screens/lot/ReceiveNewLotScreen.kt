package com.palletflow.presentation.screens.lot

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ReceiveNewLotScreen(
    onLotReceived: () -> Unit,
    viewModel: ReceiveNewLotViewModel = hiltViewModel()
) {
}