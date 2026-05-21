package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.feature.station.R
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun StationHome(onRegister: () -> Unit, onTopUp: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        ActionCard(
            icon = Icons.Filled.PersonAdd,
            title = stringResource(R.string.station_register_new),
            description = stringResource(R.string.station_register_desc),
            onClick = onRegister,
        )
        ActionCard(
            icon = Icons.Filled.AccountBalanceWallet,
            title = stringResource(R.string.station_top_up),
            description = stringResource(R.string.station_topup_desc),
            onClick = onTopUp,
        )
    }
}
