package com.eldirohmanur.parkeer.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.telkomsel.dexterity.components.molecule.card.DXCard
import com.telkomsel.dexterity.components.molecule.card.DXCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardVariant
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun RoleCard(icon: ImageVector, title: String, subtitle: String, iconColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DXCard(
        modifier = modifier,
        style = DXCardStyle.CustomLayout(
            style = CustomCardStyle(
                CustomCardVariant.Custom(
                    { DX.Color.background.white },
                    { DX.Color.stroke.border },
                ),
            ),
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(DX.Spacing.M)),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(DX.Spacing.L),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = title,
                            style = DX.Font.brandHeadingBold,
                            color = DX.Color.text.primary,
                        )
                        Text(
                            text = subtitle,
                            style = DX.Font.label,
                            color = DX.Color.text.secondary,
                        )
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 24.dp)
                            .alpha(0.1f),
                        tint = iconColor,
                    )
                }
            },
        ),
        onClick = onClick,
    )
}
