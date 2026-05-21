package com.eldirohmanur.parkeer.feature.gate.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eldirohmanur.parkeer.feature.gate.R
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonMetadata
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun GateSuccessState(memberName: String, checkInTime: Long, simEnabled: Boolean, onDone: () -> Unit) {
    val fullFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(DX.Spacing.XL2))
        DotLottieAnimation(
            source = DotLottieSource.Asset("success.lottie"),
            autoplay = true,
            loop = false,
            modifier = Modifier.size(120.dp),
        )
        Spacer(Modifier.height(DX.Spacing.M))
        Text(
            stringResource(R.string.gate_welcome, memberName),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.primary,
        )
        Text(
            stringResource(
                R.string.gate_checked_in_at,
                fullFmt.format(Instant.ofEpochMilli(checkInTime).atZone(ZoneId.systemDefault())),
            ),
            style = DX.Font.body,
            color = DX.Color.text.secondary,
        )
        if (simEnabled) {
            Text(
                stringResource(R.string.gate_simulated),
                style = DX.Font.caption,
                color = DX.Color.text.darkYellow,
            )
        }
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(
            onClick = onDone,
            text = stringResource(R.string.gate_done),
            variant = ButtonVariant.Primary.Large,
            modifier = Modifier.fillMaxWidth(),
            metadata = {
                ButtonMetadata.Regular(
                    "Done",
                    "check_in",
                )
            },
        )
    }
}
