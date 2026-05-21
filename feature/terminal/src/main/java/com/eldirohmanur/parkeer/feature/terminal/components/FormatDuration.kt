package com.eldirohmanur.parkeer.feature.terminal.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.feature.terminal.R

@Composable
internal fun formatDuration(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return stringResource(R.string.terminal_duration_format, hours, minutes)
}
