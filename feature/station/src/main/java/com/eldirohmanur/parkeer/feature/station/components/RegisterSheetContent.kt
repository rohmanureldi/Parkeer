package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.feature.station.R
import com.eldirohmanur.parkeer.feature.station.StationViewModel
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonState
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.components.atom.input.DXInput
import com.telkomsel.dexterity.components.atom.input.DXInputConfig
import com.telkomsel.dexterity.components.atom.input.HeaderConfig
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun RegisterSheetContent(viewModel: StationViewModel) {
    var name by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(horizontal = DX.Spacing.L)
            .padding(bottom = DX.Spacing.XL),
        verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
    ) {
        Text(
            stringResource(R.string.station_register_new),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.primary,
        )
        DXInput(
            config = DXInputConfig.TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.station_member_name_placeholder),
                header = HeaderConfig(label = stringResource(R.string.station_member_name_label)),
            ),
        )
        DXButton(
            onClick = { viewModel.prepareRegister(name) },
            text = stringResource(R.string.station_submit),
            variant = ButtonVariant.Primary.Large,
            state = if (name.isNotBlank()) ButtonState.Default else ButtonState.Disabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
