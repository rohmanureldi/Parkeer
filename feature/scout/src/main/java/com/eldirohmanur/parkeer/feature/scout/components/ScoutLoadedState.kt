package com.eldirohmanur.parkeer.feature.scout.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.VisitState
import com.eldirohmanur.parkeer.feature.scout.R
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun ScoutLoadedState(card: CardData, fullFmt: DateTimeFormatter, shortFmt: DateTimeFormatter, onTapAgain: () -> Unit) {
    val zone = ZoneId.systemDefault()

    Column(
        modifier = Modifier.padding(top = DX.Spacing.L),
        verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
    ) {
        Box {
            if (card.visitState is VisitState.CheckedIn) {
                val ts = (card.visitState as VisitState.CheckedIn).timestamp
                val checkedInAt = shortFmt.format(Instant.ofEpochMilli(ts).atZone(zone))
                val hours = ((System.currentTimeMillis() - ts) / 3_600_000).coerceAtLeast(1)

                var revealed by remember { mutableStateOf(false) }
                val cardBottomPadding by animateDpAsState(
                    targetValue = if (revealed) 32.dp else 0.dp,
                    animationSpec = tween(durationMillis = 500, delayMillis = 300),
                    label = "pill_reveal",
                )
                LaunchedEffect(Unit) { revealed = true }

                CheckedInPill(
                    checkedInAt = checkedInAt,
                    hours = hours,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )

                PhysicalCardUi(
                    card = card,
                    modifier = Modifier.padding(bottom = cardBottomPadding),
                )
            } else {
                PhysicalCardUi(card = card)
            }
        }
        CardDetailsSection(card, shortFmt)
        DXButton(
            onClick = onTapAgain,
            text = stringResource(R.string.scout_tap_again),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
