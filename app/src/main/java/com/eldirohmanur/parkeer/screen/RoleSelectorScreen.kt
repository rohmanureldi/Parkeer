package com.eldirohmanur.parkeer.screen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.R
import com.telkomsel.dexterity.components.molecule.card.DXCard
import com.telkomsel.dexterity.components.molecule.card.DXCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardVariant
import com.telkomsel.dexterity.theme.DX

@Composable
fun RoleSelectorScreen(
    onStationClick: () -> Unit,
    onGateClick: () -> Unit,
    onTerminalClick: () -> Unit,
    onScoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current
    BackHandler { activity?.finish() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(DX.Spacing.L),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = DX.Spacing.XL7),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.app_brand),
                style = DX.Font.brandHeroBold,
                color = Color.White,
            )
            Text(
                stringResource(R.string.app_tagline),
                style = DX.Font.caption,
                color = Color.White.copy(alpha = 0.5f),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                    RoleCard(
                        icon = Icons.Filled.MonetizationOn,
                        title = stringResource(R.string.role_station),
                        subtitle = stringResource(R.string.role_station_desc),
                        iconColor = DX.Color.icon.yellow,
                        onClick = onStationClick,
                        modifier = Modifier.weight(1f),

                    )
                    RoleCard(
                        icon = Icons.Filled.LocalParking,
                        title = stringResource(R.string.role_gate),
                        subtitle = stringResource(R.string.role_gate_desc),
                        iconColor = DX.Color.icon.green,
                        onClick = onGateClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                    RoleCard(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = stringResource(R.string.role_terminal),
                        subtitle = stringResource(R.string.role_terminal_desc),
                        iconColor = DX.Color.icon.red,
                        onClick = onTerminalClick,
                        modifier = Modifier.weight(1f),
                    )
                    RoleCard(
                        icon = Icons.Filled.Visibility,
                        title = stringResource(R.string.role_scout),
                        subtitle = stringResource(R.string.role_scout_desc),
                        iconColor = DX.Color.icon.blue,
                        onClick = onScoutClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleCard(icon: ImageVector, title: String, subtitle: String, iconColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
