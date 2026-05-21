package com.eldirohmanur.parkeer.feature.scout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.model.Activity
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.scout.R
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun TransactionRow(activity: Activity, amount: Int, timestamp: Long, fmt: DateTimeFormatter, zone: ZoneId) {
    val (icon, label, color) = when (activity) {
        Activity.PARKING -> Triple(
            Icons.Filled.LocalParking,
            stringResource(R.string.scout_activity_parking),
            DX.Color.text.red,
        )

        Activity.TOP_UP -> Triple(
            Icons.Filled.AccountBalanceWallet,
            stringResource(R.string.scout_activity_top_up),
            DX.Color.text.darkGreen,
        )

        Activity.REGISTRATION -> Triple(
            Icons.Filled.PersonAdd,
            stringResource(R.string.scout_activity_registration),
            DX.Color.text.blue,
        )
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = DX.Spacing.S),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = color)
            Column {
                Text(label, style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                val timeText = fmt.format(Instant.ofEpochMilli(timestamp).atZone(zone))
                val subtitle = if (activity == Activity.PARKING) {
                    val hours = amount / 2000
                    "$timeText · $hours jam"
                } else {
                    timeText
                }
                Text(subtitle, style = DX.Font.caption, color = DX.Color.text.secondary)
            }
        }
        val prefix = if (activity == Activity.PARKING) "-" else "+"
        Text("${prefix}${amount.toRupiah()}", style = DX.Font.bodySemiBold, color = color)
    }
}
