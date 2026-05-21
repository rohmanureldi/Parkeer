package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.station.R
import com.eldirohmanur.parkeer.feature.station.StationViewModel
import com.telkomsel.dexterity.theme.DX

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TopUpSheetContent(viewModel: StationViewModel) {
    val amounts = (5_000..100_000 step 5_000).toList()

    Column(
        modifier = Modifier
            .padding(horizontal = DX.Spacing.L)
            .padding(bottom = DX.Spacing.XL),
        verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
    ) {
        Text(
            stringResource(R.string.station_top_up),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.primary,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
            amounts.forEach { amount ->
                FilterChip(
                    selected = false,
                    onClick = { viewModel.prepareTopUp(amount) },
                    label = { Text(amount.toRupiah()) },
                )
            }
        }
    }
}
