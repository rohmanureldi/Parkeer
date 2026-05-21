package com.eldirohmanur.parkeer.feature.terminal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.eldirohmanur.parkeer.core.ui.NfcPulseAnimation
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.terminal.R
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun TerminalReadyState(ratePerHour: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
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
                append(stringResource(R.string.terminal_rate_prefix) + " ")
                withStyle(
                    SpanStyle(
                        color = DX.Color.text.red,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(ratePerHour.toRupiah())
                }
                append(stringResource(R.string.terminal_rate_suffix) + " ")
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
