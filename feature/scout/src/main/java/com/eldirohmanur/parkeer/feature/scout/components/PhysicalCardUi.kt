package com.eldirohmanur.parkeer.feature.scout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.VisitState
import com.eldirohmanur.parkeer.core.ui.toRupiah
import com.eldirohmanur.parkeer.feature.scout.R

@Composable
internal fun PhysicalCardUi(card: CardData) {
    val gradient =
        Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB)))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(24.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.scout_card_brand),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            Icon(
                Icons.Filled.Contactless,
                contentDescription = stringResource(R.string.scout_cd_nfc),
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp),
            )
        }

        Column(Modifier.align(Alignment.CenterStart)) {
            Text(
                stringResource(R.string.scout_card_balance_label),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
            Text(
                card.balance.toRupiah(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    card.memberName.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
                Text(
                    stringResource(R.string.scout_card_id, card.memberId),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                )
            }
            val statusText = when (card.visitState) {
                is VisitState.CheckedIn -> stringResource(R.string.scout_card_status_parked)
                is VisitState.Idle -> stringResource(R.string.scout_card_status_idle)
            }
            val statusColor = when (card.visitState) {
                is VisitState.CheckedIn -> Color(0xFF69F0AE)
                is VisitState.Idle -> Color.White.copy(alpha = 0.5f)
            }
            Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
