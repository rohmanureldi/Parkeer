package com.eldirohmanur.parkeer.feature.terminal

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.eldirohmanur.parkeer.core.ui.ParkeerTopAppBar
import com.eldirohmanur.parkeer.core.ui.rememberHapticFeedback
import com.eldirohmanur.parkeer.feature.terminal.components.TerminalErrorState
import com.eldirohmanur.parkeer.feature.terminal.components.TerminalInsufficientBalanceState
import com.eldirohmanur.parkeer.feature.terminal.components.TerminalProcessingState
import com.eldirohmanur.parkeer.feature.terminal.components.TerminalReadyState
import com.eldirohmanur.parkeer.feature.terminal.components.TerminalSuccessState
import com.telkomsel.dexterity.components.analyticwrapper.DXScreen
import com.telkomsel.dexterity.components.analyticwrapper.DefaultScreenMetadata
import com.telkomsel.dexterity.theme.DX
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("ParamsComparedByRef")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(modifier: Modifier = Modifier, viewModel: TerminalViewModel = hiltViewModel(), onBack: () -> Unit) {
    DXScreen(DefaultScreenMetadata("Terminal", "terminal", "TerminalScreen")) {
        val uiState by viewModel.uiState.collectAsState()
        val haptic = rememberHapticFeedback()
        val fmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm") }
        val zone = remember { ZoneId.systemDefault() }

        LaunchedEffect(uiState) {
            when (uiState) {
                is TerminalUiState.Success -> haptic.success()
                is TerminalUiState.Error -> haptic.error()
                is TerminalUiState.InsufficientBalance -> haptic.error()
                else -> {}
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ParkeerTopAppBar(
                    title = stringResource(R.string.terminal_title),
                    subtitle = stringResource(R.string.terminal_subtitle),
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
                    label = "terminal_state",
                ) { state ->
                    when (state) {
                        is TerminalUiState.Ready -> TerminalReadyState(ratePerHour = viewModel.ratePerHour)

                        is TerminalUiState.Processing -> TerminalProcessingState()

                        is TerminalUiState.Success -> TerminalSuccessState(
                            billing = state.billing,
                            fmt = fmt,
                            zone = zone,
                            onDone = viewModel::reset,
                        )

                        is TerminalUiState.InsufficientBalance -> TerminalInsufficientBalanceState(
                            checkInTime = state.checkInTime,
                            durationMs = state.durationMs,
                            hoursCharged = state.hoursCharged,
                            fee = state.fee,
                            balance = state.balance,
                            deficit = state.deficit,
                            fmt = fmt,
                            zone = zone,
                            onDismiss = viewModel::reset,
                        )

                        is TerminalUiState.Error -> TerminalErrorState(
                            error = state.error,
                            onDismiss = viewModel::reset,
                        )
                    }
                }
            }
        }
    }
}
