package com.eldirohmanur.parkeer.feature.terminal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.ui.ErrorLogger
import com.eldirohmanur.parkeer.core.ui.NfcPulseAnimation
import com.eldirohmanur.parkeer.core.ui.ParkeerCard
import com.eldirohmanur.parkeer.core.ui.TornPaperShape
import com.eldirohmanur.parkeer.core.ui.rememberHapticFeedback
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("ParamsComparedByRef")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(modifier: Modifier = Modifier, viewModel: TerminalViewModel = hiltViewModel(), onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    val analytics = LocalAnalytics.current
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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.terminal_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.terminal_cd_back))
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
            Text(stringResource(R.string.terminal_subtitle), style = DX.Font.caption, color = DX.Color.text.secondary)

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "terminal_state",
            ) { state ->
                when (state) {
                    is TerminalUiState.Ready -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL))
                            NfcPulseAnimation()
                            Spacer(Modifier.height(DX.Spacing.L))
                            Text(
                                stringResource(R.string.terminal_tap_to_check_out),
                                style = DX.Font.subHeadingSemiBold,
                                color = DX.Color.text.primary,
                            )
                            Spacer(Modifier.height(DX.Spacing.M))
                            Text(
                                buildAnnotatedString {
                                    append(stringResource(R.string.terminal_rate_prefix))
                                    withStyle(
                                        SpanStyle(
                                            color = DX.Color.text.red,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    ) {
                                        append(viewModel.ratePerHour.toRupiah())
                                    }
                                    append(stringResource(R.string.terminal_rate_suffix))
                                    withStyle(
                                        SpanStyle(
                                            color = DX.Color.text.primary,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    ) {
                                        append(stringResource(R.string.terminal_rate_rounded))
                                    }
                                },
                                style = DX.Font.caption,
                                color = DX.Color.text.secondary,
                            )
                        }
                    }
                    is TerminalUiState.Processing -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL3))
                            CircularProgressIndicator()
                            Spacer(Modifier.height(DX.Spacing.L))
                            Text(stringResource(R.string.terminal_calculating), style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                            Text(
                                stringResource(R.string.terminal_calculating_hint),
                                style = DX.Font.caption,
                                color = DX.Color.text.secondary,
                            )
                        }
                    }
                    is TerminalUiState.Success -> {
                        val b = state.billing
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(Modifier.height(DX.Spacing.M))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(TornPaperShape()),
                                color = Color(0xFFF5F0E8),
                                shadowElevation = 2.dp,
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        top = 20.dp,
                                        bottom = 20.dp,
                                        start = DX.Spacing.L,
                                        end = DX.Spacing.L,
                                    ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        "PARKEER",
                                        style = DX.Font.bodySemiBold.copy(fontFamily = FontFamily.Monospace),
                                        color = DX.Color.text.primary,
                                    )
                                    Spacer(Modifier.height(DX.Spacing.XS))
                                    Text(
                                        "✓ " + stringResource(R.string.terminal_checkout_complete),
                                        style = DX.Font.caption.copy(fontFamily = FontFamily.Monospace),
                                        color = DX.Color.text.secondary,
                                    )
                                    Spacer(Modifier.height(DX.Spacing.M))
                                    DashedDivider()
                                    Spacer(Modifier.height(DX.Spacing.M))
                                    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_member),
                                            b.memberName,
                                        )
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_checkin),
                                            fmt.format(
                                                Instant.ofEpochMilli(b.checkInTime).atZone(zone),
                                            ),
                                        )
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_checkout),
                                            fmt.format(
                                                Instant.ofEpochMilli(b.checkOutTime).atZone(zone),
                                            ),
                                        )
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_duration),
                                            formatDuration(b.durationMs),
                                        )
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_hours_billed),
                                            stringResource(
                                                R.string.terminal_receipt_hours_value,
                                                b.hoursCharged,
                                            ),
                                        )
                                    }
                                    Spacer(Modifier.height(DX.Spacing.M))
                                    DashedDivider()
                                    Spacer(Modifier.height(DX.Spacing.M))
                                    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_fee),
                                            b.fee.toRupiah(),
                                            highlight = true,
                                        )
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_prev_balance),
                                            b.oldBalance.toRupiah(),
                                        )
                                        ReceiptRow(
                                            stringResource(R.string.terminal_receipt_new_balance),
                                            b.newBalance.toRupiah(),
                                            highlight = true,
                                            highlightColor = DX.Color.text.darkGreen,
                                        )
                                    }
                                    Spacer(Modifier.height(DX.Spacing.M))
                                    DashedDivider()
                                }
                            }
                            DXButton(
                                onClick = {
                                    analytics.logButtonClick("Done", "Terminal")
                                    viewModel.reset()
                                },
                                text = stringResource(R.string.terminal_done),
                                variant = ButtonVariant.Primary.Large,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    is TerminalUiState.InsufficientBalance -> {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = stringResource(R.string.terminal_cd_insufficient),
                                    modifier = Modifier.size(48.dp),
                                    tint = DX.Color.text.darkYellow,
                                )
                                Spacer(Modifier.height(DX.Spacing.S))
                                Text(
                                    stringResource(R.string.terminal_insufficient_balance),
                                    style = DX.Font.subHeadingSemiBold,
                                    color = DX.Color.text.red,
                                )
                            }
                            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(DX.Spacing.L), verticalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                                    ReceiptRow(
                                        stringResource(R.string.terminal_receipt_checkin),
                                        fmt.format(Instant.ofEpochMilli(state.checkInTime).atZone(zone)),
                                    )
                                    ReceiptRow(stringResource(R.string.terminal_receipt_duration), formatDuration(state.durationMs))
                                    ReceiptRow(
                                        stringResource(R.string.terminal_receipt_hours_billed),
                                        stringResource(R.string.terminal_receipt_hours_value, state.hoursCharged),
                                    )
                                    ReceiptRow(stringResource(R.string.terminal_receipt_fee_required), state.fee.toRupiah())
                                    ReceiptRow(stringResource(R.string.terminal_receipt_your_balance), state.balance.toRupiah())
                                    ReceiptRow(stringResource(R.string.terminal_receipt_need_more), state.deficit.toRupiah(), highlight = true)
                                }
                            }
                            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(DX.Spacing.L),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S),
                                ) {
                                    Icon(
                                        Icons.Filled.Lightbulb,
                                        contentDescription = stringResource(R.string.terminal_cd_tip),
                                        modifier = Modifier.size(16.dp),
                                        tint = DX.Color.text.secondary,
                                    )
                                    Text(stringResource(R.string.terminal_topup_hint), style = DX.Font.body, color = DX.Color.text.secondary)
                                }
                            }
                            DXButton(
                                onClick = {
                                    viewModel.reset()
                                },
                                text = stringResource(
                                    R.string.terminal_dismiss,
                                ),
                                variant = ButtonVariant.Secondary.Large,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    is TerminalUiState.Error -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL2))
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = stringResource(R.string.terminal_cd_error),
                                modifier = Modifier.size(48.dp),
                                tint = DX.Color.text.red,
                            )
                            Spacer(Modifier.height(DX.Spacing.M))
                            Text(stringResource(R.string.terminal_error), style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
                            val message = when (state.error) {
                                is TerminalError.CardNotRecognized -> stringResource(R.string.terminal_error_card_not_recognized)
                                is TerminalError.NotCheckedIn -> stringResource(R.string.terminal_error_not_checked_in)
                                is TerminalError.InvalidTime -> stringResource(R.string.terminal_error_invalid_time)
                                is TerminalError.WriteFailed -> stringResource(R.string.terminal_error_write_failed)
                            }
                            LaunchedEffect(state.error) {
                                val reason = when (state.error) {
                                    is TerminalError.CardNotRecognized -> state.error.reason
                                    is TerminalError.WriteFailed -> state.error.reason
                                    else -> null
                                }
                                ErrorLogger.log("Terminal", message, reason)
                            }
                            Text(message, style = DX.Font.body, color = DX.Color.text.secondary)
                            Spacer(Modifier.height(DX.Spacing.XL))
                            DXButton(
                                onClick = {
                                    viewModel.reset()
                                },
                                text = stringResource(
                                    R.string.terminal_dismiss,
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

@Composable
private fun ReceiptRow(label: String, value: String, highlight: Boolean = false, highlightColor: Color = DX.Color.text.red) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = DX.Font.body.copy(fontFamily = FontFamily.Monospace),
            color = DX.Color.text.secondary,
        )
        Text(
            value,
            style = if (highlight) {
                DX.Font.bodySemiBold.copy(fontFamily = FontFamily.Monospace)
            } else {
                DX.Font.body.copy(
                    fontFamily = FontFamily.Monospace,
                )
            },
            color = if (highlight) highlightColor else DX.Color.text.primary,
        )
    }
}

@Composable
private fun formatDuration(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return stringResource(R.string.terminal_duration_format, hours, minutes)
}

@Composable
private fun DashedDivider() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = Color.LightGray,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
        )
    }
}
