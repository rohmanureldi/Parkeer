package com.eldirohmanur.parkeer.feature.gate.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.ui.ParkeerErrorState
import com.eldirohmanur.parkeer.feature.gate.GateError
import com.eldirohmanur.parkeer.feature.gate.R

@Composable
internal fun GateErrorState(error: GateError, onDismiss: () -> Unit) {
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
    ParkeerErrorState(
        modifier = Modifier.fillMaxSize(),
        title = stringResource(R.string.gate_error),
        message = message,
        buttonText = stringResource(R.string.gate_dismiss),
        onAction = onDismiss,
    )
}
