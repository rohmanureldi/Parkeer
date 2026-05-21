package com.eldirohmanur.parkeer.feature.terminal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.ui.TornPaperShape
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.terminal.BillingResult
import com.eldirohmanur.parkeer.feature.terminal.R
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonMetadata
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun TerminalSuccessState(billing: BillingResult, fmt: DateTimeFormatter, zone: ZoneId, onDone: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(DX.Spacing.M))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(TornPaperShape()),
                color = Color(0xFFF5F0E8),
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 20.dp,
                        start = DX.Spacing.L,
                        end = DX.Spacing.L,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.terminal_brand_name),
                        style = DX.Font.bodySemiBold.copy(fontFamily = FontFamily.Monospace),
                        color = DX.Color.text.primary,
                    )
                    Spacer(Modifier.height(DX.Spacing.XS))
                    Text(
                        "✓ " + stringResource(R.string.terminal_checkout_complete),
                        style = DX.Font.caption.copy(fontFamily = FontFamily.Monospace),
                        color = DX.Color.text.secondary,
                    )
                    Spacer(Modifier.height(DX.Spacing.M))
                    DashedDivider()
                    Spacer(Modifier.height(DX.Spacing.M))
                    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_member),
                            billing.memberName,
                        )
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_checkin),
                            fmt.format(Instant.ofEpochMilli(billing.checkInTime).atZone(zone)),
                        )
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_checkout),
                            fmt.format(Instant.ofEpochMilli(billing.checkOutTime).atZone(zone)),
                        )
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_duration),
                            formatDuration(billing.durationMs),
                        )
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_hours_billed),
                            stringResource(
                                R.string.terminal_receipt_hours_value,
                                billing.hoursCharged,
                            ),
                        )
                    }
                    Spacer(Modifier.height(DX.Spacing.M))
                    DashedDivider()
                    Spacer(Modifier.height(DX.Spacing.M))
                    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_fee),
                            billing.fee.toRupiah(),
                            highlight = true,
                        )
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_prev_balance),
                            billing.oldBalance.toRupiah(),
                        )
                        ReceiptRow(
                            stringResource(R.string.terminal_receipt_new_balance),
                            billing.newBalance.toRupiah(),
                            highlight = true,
                            highlightColor = DX.Color.text.darkGreen,
                        )
                    }
                    Spacer(Modifier.height(DX.Spacing.M))
                    DashedDivider()
                }
            }
            DXButton(
                onClick = onDone,
                text = stringResource(R.string.terminal_done),
                variant = ButtonVariant.Primary.Large,
                modifier = Modifier.fillMaxWidth(),
                metadata = {
                    ButtonMetadata.Regular(
                        buttonName = "Done",
                        buttonPurpose = "Dismiss Terminal Success Checkout",
                    )
                },
            )
        }
    }
}
