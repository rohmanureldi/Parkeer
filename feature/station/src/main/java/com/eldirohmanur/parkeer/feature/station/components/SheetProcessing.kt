package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.feature.station.R
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun SheetProcessing() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DX.Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(
            stringResource(R.string.station_processing),
            style = DX.Font.bodySemiBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(R.string.station_processing_hint),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )
    }
}
