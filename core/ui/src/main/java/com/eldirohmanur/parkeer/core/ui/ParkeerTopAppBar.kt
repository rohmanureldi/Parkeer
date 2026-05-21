package com.eldirohmanur.parkeer.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.telkomsel.dexterity.theme.DX

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkeerTopAppBar(modifier: Modifier = Modifier, title: String = "", subtitle: String = "", onBack: () -> Unit) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    text = title,
                    style = DX.Font.heading,
                    color = Color.White,
                )
                Text(
                    text = subtitle,
                    style = DX.Font.caption,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(20.dp),
                    tint = Color.White,
                )
            }
        },
    )
}
