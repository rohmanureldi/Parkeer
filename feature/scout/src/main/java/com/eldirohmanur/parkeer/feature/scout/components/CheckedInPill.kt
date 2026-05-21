package com.eldirohmanur.parkeer.feature.scout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.feature.scout.R
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun CheckedInPill(checkedInAt: String, hours: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF69F0AE))
            .padding(top = 32.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            modifier = Modifier.padding(bottom = DX.Spacing.S),
            text = "${stringResource(R.string.scout_checked_in)} · $checkedInAt · $hours jam",
            style = DX.Font.caption,
            color = Color(0xFF1B5E20),
        )
    }
}
