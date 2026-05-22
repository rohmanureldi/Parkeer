package com.eldirohmanur.parkeer.feature.station

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.eldirohmanur.parkeer.core.ui.ParkeerHapticFeedback
import com.eldirohmanur.parkeer.core.ui.ParkeerTopAppBar
import com.eldirohmanur.parkeer.core.ui.rememberHapticFeedback
import com.eldirohmanur.parkeer.feature.station.components.ArcTabPager
import com.eldirohmanur.parkeer.feature.station.components.NfcFlowSheet
import com.eldirohmanur.parkeer.feature.station.components.RegisterSheetContent
import com.eldirohmanur.parkeer.feature.station.components.ResetContent
import com.eldirohmanur.parkeer.feature.station.components.TopUpSheetContent
import com.eldirohmanur.parkeer.feature.station.model.ArcTab
import com.telkomsel.dexterity.components.analyticwrapper.DXScreen
import com.telkomsel.dexterity.components.analyticwrapper.DefaultScreenMetadata
import com.telkomsel.dexterity.theme.DX
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationScreen(modifier: Modifier = Modifier, viewModel: StationViewModel = hiltViewModel(), onBack: () -> Unit) {
    DXScreen(
        DefaultScreenMetadata(
            screenName = "Station",
            eventCategory = "station",
            screenClass = "StationScreen",
        ),
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val haptic = rememberHapticFeedback()
        var showNfcSheet by rememberSaveable { mutableStateOf(false) }

        val tabs = listOf(
            ArcTab.Register,
            ArcTab.TopUp,
            ArcTab.Reset,
        )

        ObserveUiState(uiState, viewModel, haptic) { showNfcSheet = it }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ParkeerTopAppBar(
                    title = stringResource(R.string.station_title),
                    subtitle = stringResource(R.string.station_subtitle),
                    onBack = onBack,
                )
            },
        ) { innerPadding ->
            ArcTabPager(
                tabs = tabs,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = DX.Spacing.L),
                onPageChanged = { page ->
                    viewModel.reset()
                    if (tabs[page] == ArcTab.Reset) {
                        viewModel.prepareResetTab()
                    }
                },
            ) { page ->
                when (tabs[page]) {
                    ArcTab.Register -> RegisterSheetContent(viewModel)
                    ArcTab.Reset -> ResetContent(viewModel, uiState)
                    ArcTab.TopUp -> TopUpSheetContent(viewModel)
                }
            }
        }
        NfcFlowSheet(
            isVisible = showNfcSheet,
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = {
                showNfcSheet = false
                viewModel.reset()
            },
        )
    }
}

@Composable
private fun ObserveUiState(
    uiState: StationUiState,
    viewModel: StationViewModel,
    haptic: ParkeerHapticFeedback,
    onSheetVisibility: (Boolean) -> Unit,
) {
    LaunchedEffect(uiState) {
        when (uiState) {
            is StationUiState.WaitingForTap, is StationUiState.Processing -> {
                if (viewModel.currentMode != StationMode.RESET) onSheetVisibility(true)
            }

            is StationUiState.RegisterSuccess, is StationUiState.TopUpSuccess -> {
                haptic.success()
                delay(3000)
                onSheetVisibility(false)
                viewModel.reset()
            }

            is StationUiState.Error -> {
                if (viewModel.currentMode != StationMode.RESET) haptic.error()
            }

            else -> onSheetVisibility(false)
        }
    }
}
