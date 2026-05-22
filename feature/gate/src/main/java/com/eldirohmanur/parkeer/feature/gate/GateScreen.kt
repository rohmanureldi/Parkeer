package com.eldirohmanur.parkeer.feature.gate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.eldirohmanur.parkeer.core.ui.ParkeerTopAppBar
import com.eldirohmanur.parkeer.core.ui.rememberHapticFeedback
import com.eldirohmanur.parkeer.feature.gate.components.GateErrorState
import com.eldirohmanur.parkeer.feature.gate.components.GateProcessingState
import com.eldirohmanur.parkeer.feature.gate.components.GateReadyState
import com.eldirohmanur.parkeer.feature.gate.components.GateSuccessState
import com.telkomsel.dexterity.components.analyticwrapper.DXScreen
import com.telkomsel.dexterity.components.analyticwrapper.DefaultScreenMetadata
import com.telkomsel.dexterity.theme.DX

@Suppress("ParamsComparedByRef")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateScreen(modifier: Modifier = Modifier, viewModel: GateViewModel = hiltViewModel(), onBack: () -> Unit) {
    DXScreen(DefaultScreenMetadata("Gate", "gate", "GateScreen")) {
        val uiState by viewModel.uiState.collectAsState()
        val haptic = rememberHapticFeedback()
        var simEnabled by remember { mutableStateOf(false) }
        var simHoursAgo by remember { mutableStateOf("2") }
        val snackbarHostState = remember { SnackbarHostState() }
        val busyMsg = stringResource(R.string.gate_busy)

        LaunchedEffect(Unit) {
            viewModel.busyTaps.collect {
                haptic.error()
                snackbarHostState.showSnackbar(busyMsg, duration = SnackbarDuration.Short)
            }
        }

        LaunchedEffect(uiState) {
            when (uiState) {
                is GateUiState.Success -> haptic.success()
                is GateUiState.Error -> haptic.error()
                else -> {}
            }
        }

        LaunchedEffect(simEnabled, simHoursAgo) {
            viewModel.updateSimulation(simEnabled, simHoursAgo.toLongOrNull() ?: 0L)
        }

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ParkeerTopAppBar(
                    title = stringResource(id = R.string.gate_title),
                    subtitle = stringResource(R.string.gate_subtitle),
                    onBack = onBack,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = DX.Spacing.L)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
            ) {
                AnimatedContent(
                    modifier = Modifier.weight(1f),
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "gate_state",
                ) { state ->
                    when (state) {
                        is GateUiState.Ready -> GateReadyState(
                            simEnabled = simEnabled,
                            simHoursAgo = simHoursAgo,
                            onSimToggle = { simEnabled = it },
                            onSimHoursChange = {
                                simHoursAgo =
                                    it
                            },
                        )

                        is GateUiState.Processing -> GateProcessingState()
                        is GateUiState.Success -> GateSuccessState(
                            memberName = state.memberName,
                            checkInTime = state.checkInTime,
                            simEnabled = simEnabled,
                            onDone = {
                                viewModel.reset()
                            },
                        )

                        is GateUiState.Error -> GateErrorState(state.error, onDismiss = {
                            viewModel.reset()
                        })
                    }
                }
            }
        }
    }
}
