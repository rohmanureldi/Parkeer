package com.eldirohmanur.parkeer.feature.scout.components

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
import com.eldirohmanur.parkeer.feature.scout.R
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun ScoutReadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(DX.Spacing.XL3))
        CircularProgressIndicator()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(
            stringResource(R.string.scout_reading),
            style = DX.Font.bodySemiBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(R.string.scout_reading_hint),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )
    }
}
