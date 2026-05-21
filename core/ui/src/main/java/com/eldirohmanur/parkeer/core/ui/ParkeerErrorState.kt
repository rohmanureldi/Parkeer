package com.eldirohmanur.parkeer.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX

@Composable
fun ParkeerErrorState(message: String, buttonText: String, onAction: () -> Unit, modifier: Modifier = Modifier, title: String? = null) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DX.Spacing.L),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = DX.Color.text.red,
        )
        Spacer(Modifier.height(DX.Spacing.M))
        if (title != null) {
            Text(title, style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
        }
        Text(
            text = message,
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(
            onClick = onAction,
            text = buttonText,
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
