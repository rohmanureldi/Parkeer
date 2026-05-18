package com.kdx.parkeer.feature.station

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kdx.parkeer.core.ui.ErrorLogger
import com.kdx.parkeer.core.ui.NfcPulseAnimation
import com.kdx.parkeer.core.ui.rememberHapticFeedback
import com.kdx.parkeer.core.ui.toRupiah
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonState
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.components.atom.input.DXInput
import com.telkomsel.dexterity.components.atom.input.DXInputConfig
import com.telkomsel.dexterity.components.atom.input.HeaderConfig
import com.telkomsel.dexterity.components.molecule.card.DXCard
import com.telkomsel.dexterity.components.molecule.card.DXCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardVariant
import com.telkomsel.dexterity.theme.DX

@Suppress("ParamsComparedByRef")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationScreen(
    modifier: Modifier = Modifier,
    viewModel: StationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    var screen by rememberSaveable { mutableStateOf("home") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is StationUiState.RegisterSuccess, is StationUiState.TopUpSuccess -> haptic.success()
            is StationUiState.Error -> haptic.error()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.station_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.station_cd_back))
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
            Text(stringResource(R.string.station_subtitle), style = DX.Font.caption, color = DX.Color.text.secondary)
            Spacer(Modifier.height(DX.Spacing.S))

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "station_state",
            ) { state ->
                when (state) {
                    is StationUiState.Idle -> {
                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "station_sub",
                        ) { sub ->
                            when (sub) {
                                "register" -> RegisterForm(viewModel) { screen = "home" }
                                "topup" -> TopUpForm(viewModel) { screen = "home" }
                                else -> StationHome(
                                    onRegister = { screen = "register" },
                                    onTopUp = { screen = "topup" },
                                )
                            }
                        }
                    }
                    is StationUiState.WaitingForTap -> NfcTapPrompt()
                    is StationUiState.Processing -> ProcessingState()
                    is StationUiState.RegisterSuccess -> {
                        SuccessState(
                            stringResource(R.string.station_register_success),
                            stringResource(R.string.station_register_detail, state.name, state.id),
                        ) {
                            viewModel.reset()
                            screen = "home"
                        }
                    }
                    is StationUiState.TopUpSuccess -> {
                        SuccessState(
                            stringResource(R.string.station_topup_success),
                            stringResource(
                                R.string.station_topup_detail,
                                state.name,
                                state.oldBalance.toRupiah(),
                                state.added.toRupiah(),
                                state.newBalance.toRupiah(),
                            ),
                        ) {
                            viewModel.reset()
                            screen = "home"
                        }
                    }
                    is StationUiState.Error -> {
                        ErrorState(state.error) { viewModel.reset() }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationHome(onRegister: () -> Unit, onTopUp: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        DXCard(
            modifier = Modifier.fillMaxWidth(),
            style = DXCardStyle.CustomLayout(
                style = CustomCardStyle(CustomCardVariant.Custom({ DX.Color.background.white }, { DX.Color.stroke.border })),
                content = {
                    Column(Modifier.padding(DX.Spacing.L), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Store,
                            contentDescription = stringResource(R.string.station_cd_station),
                            modifier = Modifier.size(48.dp),
                            tint = DX.Color.text.primary,
                        )
                        Spacer(Modifier.height(DX.Spacing.S))
                        Text(stringResource(R.string.station_welcome_admin), style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                    }
                },
            ),
        )
        DXButton(
            onClick = onRegister,
            text = stringResource(R.string.station_register_new),
            variant = ButtonVariant.Primary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
        DXButton(
            onClick = onTopUp,
            text = stringResource(R.string.station_top_up),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RegisterForm(viewModel: StationViewModel, onCancel: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var id by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        DXInput(
            config = DXInputConfig.TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.station_member_name_placeholder),
                header = HeaderConfig(label = stringResource(R.string.station_member_name_label)),
            ),
        )
        DXInput(
            config = DXInputConfig.TextField(
                value = id,
                onValueChange = { id = it },
                placeholder = stringResource(R.string.station_member_id_placeholder),
                header = HeaderConfig(label = stringResource(R.string.station_member_id_label)),
            ),
        )
        DXButton(
            onClick = { viewModel.prepareRegister(name, id) },
            text = stringResource(R.string.station_ready_to_write),
            variant = ButtonVariant.Primary.Large,
            state = if (name.isNotBlank() && id.isNotBlank()) ButtonState.Default else ButtonState.Disabled,
            modifier = Modifier.fillMaxWidth(),
        )
        DXButton(
            onClick = onCancel,
            text = stringResource(R.string.station_cancel),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TopUpForm(viewModel: StationViewModel, onCancel: () -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        DXInput(
            config = DXInputConfig.TextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                placeholder = stringResource(R.string.station_topup_placeholder),
                header = HeaderConfig(label = stringResource(R.string.station_topup_label)),
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
            listOf(10000, 20000, 50000, 100000).forEach { v ->
                DXButton(onClick = { amount = v.toString() }, text = "${v / 1000}K", variant = ButtonVariant.Secondary.Small)
            }
        }
        DXButton(
            onClick = { viewModel.prepareTopUp(amount.toIntOrNull() ?: 0) },
            text = stringResource(R.string.station_ready_to_write),
            variant = ButtonVariant.Primary.Large,
            state = if ((amount.toIntOrNull() ?: 0) > 0) ButtonState.Default else ButtonState.Disabled,
            modifier = Modifier.fillMaxWidth(),
        )
        DXButton(
            onClick = onCancel,
            text = stringResource(R.string.station_cancel),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NfcTapPrompt() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL))
        NfcPulseAnimation()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(stringResource(R.string.station_tap_nfc), style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
        Text(stringResource(R.string.station_hold_steady), style = DX.Font.caption, color = DX.Color.text.secondary)
    }
}

@Composable
private fun ProcessingState() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL3))
        CircularProgressIndicator()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(stringResource(R.string.station_processing), style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
        Text(
            stringResource(R.string.station_processing_hint),
            style = DX.Font.caption,
            color = DX.Color.text.secondary
        )
    }
}

@Composable
private fun SuccessState(title: String, detail: String, onDone: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL2))
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.station_cd_success),
            modifier = Modifier.size(48.dp),
            tint = DX.Color.text.darkGreen,
        )
        Spacer(Modifier.height(DX.Spacing.M))
        Text(title, style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
        Spacer(Modifier.height(DX.Spacing.S))
        Text(detail, style = DX.Font.body, color = DX.Color.text.secondary)
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(
            onClick = onDone,
            text = stringResource(R.string.station_done),
            variant = ButtonVariant.Primary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ErrorState(error: StationError, onDismiss: () -> Unit) {
    val message = when (error) {
        is StationError.AlreadyRegistered -> stringResource(R.string.station_error_already_registered)
        is StationError.InvalidMemberId -> stringResource(R.string.station_error_invalid_member_id)
        is StationError.CardNotRecognized -> stringResource(R.string.station_error_card_not_recognized)
        is StationError.MaxBalanceExceeded -> stringResource(R.string.station_error_max_balance, error.currentBalance)
        is StationError.WriteFailed -> stringResource(R.string.station_error_write_failed)
    }
    LaunchedEffect(error) {
        val reason = when (error) {
            is StationError.CardNotRecognized -> error.reason
            is StationError.WriteFailed -> error.reason
            else -> null
        }
        ErrorLogger.log("Station", message, reason)
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL2))
        Icon(
            Icons.Filled.Error,
            contentDescription = stringResource(R.string.station_cd_error),
            modifier = Modifier.size(48.dp),
            tint = DX.Color.text.red,
        )
        Spacer(Modifier.height(DX.Spacing.M))
        Text(stringResource(R.string.station_error), style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
        Spacer(Modifier.height(DX.Spacing.S))
        Text(message, style = DX.Font.body, color = DX.Color.text.secondary)
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(
            onClick = onDismiss,
            text = stringResource(R.string.station_try_again),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
