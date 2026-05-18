package com.kdx.parkeer.feature.station

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
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationScreen(
    onBack: () -> Unit,
    viewModel: StationViewModel = hiltViewModel()
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
                title = { Text("Station") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = DX.Spacing.L)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)
        ) {
            Text("Admin – Register & Top-Up", style = DX.Font.caption, color = DX.Color.text.secondary)
            Spacer(Modifier.height(DX.Spacing.S))

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "station_state"
            ) { state ->
                when (state) {
                    is StationUiState.Idle -> {
                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "station_sub"
                        ) { sub ->
                            when (sub) {
                                "home" -> StationHome(
                                    onRegister = { screen = "register" },
                                    onTopUp = { screen = "topup" }
                                )
                                "register" -> RegisterForm(viewModel) { screen = "home" }
                                "topup" -> TopUpForm(viewModel) { screen = "home" }
                                else -> StationHome(
                                    onRegister = { screen = "register" },
                                    onTopUp = { screen = "topup" }
                                )
                            }
                        }
                    }
                    is StationUiState.WaitingForTap -> NfcTapPrompt()
                    is StationUiState.Processing -> ProcessingState()
                    is StationUiState.RegisterSuccess -> {
                        SuccessState("Registration Successful!", "Member: ${state.name} (#${state.id})") {
                            viewModel.reset(); screen = "home"
                        }
                    }
                    is StationUiState.TopUpSuccess -> {
                        SuccessState(
                            "Top-Up Successful!",
                            "${state.name}\n${state.oldBalance.toRupiah()} + ${state.added.toRupiah()} = ${state.newBalance.toRupiah()}"
                        ) { viewModel.reset(); screen = "home" }
                    }
                    is StationUiState.Error -> {
                        ErrorState(state.message) { viewModel.reset() }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationHome(onRegister: () -> Unit, onTopUp: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        DXCard(modifier = Modifier.fillMaxWidth(), style = DXCardStyle.CustomLayout(
            style = CustomCardStyle(CustomCardVariant.Custom({ DX.Color.background.white }, { DX.Color.stroke.border })),
            content = {
                Column(Modifier.padding(DX.Spacing.L), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Store, contentDescription = "Station", modifier = Modifier.size(48.dp), tint = DX.Color.text.primary)
                    Spacer(Modifier.height(DX.Spacing.S))
                    Text("Welcome, Admin", style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                }
            }
        ))
        DXButton(onClick = onRegister, text = "Register New Member", variant = ButtonVariant.Primary.Large, modifier = Modifier.fillMaxWidth())
        DXButton(onClick = onTopUp, text = "Top-Up Balance", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun RegisterForm(viewModel: StationViewModel, onCancel: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var id by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        DXInput(config = DXInputConfig.TextField(
            value = name, onValueChange = { name = it },
            placeholder = "Enter member name",
            header = HeaderConfig(label = "Member Name")
        ))
        DXInput(config = DXInputConfig.TextField(
            value = id, onValueChange = { id = it },
            placeholder = "Enter member ID",
            header = HeaderConfig(label = "Member ID")
        ))
        DXButton(
            onClick = { viewModel.prepareRegister(name, id) },
            text = "Ready to Write →",
            variant = ButtonVariant.Primary.Large,
            state = if (name.isNotBlank() && id.isNotBlank()) ButtonState.Default else ButtonState.Disabled,
            modifier = Modifier.fillMaxWidth()
        )
        DXButton(onClick = onCancel, text = "Cancel", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun TopUpForm(viewModel: StationViewModel, onCancel: () -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        DXInput(config = DXInputConfig.TextField(
            value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } },
            placeholder = "Enter amount",
            header = HeaderConfig(label = "Top-Up Amount (Rp)")
        ))
        Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
            listOf(10000, 20000, 50000, 100000).forEach { v ->
                DXButton(onClick = { amount = v.toString() }, text = "${v / 1000}K", variant = ButtonVariant.Secondary.Small)
            }
        }
        DXButton(
            onClick = { viewModel.prepareTopUp(amount.toIntOrNull() ?: 0) },
            text = "Ready to Write →",
            variant = ButtonVariant.Primary.Large,
            state = if ((amount.toIntOrNull() ?: 0) > 0) ButtonState.Default else ButtonState.Disabled,
            modifier = Modifier.fillMaxWidth()
        )
        DXButton(onClick = onCancel, text = "Cancel", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NfcTapPrompt() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL))
        NfcPulseAnimation()
        Spacer(Modifier.height(DX.Spacing.L))
        Text("Tap NFC Card", style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
        Text("Hold card steady on the back of the phone", style = DX.Font.caption, color = DX.Color.text.secondary)
    }
}

@Composable
private fun ProcessingState() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL3))
        CircularProgressIndicator()
        Spacer(Modifier.height(DX.Spacing.L))
        Text("Processing...", style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
    }
}

@Composable
private fun SuccessState(title: String, detail: String, onDone: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL2))
        Icon(Icons.Filled.CheckCircle, contentDescription = "Success", modifier = Modifier.size(48.dp), tint = DX.Color.text.darkGreen)
        Spacer(Modifier.height(DX.Spacing.M))
        Text(title, style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
        Spacer(Modifier.height(DX.Spacing.S))
        Text(detail, style = DX.Font.body, color = DX.Color.text.secondary)
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(onClick = onDone, text = "Done", variant = ButtonVariant.Primary.Large, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ErrorState(message: String, onDismiss: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(DX.Spacing.XL2))
        Icon(Icons.Filled.Error, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = DX.Color.text.red)
        Spacer(Modifier.height(DX.Spacing.M))
        Text("Error", style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
        Spacer(Modifier.height(DX.Spacing.S))
        Text(message, style = DX.Font.body, color = DX.Color.text.secondary)
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(onClick = onDismiss, text = "Try Again", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
    }
}
