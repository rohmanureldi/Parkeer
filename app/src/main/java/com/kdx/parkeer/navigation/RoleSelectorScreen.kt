package com.kdx.parkeer.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
) {
    val activity = LocalContext.current as? Activity
    BackHandler { activity?.finish() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DX.Spacing.L),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(DX.Spacing.XL3))
        Text("KDX Parkeer", style = DX.Font.brandHeadingBold, color = DX.Color.text.primary)
        Text("Membership Benefit Card", style = DX.Font.caption, color = DX.Color.text.secondary)
        Spacer(Modifier.height(DX.Spacing.XL2))

        Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
            Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                RoleCard(Icons.Filled.Store, "Station", "Register & Top-Up", onStationClick, Modifier.weight(1f))
                RoleCard(Icons.Filled.MeetingRoom, "Gate", "Check-In", onGateClick, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                RoleCard(Icons.Filled.LocalParking, "Terminal", "Check-Out", onTerminalClick, Modifier.weight(1f))
                RoleCard(Icons.Filled.Visibility, "Scout", "View Card", onScoutClick, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RoleCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DXCard(
        modifier = modifier,
        style = DXCardStyle.CustomLayout(
            style = CustomCardStyle(CustomCardVariant.Custom({ DX.Color.background.white }, { DX.Color.stroke.border })),
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(DX.Spacing.L),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, contentDescription = title, modifier = Modifier.size(32.dp), tint = DX.Color.text.primary)
                    Spacer(Modifier.height(DX.Spacing.S))
                    Text(title, style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                    Text(subtitle, style = DX.Font.caption, color = DX.Color.text.secondary, textAlign = TextAlign.Center)
                }
            }
        ),
        onClick = onClick,
    )
}
