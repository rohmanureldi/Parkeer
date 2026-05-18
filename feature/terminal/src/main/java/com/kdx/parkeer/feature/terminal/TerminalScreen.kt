package com.kdx.parkeer.feature.terminal

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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kdx.parkeer.core.ui.NfcPulseAnimation
import com.kdx.parkeer.core.ui.ParkeerCard
import com.kdx.parkeer.core.ui.rememberHapticFeedback
import com.kdx.parkeer.core.ui.toRupiah
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onBack: () -> Unit,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is TerminalUiState.Success -> haptic.success()
            is TerminalUiState.Error -> haptic.error()
            is TerminalUiState.InsufficientBalance -> haptic.error()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal") },
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
            Text("Exit Point – Check-Out", style = DX.Font.caption, color = DX.Color.text.secondary)

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "terminal_state"
            ) { state ->
                when (state) {
                    is TerminalUiState.Ready -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL))
                            NfcPulseAnimation()
                            Spacer(Modifier.height(DX.Spacing.L))
                            Text("Ready", style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
                            Text("Tap member card to check out", style = DX.Font.caption, color = DX.Color.text.secondary)
                            Spacer(Modifier.height(DX.Spacing.M))
                            Text("Rate: ${2000.toRupiah()}/hour (rounded up)", style = DX.Font.caption, color = DX.Color.text.secondary)
                        }
                    }
                    is TerminalUiState.Processing -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL3))
                            CircularProgressIndicator()
                            Spacer(Modifier.height(DX.Spacing.L))
                            Text("Calculating...", style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                        }
                    }
                    is TerminalUiState.Success -> {
                        val b = state.billing
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Success", modifier = Modifier.size(48.dp), tint = DX.Color.text.darkGreen)
                                Spacer(Modifier.height(DX.Spacing.S))
                                Text("Checkout Complete!", style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
                            }
                            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(DX.Spacing.L), verticalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                                    ReceiptRow("Member", b.memberName)
                                    ReceiptRow("Check-in", fmt.format(Date(b.checkInTime)))
                                    ReceiptRow("Check-out", fmt.format(Date(b.checkOutTime)))
                                    ReceiptRow("Duration", formatDuration(b.durationMs))
                                    ReceiptRow("Hours Billed", "${b.hoursCharged} hour${if (b.hoursCharged > 1) "s" else ""} ↑")
                                    HorizontalDivider(color = DX.Color.stroke.divider)
                                    ReceiptRow("Fee", b.fee.toRupiah(), highlight = true)
                                    ReceiptRow("Previous Balance", b.oldBalance.toRupiah())
                                    ReceiptRow("New Balance", b.newBalance.toRupiah())
                                }
                            }
                            DXButton(onClick = { viewModel.reset() }, text = "Done", variant = ButtonVariant.Primary.Large, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    is TerminalUiState.InsufficientBalance -> {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Warning, contentDescription = "Insufficient Balance", modifier = Modifier.size(48.dp), tint = DX.Color.text.darkYellow)
                                Spacer(Modifier.height(DX.Spacing.S))
                                Text("Insufficient Balance", style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
                            }
                            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(DX.Spacing.L), verticalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                                    ReceiptRow("Check-in", fmt.format(Date(state.checkInTime)))
                                    ReceiptRow("Duration", formatDuration(state.durationMs))
                                    ReceiptRow("Hours Billed", "${state.hoursCharged} hour${if (state.hoursCharged > 1) "s" else ""} ↑")
                                    ReceiptRow("Fee Required", state.fee.toRupiah())
                                    ReceiptRow("Your Balance", state.balance.toRupiah())
                                    ReceiptRow("Need More", state.deficit.toRupiah(), highlight = true)
                                }
                            }
                            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(DX.Spacing.L), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                                    Icon(Icons.Filled.Lightbulb, contentDescription = "Tip", modifier = Modifier.size(16.dp), tint = DX.Color.text.secondary)
                                    Text(
                                        "Please top-up at the nearest Station, then return here.",
                                        style = DX.Font.body,
                                        color = DX.Color.text.secondary,
                                    )
                                }
                            }
                            DXButton(onClick = { viewModel.reset() }, text = "Dismiss", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    is TerminalUiState.Error -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL2))
                            Icon(Icons.Filled.Error, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = DX.Color.text.red)
                            Spacer(Modifier.height(DX.Spacing.M))
                            Text("Error", style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
                            Text(state.message, style = DX.Font.body, color = DX.Color.text.secondary)
                            Spacer(Modifier.height(DX.Spacing.XL))
                            DXButton(onClick = { viewModel.reset() }, text = "Dismiss", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = DX.Font.body, color = DX.Color.text.secondary)
        Text(
            value,
            style = if (highlight) DX.Font.bodySemiBold else DX.Font.body,
            color = if (highlight) DX.Color.text.red else DX.Color.text.primary
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "${hours}h ${minutes}m"
}
