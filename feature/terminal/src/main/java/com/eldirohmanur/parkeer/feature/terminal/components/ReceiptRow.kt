package com.eldirohmanur.parkeer.feature.terminal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun ReceiptRow(label: String, value: String, highlight: Boolean = false, highlightColor: Color = DX.Color.text.red) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = DX.Font.body.copy(fontFamily = FontFamily.Monospace),
            color = DX.Color.text.secondary,
        )
        Text(
            value,
            style = if (highlight) {
                DX.Font.bodySemiBold.copy(fontFamily = FontFamily.Monospace)
            } else {
                DX.Font.body.copy(
                    fontFamily = FontFamily.Monospace,
                )
            },
            color = if (highlight) highlightColor else DX.Color.text.primary,
        )
    }
}
