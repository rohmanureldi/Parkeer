package com.eldirohmanur.parkeer.feature.station.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.telkomsel.dexterity.theme.DX

@Composable
internal fun SheetSuccess(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DX.Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DotLottieAnimation(
            source = DotLottieSource.Asset("success.lottie"),
            autoplay = true,
            loop = false,
            modifier = Modifier.size(150.dp),
        )
        Text(title, style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
        if (subtitle != null) {
            Text(subtitle, style = DX.Font.body, color = DX.Color.text.secondary)
        }
    }
}
