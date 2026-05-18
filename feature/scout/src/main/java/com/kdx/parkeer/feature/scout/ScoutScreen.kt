package com.kdx.parkeer.feature.scout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kdx.parkeer.core.model.Activity
import com.kdx.parkeer.core.model.CardData
import com.kdx.parkeer.core.model.VisitState
import com.kdx.parkeer.core.ui.NfcPulseAnimation
import com.kdx.parkeer.core.ui.ParkeerCard
import com.kdx.parkeer.core.ui.rememberHapticFeedback
import com.kdx.parkeer.core.ui.toRupiah
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.theme.DX
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutScreen(
    onBack: () -> Unit,
    viewModel: ScoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    val fullFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss") }
    val shortFmt = remember { DateTimeFormatter.ofPattern("dd MMM, HH:mm") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ScoutUiState.Loaded -> haptic.success()
            is ScoutUiState.Error -> haptic.error()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = DX.Spacing.L)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DX.Spacing.XS)) {
                Icon(Icons.Filled.Lock, contentDescription = "Read only mode", modifier = Modifier.size(16.dp), tint = DX.Color.text.blue)
                Text("Read Only – Will NOT modify your card", style = DX.Font.caption, color = DX.Color.text.blue)
            }

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "scout_state"
            ) { state ->
                when (state) {
                    is ScoutUiState.Ready -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL))
                            NfcPulseAnimation()
                            Spacer(Modifier.height(DX.Spacing.L))
                            Text("Tap your card to view info", style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
                        }
                    }
                    is ScoutUiState.Reading -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL3))
                            CircularProgressIndicator()
                            Spacer(Modifier.height(DX.Spacing.L))
                            Text("Reading...", style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                        }
                    }
                    is ScoutUiState.Loaded -> {
                        Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
                            PhysicalCardUi(state.card)
                            CardDetailsSection(state.card, fullFmt, shortFmt)
                            DXButton(onClick = { viewModel.reset() }, text = "Tap Again to Refresh", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    is ScoutUiState.Error -> {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(DX.Spacing.XL2))
                            Icon(Icons.Filled.Error, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = DX.Color.text.red)
                            Spacer(Modifier.height(DX.Spacing.M))
                            Text(state.message, style = DX.Font.body, color = DX.Color.text.red)
                            Spacer(Modifier.height(DX.Spacing.XL))
                            DXButton(onClick = { viewModel.reset() }, text = "Try Again", variant = ButtonVariant.Secondary.Large, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhysicalCardUi(card: CardData) {
    val gradient = Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB)))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // standard card ratio (85.6mm x 53.98mm)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(24.dp)
    ) {
        // Top row: logo + NFC icon
        Row(
            Modifier.fillMaxWidth().align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PARKEER", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Icon(Icons.Filled.Contactless, contentDescription = "NFC", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
        }

        // Center: balance
        Column(Modifier.align(Alignment.CenterStart)) {
            Text("BALANCE", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.sp)
            Text(card.balance.toRupiah(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // Bottom row: member info + status
        Row(
            Modifier.fillMaxWidth().align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(card.memberName.uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Text("ID ${card.memberId}", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            }
            val statusText = when (card.visitState) {
                is VisitState.CheckedIn -> "PARKED"
                is VisitState.Idle -> "IDLE"
            }
            val statusColor = when (card.visitState) {
                is VisitState.CheckedIn -> Color(0xFF69F0AE)
                is VisitState.Idle -> Color.White.copy(alpha = 0.5f)
            }
            Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CardDetailsSection(card: CardData, fullFmt: DateTimeFormatter, shortFmt: DateTimeFormatter) {
    val zone = remember { ZoneId.systemDefault() }

    // Visit status
    val visitState = card.visitState
    if (visitState is VisitState.CheckedIn) {
        ParkeerCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(DX.Spacing.L)) {
                Text("Checked In", style = DX.Font.bodySemiBold, color = DX.Color.text.darkGreen)
                Text(
                    "Since: ${fullFmt.format(Instant.ofEpochMilli(visitState.timestamp).atZone(zone))}",
                    style = DX.Font.caption, color = DX.Color.text.secondary
                )
            }
        }
    }

    // Transaction logs
    ParkeerCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(DX.Spacing.L)) {
            Text("Recent Transactions", style = DX.Font.caption, color = DX.Color.text.secondary)
            Spacer(Modifier.height(DX.Spacing.S))
            if (card.logs.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(vertical = DX.Spacing.L), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(24.dp), tint = DX.Color.text.secondary)
                    Spacer(Modifier.height(DX.Spacing.XS))
                    Text("No transactions yet", style = DX.Font.caption, color = DX.Color.text.secondary)
                }
            } else {
                card.logs.forEachIndexed { index, log ->
                    if (index > 0) HorizontalDivider(color = DX.Color.stroke.divider)
                    TransactionRow(log.activity, log.amount, log.timestamp, shortFmt, zone)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(activity: Activity, amount: Int, timestamp: Long, fmt: DateTimeFormatter, zone: ZoneId) {
    val (icon, label, color) = when (activity) {
        Activity.PARKING -> Triple(Icons.Filled.LocalParking, "Parking", DX.Color.text.red)
        Activity.TOP_UP -> Triple(Icons.Filled.AccountBalanceWallet, "Top-Up", DX.Color.text.darkGreen)
        Activity.REGISTRATION -> Triple(Icons.Filled.PersonAdd, "Registration", DX.Color.text.blue)
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = DX.Spacing.S),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = color)
            Column {
                Text(label, style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                Text(fmt.format(Instant.ofEpochMilli(timestamp).atZone(zone)), style = DX.Font.caption, color = DX.Color.text.secondary)
            }
        }
        val prefix = if (activity == Activity.PARKING) "-" else "+"
        Text("${prefix}${amount.toRupiah()}", style = DX.Font.bodySemiBold, color = color)
    }
}
