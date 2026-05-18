package com.kdx.parkeer.feature.gate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kdx.parkeer.core.ui.NfcPulseAnimation
import com.kdx.parkeer.core.ui.ParkeerCard
import com.kdx.parkeer.core.ui.rememberHapticFeedback
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.components.atom.input.DXInput
import com.telkomsel.dexterity.components.atom.input.DXInputConfig
import com.telkomsel.dexterity.components.atom.input.HeaderConfig
import com.telkomsel.dexterity.components.atom.switch.DXSwitch
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateScreen(onBack: () -> Unit, viewModel: GateViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    var simEnabled by remember { mutableStateOf(false) }
    var simHoursAgo by remember { mutableStateOf("2") }
    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val fullFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss") }
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
        viewModel.simulationEnabled = simEnabled
        val hours = simHoursAgo.toLongOrNull() ?: 0L
        viewModel.simulatedTime = System.currentTimeMillis() - (hours * 3_600_000L)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gate_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.gate_cd_back))
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
                    is GateUiState.Ready -> {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
                        ) {
                            Spacer(Modifier.height(DX.Spacing.L))
                            NfcPulseAnimation()
                            Text(stringResource(R.string.gate_ready), style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
                            Text(stringResource(R.string.gate_tap_to_check_in), style = DX.Font.caption, color = DX.Color.text.secondary)

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
                                        DXSwitch(checked = simEnabled, onCheckedChange = { simEnabled = it })
                                    }
                                    if (simEnabled) {
                                        Spacer(Modifier.height(DX.Spacing.M))
                                        DXInput(
                                            config = DXInputConfig.TextField(
                                                value = simHoursAgo,
                                                onValueChange = { simHoursAgo = it.filter { c -> c.isDigit() } },
                                                placeholder = stringResource(R.string.gate_sim_hours_placeholder),
                                                header = HeaderConfig(label = stringResource(R.string.gate_sim_hours_label)),
                                            ),
                                        )
                                        Spacer(Modifier.height(DX.Spacing.S))
                                        val simTime = System.currentTimeMillis() - ((simHoursAgo.toLongOrNull() ?: 0L) * 3_600_000L)
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
                                                    timeFmt.format(Instant.ofEpochMilli(simTime).atZone(ZoneId.systemDefault())),
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
                    is GateUiState.Processing -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL3))
                            CircularProgressIndicator()
                            Spacer(Modifier.height(DX.Spacing.L))
                            Text(stringResource(R.string.gate_processing), style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                        }
                    }
                    is GateUiState.Success -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL2))
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = stringResource(R.string.gate_cd_success),
                                modifier = Modifier.size(48.dp),
                                tint = DX.Color.text.darkGreen,
                            )
                            Spacer(Modifier.height(DX.Spacing.M))
                            Text(
                                stringResource(R.string.gate_welcome, state.memberName),
                                style = DX.Font.subHeadingSemiBold,
                                color = DX.Color.text.primary,
                            )
                            Text(
                                stringResource(
                                    R.string.gate_checked_in_at,
                                    fullFmt.format(Instant.ofEpochMilli(state.checkInTime).atZone(ZoneId.systemDefault())),
                                ),
                                style = DX.Font.body,
                                color = DX.Color.text.secondary,
                            )
                            if (simEnabled) Text(stringResource(R.string.gate_simulated), style = DX.Font.caption, color = DX.Color.text.darkYellow)
                            Spacer(Modifier.height(DX.Spacing.XL))
                            DXButton(onClick = {
                                viewModel.reset()
                            }, text = stringResource(R.string.gate_done), variant = ButtonVariant.Primary.Large, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    is GateUiState.Error -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL2))
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = stringResource(R.string.gate_cd_error),
                                modifier = Modifier.size(48.dp),
                                tint = DX.Color.text.red,
                            )
                            Spacer(Modifier.height(DX.Spacing.M))
                            Text(stringResource(R.string.gate_error), style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
                            Text(state.message, style = DX.Font.body, color = DX.Color.text.secondary)
                            Spacer(Modifier.height(DX.Spacing.XL))
                            DXButton(
                                onClick = {
                                    viewModel.reset()
                                },
                                text = stringResource(
                                    R.string.gate_dismiss,
                                ),
                                variant = ButtonVariant.Secondary.Large,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
