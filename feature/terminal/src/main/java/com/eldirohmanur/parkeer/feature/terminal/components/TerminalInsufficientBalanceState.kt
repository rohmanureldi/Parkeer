package com.eldirohmanur.parkeer.feature.terminal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.ui.ParkeerCard
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.terminal.R
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun TerminalInsufficientBalanceState(
    checkInTime: Long,
    durationMs: Long,
    hoursCharged: Int,
    fee: Int,
    balance: Int,
    deficit: Int,
    fmt: DateTimeFormatter,
    zone: ZoneId,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = stringResource(R.string.terminal_cd_insufficient),
                    modifier = Modifier.size(48.dp),
                    tint = DX.Color.text.darkYellow,
                )
                Spacer(Modifier.height(DX.Spacing.S))
                Text(
                    stringResource(R.string.terminal_insufficient_balance),
                    style = DX.Font.subHeadingSemiBold,
                    color = DX.Color.text.red,
                )
            }
            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(DX.Spacing.L),
                    verticalArrangement = Arrangement.spacedBy(DX.Spacing.S),
                ) {
                    ReceiptRow(
                        stringResource(R.string.terminal_receipt_checkin),
                        fmt.format(Instant.ofEpochMilli(checkInTime).atZone(zone)),
                    )
                    ReceiptRow(
                        stringResource(R.string.terminal_receipt_duration),
                        formatDuration(durationMs),
                    )
                    ReceiptRow(
                        stringResource(R.string.terminal_receipt_hours_billed),
                        stringResource(R.string.terminal_receipt_hours_value, hoursCharged),
                    )
                    ReceiptRow(
                        stringResource(R.string.terminal_receipt_fee_required),
                        fee.toRupiah(),
                    )
                    ReceiptRow(
                        stringResource(R.string.terminal_receipt_your_balance),
                        balance.toRupiah(),
                    )
                    ReceiptRow(
                        stringResource(R.string.terminal_receipt_need_more),
                        deficit.toRupiah(),
                        highlight = true,
                    )
                }
            }
            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(DX.Spacing.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S),
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = stringResource(R.string.terminal_cd_tip),
                        modifier = Modifier.size(16.dp),
                        tint = DX.Color.text.secondary,
                    )
                    Text(
                        stringResource(R.string.terminal_topup_hint),
                        style = DX.Font.body,
                        color = DX.Color.text.secondary,
                    )
                }
            }
            DXButton(
                onClick = onDismiss,
                text = stringResource(R.string.terminal_dismiss),
                variant = ButtonVariant.Secondary.Large,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
