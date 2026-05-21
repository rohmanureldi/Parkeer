package com.eldirohmanur.parkeer.feature.terminal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.feature.terminal.R
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun TerminalProcessingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(DX.Spacing.XL3))
        CircularProgressIndicator()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(
            stringResource(R.string.terminal_calculating),
            style = DX.Font.bodySemiBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(R.string.terminal_calculating_hint),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )
    }
}
