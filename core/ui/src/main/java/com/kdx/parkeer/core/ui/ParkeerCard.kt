package com.kdx.parkeer.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.telkomsel.dexterity.components.molecule.card.DXCard
import com.telkomsel.dexterity.components.molecule.card.DXCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardVariant
import com.telkomsel.dexterity.theme.DX

@Composable
fun ParkeerCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    DXCard(
        modifier = modifier,
        style = DXCardStyle.CustomLayout(
            style = CustomCardStyle(
                CustomCardVariant.Custom({ DX.Color.background.white }, { DX.Color.stroke.border }),
            ),
            content = content,
        ),
        onClick = onClick,
    )
}
