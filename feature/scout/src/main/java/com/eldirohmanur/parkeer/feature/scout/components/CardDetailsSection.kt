package com.eldirohmanur.parkeer.feature.scout.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.ui.ParkeerCard
import com.eldirohmanur.parkeer.feature.scout.R
import com.telkomsel.dexterity.theme.DX
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun CardDetailsSection(card: CardData, shortFmt: DateTimeFormatter) {
    val zone = remember { ZoneId.systemDefault() }

    ParkeerCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(DX.Spacing.L)) {
            Text(
                stringResource(R.string.scout_recent_transactions),
                style = DX.Font.caption,
                color = DX.Color.text.secondary,
            )
            Spacer(Modifier.height(DX.Spacing.S))
            if (card.logs.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = DX.Spacing.L),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = DX.Color.text.secondary,
                    )
                    Spacer(Modifier.height(DX.Spacing.XS))
                    Text(
                        stringResource(R.string.scout_no_transactions),
                        style = DX.Font.caption,
                        color = DX.Color.text.secondary,
                    )
                }
            } else {
                card.logs.forEachIndexed { index, log ->
                    if (index > 0) HorizontalDivider(color = DX.Color.stroke.divider)
                    TransactionRow(log.activity, log.amount, log.timestamp, shortFmt, zone)
                }
            }
        }
    }
}
