package com.eldirohmanur.parkeer.feature.station.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector
import com.eldirohmanur.parkeer.feature.station.R

sealed class ArcTab(val icon: ImageVector, val titleRes: Int) {
    data object TopUp : ArcTab(Icons.Filled.AccountBalanceWallet, R.string.station_top_up)
    data object Register : ArcTab(Icons.Filled.PersonAdd, R.string.station_register_new)
    data object Reset : ArcTab(Icons.Filled.DeleteForever, R.string.station_reset)
}
