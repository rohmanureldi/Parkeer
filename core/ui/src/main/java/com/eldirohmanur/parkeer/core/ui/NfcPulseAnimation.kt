package com.eldirohmanur.parkeer.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource

@Composable
fun NfcPulseAnimation(modifier: Modifier = Modifier, size: Dp = 120.dp) {
    DotLottieAnimation(
        source = DotLottieSource.Asset("nfc-tag.lottie"),
        autoplay = true,
        loop = true,
        modifier = modifier.size(size),
    )
}
