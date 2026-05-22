package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.ui.NfcPulseAnimation
import com.eldirohmanur.parkeer.core.ui.ParkeerErrorState
import com.eldirohmanur.parkeer.feature.station.R
import com.eldirohmanur.parkeer.feature.station.StationUiState
import com.eldirohmanur.parkeer.feature.station.StationViewModel
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun ResetContent(viewModel: StationViewModel, uiState: StationUiState) {
    when (uiState) {
        is StationUiState.Idle, is StationUiState.WaitingForTap -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                NfcPulseAnimation()
                Spacer(Modifier.height(DX.Spacing.L))
                Text(
                    stringResource(R.string.station_reset_tap),
                    style = DX.Font.subHeadingSemiBold,
                    color = DX.Color.text.primary,
                )
                Text(
                    stringResource(R.string.station_reset_desc),
                    style = DX.Font.caption,
                    color = DX.Color.text.secondary,
                )
            }
        }

        is StationUiState.Processing -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(DX.Spacing.L))
                Text(
                    stringResource(R.string.station_processing),
                    style = DX.Font.bodySemiBold,
                    color = DX.Color.text.primary,
                )
                Text(
                    stringResource(R.string.station_processing_hint),
                    style = DX.Font.caption,
                    color = DX.Color.text.secondary,
                )
            }
        }

        is StationUiState.ResetSuccess -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                DotLottieAnimation(
                    source = DotLottieSource.Asset("success.lottie"),
                    autoplay = true,
                    loop = false,
                    modifier = Modifier.size(150.dp),
                )
                Text(
                    stringResource(R.string.station_reset_success),
                    style = DX.Font.subHeadingSemiBold,
                    color = DX.Color.text.primary,
                )
            }
        }

        is StationUiState.Error -> {
            ParkeerErrorState(
                modifier = Modifier.fillMaxSize(),
                message = stringResource(R.string.station_reset_failed),
                buttonText = stringResource(R.string.station_try_again),
                onAction = viewModel::prepareResetTab,
            )
        }

        else -> {}
    }
}
