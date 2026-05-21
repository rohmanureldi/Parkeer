package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.ui.ParkeerErrorState
import com.eldirohmanur.parkeer.feature.station.R
import com.eldirohmanur.parkeer.feature.station.StationError
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun SheetError(error: StationError, onRetry: () -> Unit) {
    val analytics = LocalAnalytics.current
    val message = when (error) {
        is StationError.AlreadyRegistered -> stringResource(R.string.station_error_already_registered)
        is StationError.InvalidMemberId -> stringResource(R.string.station_error_invalid_member_id)
        is StationError.CardNotRecognized -> stringResource(R.string.station_error_card_not_recognized)
        is StationError.MaxBalanceExceeded -> stringResource(
            R.string.station_error_max_balance,
            error.currentBalance,
        )

        is StationError.WriteFailed -> stringResource(R.string.station_error_write_failed)
    }
    LaunchedEffect(error) {
        val reason = when (error) {
            is StationError.CardNotRecognized -> error.reason
            is StationError.WriteFailed -> error.reason
            else -> null
        }
        analytics.logEvent(
            name = "station_error",
            params = mapOf(
                "message" to message,
                "reason" to reason.orEmpty(),
            ),
        )
    }
    ParkeerErrorState(
        title = stringResource(R.string.station_error),
        message = message,
        buttonText = stringResource(R.string.station_try_again),
        onAction = onRetry,
        modifier = Modifier
            .padding(horizontal = DX.Spacing.L)
            .padding(bottom = DX.Spacing.XL),
    )
}
