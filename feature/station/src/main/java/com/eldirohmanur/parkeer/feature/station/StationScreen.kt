package com.eldirohmanur.parkeer.feature.station

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eldirohmanur.parkeer.core.ui.ParkeerTopAppBar
import com.eldirohmanur.parkeer.core.ui.rememberHapticFeedback
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.station.components.ArcTab
import com.eldirohmanur.parkeer.feature.station.components.ArcTabPager
import com.eldirohmanur.parkeer.feature.station.components.RegisterSheetContent
import com.eldirohmanur.parkeer.feature.station.components.ResetContent
import com.eldirohmanur.parkeer.feature.station.components.SheetError
import com.eldirohmanur.parkeer.feature.station.components.SheetNfcTap
import com.eldirohmanur.parkeer.feature.station.components.SheetProcessing
import com.eldirohmanur.parkeer.feature.station.components.SheetSuccess
import com.eldirohmanur.parkeer.feature.station.components.TopUpSheetContent
import com.telkomsel.dexterity.components.analyticwrapper.DXScreen
import com.telkomsel.dexterity.components.analyticwrapper.DefaultScreenMetadata
import com.telkomsel.dexterity.theme.DX
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationScreen(modifier: Modifier = Modifier, viewModel: StationViewModel = hiltViewModel(), onBack: () -> Unit) {
    DXScreen(DefaultScreenMetadata("Station", "station", "StationScreen")) {
        val uiState by viewModel.uiState.collectAsState()
        val haptic = rememberHapticFeedback()
        var showNfcSheet by rememberSaveable { mutableStateOf(false) }

        val tabs = listOf(
            ArcTab(Icons.Filled.AccountBalanceWallet, stringResource(R.string.station_top_up)),
            ArcTab(Icons.Filled.PersonAdd, stringResource(R.string.station_register_new)),
            ArcTab(Icons.Filled.DeleteForever, stringResource(R.string.station_reset)),
        )

        LaunchedEffect(uiState) {
            when (uiState) {
                is StationUiState.WaitingForTap, is StationUiState.Processing -> {
                    if (viewModel.currentMode != StationMode.RESET) showNfcSheet = true
                }
                is StationUiState.RegisterSuccess, is StationUiState.TopUpSuccess -> {
                    haptic.success()
                    delay(3000)
                    showNfcSheet = false
                    viewModel.reset()
                }
                is StationUiState.Error -> {
                    if (viewModel.currentMode != StationMode.RESET) haptic.error()
                }

                else -> showNfcSheet = false
            }
        }

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
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                    )
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = DX.Spacing.L),
                onPageChanged = { page ->
                    viewModel.reset()
                    if (page == 2) viewModel.prepareResetTab()
                },
            ) { page ->
                when (page) {
                    0 -> TopUpSheetContent(viewModel)
                    1 -> RegisterSheetContent(viewModel)
                    2 -> ResetContent(viewModel, uiState)
                }
            }
        }

        if (showNfcSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    showNfcSheet = false
                    viewModel.reset()
                },
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
    }
}
