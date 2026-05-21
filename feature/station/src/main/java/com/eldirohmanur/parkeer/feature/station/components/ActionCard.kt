package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.telkomsel.dexterity.components.molecule.card.DXCard
import com.telkomsel.dexterity.components.molecule.card.DXCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardVariant
import com.telkomsel.dexterity.components.molecule.card.model.CardMetadata
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun ActionCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    DXCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        style = DXCardStyle.CustomLayout(
            style = CustomCardStyle(
                CustomCardVariant.Custom(
                    { DX.Color.background.white },
                    { DX.Color.stroke.border },
                ),
            ),
            content = {
                Row(
                    modifier = Modifier.padding(DX.Spacing.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M),
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = DX.Color.text.primary,
                    )
                    Column {
                        Text(title, style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                        Text(description, style = DX.Font.caption, color = DX.Color.text.secondary)
                    }
                }
            },
        ),
        metadata = {
            CardMetadata.Regular(
                cardName = title,
                listName = "Station Menu",
            )
        },
    )
}
