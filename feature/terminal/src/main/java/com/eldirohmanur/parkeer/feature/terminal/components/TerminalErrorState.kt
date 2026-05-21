package com.eldirohmanur.parkeer.feature.terminal.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.ui.ParkeerErrorState
import com.eldirohmanur.parkeer.feature.terminal.R
import com.eldirohmanur.parkeer.feature.terminal.TerminalError

@Composable
internal fun TerminalErrorState(error: TerminalError, onDismiss: () -> Unit) {
    val analytics = LocalAnalytics.current
    val message = when (error) {
        is TerminalError.CardNotRecognized -> stringResource(R.string.terminal_error_card_not_recognized)
        is TerminalError.NotCheckedIn -> stringResource(R.string.terminal_error_not_checked_in)
        is TerminalError.InvalidTime -> stringResource(R.string.terminal_error_invalid_time)
        is TerminalError.WriteFailed -> stringResource(R.string.terminal_error_write_failed)
    }
    LaunchedEffect(error) {
        val reason = when (error) {
            is TerminalError.CardNotRecognized -> error.reason
            is TerminalError.WriteFailed -> error.reason
            else -> null
        }
        analytics.logEvent(
            "check_out_failed",
            params = mapOf(
                "message" to message,
                "reason" to reason.orEmpty().ifEmpty { "unknown" },
            ),
        )
    }
    ParkeerErrorState(
        modifier = Modifier.fillMaxSize(),
        title = stringResource(R.string.terminal_error),
        message = message,
        buttonText = stringResource(R.string.terminal_dismiss),
        onAction = onDismiss,
    )
}
