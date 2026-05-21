package com.eldirohmanur.parkeer.feature.scout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.feature.scout.R
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.time.format.DateTimeFormatter

@Composable
internal fun ScoutLoadedState(card: CardData, fullFmt: DateTimeFormatter, shortFmt: DateTimeFormatter, onTapAgain: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = DX.Spacing.L),
        verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
    ) {
        PhysicalCardUi(card)
        CardDetailsSection(card, fullFmt, shortFmt)
        DXButton(
            onClick = onTapAgain,
            text = stringResource(R.string.scout_tap_again),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
