package com.eldirohmanur.parkeer.feature.gate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.ui.NfcPulseAnimation
import com.eldirohmanur.parkeer.core.ui.ParkeerCard
import com.eldirohmanur.parkeer.core.ui.rememberHapticFeedback
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.telkomsel.dexterity.components.analyticwrapper.DXScreen
import com.telkomsel.dexterity.components.analyticwrapper.DefaultScreenMetadata
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonMetadata
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.components.atom.input.DXInput
import com.telkomsel.dexterity.components.atom.input.DXInputConfig
import com.telkomsel.dexterity.components.atom.input.HeaderConfig
import com.telkomsel.dexterity.components.atom.switch.DXSwitch
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("ParamsComparedByRef")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateScreen(modifier: Modifier = Modifier, viewModel: GateViewModel = hiltViewModel(), onBack: () -> Unit) {
    DXScreen(DefaultScreenMetadata("Gate", "gate", "GateScreen")) {
        val uiState by viewModel.uiState.collectAsState()
        val haptic = rememberHapticFeedback()
        val analytics = LocalAnalytics.current
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.gate_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.gate_cd_back),
                            )
                        }
                    },
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
                Text(
                    if (simEnabled) stringResource(R.string.gate_subtitle_sim) else stringResource(R.string.gate_subtitle),
                    style = DX.Font.caption,
                    color = if (simEnabled) DX.Color.text.darkYellow else DX.Color.text.secondary,
                )

                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "gate_state",
                ) { state ->
                    when (state) {
                        is GateUiState.Ready -> GateReadyState(
                            simEnabled,
                            simHoursAgo,
                            onSimToggle = { simEnabled = it },
                            onSimHoursChange = {
                                simHoursAgo =
                                    it
                            },
                        )

                        is GateUiState.Processing -> GateProcessingState()
                        is GateUiState.Success -> GateSuccessState(
                            state.memberName,
                            state.checkInTime,
                            simEnabled,
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

@Composable
private fun GateReadyState(simEnabled: Boolean, simHoursAgo: String, onSimToggle: (Boolean) -> Unit, onSimHoursChange: (String) -> Unit) {
    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
    ) {
        Spacer(Modifier.height(DX.Spacing.L))
        NfcPulseAnimation()
        Text(
            stringResource(R.string.gate_ready),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(R.string.gate_tap_to_check_in),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )

        Spacer(Modifier.height(DX.Spacing.XL))

        ParkeerCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(DX.Spacing.L)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S),
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.gate_cd_sim_settings),
                            modifier = Modifier.size(16.dp),
                            tint = DX.Color.text.secondary,
                        )
                        Text(
                            stringResource(R.string.gate_simulation_mode),
                            style = DX.Font.bodySemiBold,
                            color = DX.Color.text.primary,
                        )
                    }
                    DXSwitch(
                        checked = simEnabled,
                        onCheckedChange = onSimToggle,
                        eventSwitchName = "Check-in Simulation",
                    )
                }
                if (simEnabled) {
                    Spacer(Modifier.height(DX.Spacing.M))
                    DXInput(
                        config = DXInputConfig.TextField(
                            value = simHoursAgo,
                            onValueChange = { onSimHoursChange(it.filter { c -> c.isDigit() }) },
                            placeholder = stringResource(R.string.gate_sim_hours_placeholder),
                            header = HeaderConfig(label = stringResource(R.string.gate_sim_hours_label)),
                        ),
                    )
                    Spacer(Modifier.height(DX.Spacing.S))
                    val simTime = System.currentTimeMillis() - (
                        (
                            simHoursAgo.toLongOrNull()
                                ?: 0L
                            ) * 3_600_000L
                        )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DX.Spacing.XS),
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = stringResource(R.string.gate_cd_sim_warning),
                            modifier = Modifier.size(16.dp),
                            tint = DX.Color.text.darkYellow,
                        )
                        Text(
                            stringResource(
                                R.string.gate_sim_will_record,
                                timeFmt.format(
                                    Instant.ofEpochMilli(simTime).atZone(ZoneId.systemDefault()),
                                ),
                            ),
                            style = DX.Font.caption,
                            color = DX.Color.text.darkYellow,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GateProcessingState() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL3))
        CircularProgressIndicator()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(
            stringResource(R.string.gate_processing),
            style = DX.Font.bodySemiBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(R.string.gate_processing_hint),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )
    }
}

@Composable
private fun GateSuccessState(memberName: String, checkInTime: Long, simEnabled: Boolean, onDone: () -> Unit) {
    val fullFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss") }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL2))
        DotLottieAnimation(
            source = DotLottieSource.Asset("success.lottie"),
            autoplay = true,
            loop = false,
            modifier = Modifier.size(120.dp),
        )
        Spacer(Modifier.height(DX.Spacing.M))
        Text(
            stringResource(R.string.gate_welcome, memberName),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(
                R.string.gate_checked_in_at,
                fullFmt.format(Instant.ofEpochMilli(checkInTime).atZone(ZoneId.systemDefault())),
            ),
            style = DX.Font.body,
            color = DX.Color.text.secondary,
        )
        if (simEnabled) {
            Text(
                stringResource(R.string.gate_simulated),
                style = DX.Font.caption,
                color = DX.Color.text.darkYellow,
            )
        }
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(
            onClick = onDone,
            text = stringResource(R.string.gate_done),
            variant = ButtonVariant.Primary.Large,
            modifier = Modifier.fillMaxWidth(),
            metadata = {
                ButtonMetadata.Regular(
                    "Done",
                    "check_in",
                )
            },
        )
    }
}

@Composable
private fun GateErrorState(error: GateError, onDismiss: () -> Unit) {
    val analytics = LocalAnalytics.current
    val message = when (error) {
        is GateError.CardNotRecognized -> stringResource(R.string.gate_error_card_not_recognized)
        is GateError.AlreadyCheckedIn -> stringResource(R.string.gate_error_already_checked_in)
        is GateError.WriteFailed -> stringResource(R.string.gate_error_write_failed)
    }
    LaunchedEffect(error) {
        val reason = when (error) {
            is GateError.CardNotRecognized -> error.reason
            is GateError.WriteFailed -> error.reason
            else -> null
        }
        analytics.logEvent(
            "check_in_failed",
            params = mapOf(
                "message" to message,
                "reason" to reason.orEmpty().ifEmpty { "unknown" },
            ),
        )
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL2))
        Icon(
            Icons.Filled.Error,
            contentDescription = stringResource(R.string.gate_cd_error),
            modifier = Modifier.size(48.dp),
            tint = DX.Color.text.red,
        )
        Spacer(Modifier.height(DX.Spacing.M))
        Text(
            stringResource(R.string.gate_error),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.red,
        )
        Text(message, style = DX.Font.body, color = DX.Color.text.secondary)
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(
            onClick = onDismiss,
            text = stringResource(R.string.gate_dismiss),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
            metadata = {
                ButtonMetadata.Regular(
                    "Dismiss",
                    "Close Check In Error Screen",
                )
            },
        )
    }
}
