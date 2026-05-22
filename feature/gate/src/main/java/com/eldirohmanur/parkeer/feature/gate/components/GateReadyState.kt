package com.eldirohmanur.parkeer.feature.gate.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.ui.NfcPulseAnimation
import com.eldirohmanur.parkeer.core.ui.ParkeerCard
import com.eldirohmanur.parkeer.feature.gate.R
import com.telkomsel.dexterity.components.atom.input.DXInput
import com.telkomsel.dexterity.components.atom.input.DXInputConfig
import com.telkomsel.dexterity.components.atom.input.HeaderConfig
import com.telkomsel.dexterity.components.atom.input.SupportingConfig
import com.telkomsel.dexterity.components.atom.switch.DXSwitch
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun GateReadyState(simEnabled: Boolean, simHoursAgo: String, onSimToggle: (Boolean) -> Unit, onSimHoursChange: (String) -> Unit) {
    val timeFmt = remember { DateTimeFormatter.ofPattern("HH:mm") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
        ) {
            Spacer(Modifier.height(DX.Spacing.L))
            NfcPulseAnimation()
            Text(
                stringResource(R.string.gate_tap_to_check_in),
                style = DX.Font.subHeadingSemiBold,
                color = DX.Color.text.primary,
            )

            Spacer(Modifier.height(DX.Spacing.XL))

            ParkeerCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(DX.Spacing.L)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S),
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.gate_cd_sim_settings),
                                modifier = Modifier.size(16.dp),
                                tint = DX.Color.text.secondary,
                            )
                            Text(
                                stringResource(R.string.gate_simulation_mode),
                                style = DX.Font.bodySemiBold,
                                color = DX.Color.text.primary,
                            )
                        }
                        DXSwitch(
                            checked = simEnabled,
                            onCheckedChange = onSimToggle,
                            eventSwitchName = "Check-in Simulation",
                        )
                    }
                    if (simEnabled) {
                        Spacer(Modifier.height(DX.Spacing.M))
                        DXInput(
                            config = DXInputConfig.TextField(
                                value = simHoursAgo,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                ),
                                onValueChange = { onSimHoursChange(it.filter { c -> c.isDigit() }) },
                                placeholder = stringResource(R.string.gate_sim_hours_placeholder),
                                header = HeaderConfig(label = stringResource(R.string.gate_sim_hours_label)),
                                supporting = SupportingConfig(wordCount = 2),
                            ),
                        )
                        Spacer(Modifier.height(DX.Spacing.S))
                        val simTime = System.currentTimeMillis() - (
                            (
                                simHoursAgo.toLongOrNull()
                                    ?: 0L
                                ) * 3_600_000L
                            )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DX.Spacing.XS),
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = stringResource(R.string.gate_cd_sim_warning),
                                modifier = Modifier.size(16.dp),
                                tint = DX.Color.text.darkYellow,
                            )
                            Text(
                                stringResource(
                                    R.string.gate_sim_will_record,
                                    timeFmt.format(
                                        Instant.ofEpochMilli(simTime)
                                            .atZone(ZoneId.systemDefault()),
                                    ),
                                ),
                                style = DX.Font.caption,
                                color = DX.Color.text.darkYellow,
                            )
                        }
                    }
                }
            }
        }
    }
}
