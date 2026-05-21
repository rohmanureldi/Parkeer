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
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.ui.ParkeerTopAppBar
import com.eldirohmanur.parkeer.core.ui.rememberHapticFeedback
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.station.components.RegisterSheetContent
import com.eldirohmanur.parkeer.feature.station.components.SheetError
import com.eldirohmanur.parkeer.feature.station.components.SheetNfcTap
import com.eldirohmanur.parkeer.feature.station.components.SheetProcessing
import com.eldirohmanur.parkeer.feature.station.components.SheetSuccess
import com.eldirohmanur.parkeer.feature.station.components.StationHome
import com.eldirohmanur.parkeer.feature.station.components.TopUpSheetContent
import com.telkomsel.dexterity.components.analyticwrapper.DXScreen
import com.telkomsel.dexterity.components.analyticwrapper.DefaultScreenMetadata
import com.telkomsel.dexterity.theme.DX
import kotlinx.coroutines.delay

private enum class SheetType { REGISTER, TOP_UP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationScreen(modifier: Modifier = Modifier, viewModel: StationViewModel = hiltViewModel(), onBack: () -> Unit) {
    DXScreen(DefaultScreenMetadata("Station", "station", "StationScreen")) {
        val uiState by viewModel.uiState.collectAsState()
        val haptic = rememberHapticFeedback()
        LocalAnalytics.current
        var activeSheet by rememberSaveable { mutableStateOf<SheetType?>(null) }

        LaunchedEffect(uiState) {
            when (uiState) {
                is StationUiState.RegisterSuccess, is StationUiState.TopUpSuccess -> {
                    haptic.success()
                    delay(3000)
                    activeSheet = null
                    viewModel.reset()
                }

                is StationUiState.Error -> haptic.error()
                else -> {}
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = DX.Spacing.L),
                verticalArrangement = Arrangement.Center,
            ) {
                StationHome(
                    onRegister = {
                        activeSheet = SheetType.REGISTER
                    },
                    onTopUp = {
                        activeSheet = SheetType.TOP_UP
                    },
                )
            }
        }

        if (activeSheet != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    activeSheet = null
                    viewModel.reset()
                },
                sheetState = sheetState,
            ) {
                val minSheetHeight = (LocalWindowInfo.current.containerDpSize.height.value / 3).dp
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "sheet_content",
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
                            is StationUiState.Idle -> {
                                when (activeSheet) {
                                    SheetType.REGISTER -> RegisterSheetContent(viewModel)
                                    SheetType.TOP_UP -> TopUpSheetContent(viewModel)
                                    else -> {}
                                }
                            }

                            is StationUiState.WaitingForTap -> SheetNfcTap(
                                subtitle = if (activeSheet == SheetType.TOP_UP) {
                                    stringResource(
                                        R.string.station_topup_amount,
                                        viewModel.pendingTopUpAmount.toRupiah(),
                                    )
                                } else {
                                    null
                                },
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
                        }
                    }
                }
            }
        }
    }
}
