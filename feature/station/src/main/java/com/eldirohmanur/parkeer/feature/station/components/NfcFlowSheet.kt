package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.station.R
import com.eldirohmanur.parkeer.feature.station.StationUiState
import com.eldirohmanur.parkeer.feature.station.StationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NfcFlowSheet(uiState: StationUiState, viewModel: StationViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        val minSheetHeight = (LocalWindowInfo.current.containerDpSize.height.value / 3).dp
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "nfc_sheet",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minSheetHeight),
        ) { state ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (state) {
                    is StationUiState.WaitingForTap -> SheetNfcTap(
                        subtitle = stringResource(
                            R.string.station_topup_amount,
                            viewModel.pendingTopUpAmount.toRupiah(),
                        ).takeIf { viewModel.pendingTopUpAmount > 0 },
                    )

                    is StationUiState.Processing -> SheetProcessing()
                    is StationUiState.RegisterSuccess -> SheetSuccess(
                        stringResource(R.string.station_register_success),
                    )

                    is StationUiState.TopUpSuccess -> SheetSuccess(
                        stringResource(R.string.station_topup_success),
                        subtitle = stringResource(
                            R.string.station_topup_balance,
                            state.newBalance.toRupiah(),
                        ),
                    )

                    is StationUiState.Error -> SheetError(state.error) { viewModel.retryLastOperation() }
                    else -> {}
                }
            }
        }
    }
}
