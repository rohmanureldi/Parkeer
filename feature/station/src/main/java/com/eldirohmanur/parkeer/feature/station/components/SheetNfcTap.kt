package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.core.ui.NfcPulseAnimation
import com.eldirohmanur.parkeer.feature.station.R
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun SheetNfcTap(subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = DX.Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NfcPulseAnimation()
        Spacer(Modifier.height(DX.Spacing.L))
        if (subtitle != null) {
            Text(subtitle, style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
            Spacer(Modifier.height(DX.Spacing.S))
        }
        Text(
            stringResource(R.string.station_tap_nfc),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )
    }
}
