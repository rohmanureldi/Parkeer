package com.eldirohmanur.parkeer.feature.scout.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.ui.ParkeerErrorState
import com.eldirohmanur.parkeer.feature.scout.R

@Composable
internal fun ScoutErrorState(reason: String?, onRetry: () -> Unit) {
    val analytics = LocalAnalytics.current
    LaunchedEffect(reason) {
        analytics.logEvent(
            name = "scout_failed",
            params = mapOf("reason" to reason.orEmpty()),
        )
    }
    ParkeerErrorState(
        modifier = Modifier.fillMaxSize(),
        message = stringResource(R.string.scout_error_read_failed),
        buttonText = stringResource(R.string.scout_try_again),
        onAction = onRetry,
    )
}
