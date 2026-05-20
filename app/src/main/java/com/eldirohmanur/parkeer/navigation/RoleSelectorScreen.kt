package com.eldirohmanur.parkeer.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DX.Spacing.L),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(DX.Spacing.XL3))
        Text(
            stringResource(R.string.app_brand),
            style = DX.Font.brandHeadingBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(R.string.app_tagline),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )
        Spacer(Modifier.height(DX.Spacing.XL2))

        Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
            Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                RoleCard(
                    Icons.Filled.Store,
                    stringResource(R.string.role_station),
                    stringResource(R.string.role_station_desc),
                    onStationClick,
                    Modifier.weight(1f),
                )
                RoleCard(
                    Icons.Filled.MeetingRoom,
                    stringResource(R.string.role_gate),
                    stringResource(R.string.role_gate_desc),
                    onGateClick,
                    Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                RoleCard(
                    Icons.Filled.LocalParking,
                    stringResource(R.string.role_terminal),
                    stringResource(R.string.role_terminal_desc),
                    onTerminalClick,
                    Modifier.weight(1f),
                )
                RoleCard(
                    Icons.Filled.Visibility,
                    stringResource(R.string.role_scout),
                    stringResource(R.string.role_scout_desc),
                    onScoutClick,
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RoleCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DX.Spacing.L),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        modifier = Modifier.size(32.dp),
                        tint = DX.Color.text.primary,
                    )
                    Spacer(Modifier.height(DX.Spacing.S))
                    Text(title, style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                    Text(
                        subtitle,
                        style = DX.Font.caption,
                        color = DX.Color.text.secondary,
                        textAlign = TextAlign.Center,
                    )
                }
            },
        ),
        onClick = onClick,
    )
}
