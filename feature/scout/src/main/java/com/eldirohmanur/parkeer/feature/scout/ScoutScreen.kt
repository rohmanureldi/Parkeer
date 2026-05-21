package com.eldirohmanur.parkeer.feature.scout

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
import com.eldirohmanur.parkeer.feature.scout.components.ScoutErrorState
import com.eldirohmanur.parkeer.feature.scout.components.ScoutLoadedState
import com.eldirohmanur.parkeer.feature.scout.components.ScoutReadingState
import com.eldirohmanur.parkeer.feature.scout.components.ScoutReadyState
import com.telkomsel.dexterity.components.analyticwrapper.DXScreen
import com.telkomsel.dexterity.components.analyticwrapper.DefaultScreenMetadata
import com.telkomsel.dexterity.theme.DX
import java.time.format.DateTimeFormatter

@Suppress("ParamsComparedByRef")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutScreen(modifier: Modifier = Modifier, viewModel: ScoutViewModel = hiltViewModel(), onBack: () -> Unit) {
    DXScreen(DefaultScreenMetadata("Scout", "scout", "ScoutScreen")) {
        val uiState by viewModel.uiState.collectAsState()
        val haptic = rememberHapticFeedback()
        val fullFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss") }
        val shortFmt = remember { DateTimeFormatter.ofPattern("dd MMM, HH:mm") }

        LaunchedEffect(uiState) {
            when (uiState) {
                is ScoutUiState.Loaded -> haptic.success()
                is ScoutUiState.Error -> haptic.error()
                else -> {}
            }
        }

        Scaffold(
            topBar = {
                ParkeerTopAppBar(
                    title = stringResource(R.string.scout_title),
                    subtitle = stringResource(R.string.scout_subtitle),
                    onBack = onBack,
                )
            },
            containerColor = Color.Transparent,
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
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "scout_state",
                    modifier = Modifier.weight(1f),
                ) { state ->
                    when (state) {
                        is ScoutUiState.Ready -> ScoutReadyState()
                        is ScoutUiState.Reading -> ScoutReadingState()
                        is ScoutUiState.Loaded -> ScoutLoadedState(
                            state.card,
                            fullFmt,
                            shortFmt,
                            onTapAgain = { viewModel.reset() },
                        )

                        is ScoutUiState.Error -> ScoutErrorState(
                            state.reason,
                            onRetry = { viewModel.reset() },
                        )
                    }
                }
            }
        }
    }
}
