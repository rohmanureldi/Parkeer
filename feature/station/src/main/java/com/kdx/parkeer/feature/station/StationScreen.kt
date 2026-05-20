package com.kdx.parkeer.feature.station

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kdx.parkeer.core.ui.ErrorLogger
import com.kdx.parkeer.core.ui.NfcPulseAnimation
import com.kdx.parkeer.core.ui.rememberHapticFeedback
import com.kdx.parkeer.core.ui.toRupiah
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.telkomsel.dexterity.components.atom.button.DXButton
import com.telkomsel.dexterity.components.atom.button.model.ButtonState
import com.telkomsel.dexterity.components.atom.button.model.ButtonVariant
import com.telkomsel.dexterity.components.atom.input.DXInput
import com.telkomsel.dexterity.components.atom.input.DXInputConfig
import com.telkomsel.dexterity.components.atom.input.HeaderConfig
import com.telkomsel.dexterity.components.molecule.card.DXCard
import com.telkomsel.dexterity.components.molecule.card.DXCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardStyle
import com.telkomsel.dexterity.components.molecule.card.customcard.CustomCardVariant
import com.telkomsel.dexterity.theme.DX
import kotlinx.coroutines.delay

private enum class SheetType { REGISTER, TOP_UP }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StationScreen(modifier: Modifier = Modifier, viewModel: StationViewModel = hiltViewModel(), onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    var activeSheet by rememberSaveable { mutableStateOf<SheetType?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is StationUiState.RegisterSuccess, is StationUiState.TopUpSuccess -> {
                haptic.success()
                delay(3000)
                activeSheet = null
                viewModel.reset()
            }
            is StationUiState.Error -> haptic.error()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.station_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.station_cd_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = DX.Spacing.L),
            verticalArrangement = Arrangement.Center,
        ) {
            StationHome(
                onRegister = { activeSheet = SheetType.REGISTER },
                onTopUp = { activeSheet = SheetType.TOP_UP },
            )
        }
    }

    if (activeSheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                activeSheet = null
                viewModel.reset()
            },
            sheetState = sheetState,
        ) {
            val minSheetHeight = (LocalWindowInfo.current.containerDpSize.height.value / 3).dp
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "sheet_content",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minSheetHeight),
            ) { state ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (state) {
                        is StationUiState.Idle -> {
                            when (activeSheet) {
                                SheetType.REGISTER -> RegisterSheetContent(viewModel)
                                SheetType.TOP_UP -> TopUpSheetContent(viewModel)
                                else -> {}
                            }
                        }

                        is StationUiState.WaitingForTap -> SheetNfcTap()
                        is StationUiState.Processing -> SheetProcessing()
                        is StationUiState.RegisterSuccess -> SheetSuccess(
                            stringResource(R.string.station_register_success),
                        )

                        is StationUiState.TopUpSuccess -> SheetSuccess(
                            stringResource(R.string.station_topup_success),
                            subtitle = stringResource(
                                R.string.station_topup_balance,
                                state.newBalance.toRupiah(),
                            ),
                        )

                        is StationUiState.Error -> SheetError(state.error) { viewModel.retryLastOperation() }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationHome(onRegister: () -> Unit, onTopUp: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DX.Spacing.M)) {
        ActionCard(
            icon = Icons.Filled.PersonAdd,
            title = stringResource(R.string.station_register_new),
            description = stringResource(R.string.station_register_desc),
            onClick = onRegister,
        )
        ActionCard(
            icon = Icons.Filled.AccountBalanceWallet,
            title = stringResource(R.string.station_top_up),
            description = stringResource(R.string.station_topup_desc),
            onClick = onTopUp,
        )
    }
}

@Composable
private fun ActionCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    DXCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        style = DXCardStyle.CustomLayout(
            style = CustomCardStyle(
                CustomCardVariant.Custom(
                    { DX.Color.background.white },
                    { DX.Color.stroke.border },
                ),
            ),
            content = {
                Row(
                    modifier = Modifier.padding(DX.Spacing.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DX.Spacing.M),
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = DX.Color.text.primary,
                    )
                    Column {
                        Text(title, style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
                        Text(description, style = DX.Font.caption, color = DX.Color.text.secondary)
                    }
                }
            },
        ),
    )
}

@Composable
private fun RegisterSheetContent(viewModel: StationViewModel) {
    var name by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(horizontal = DX.Spacing.L)
            .padding(bottom = DX.Spacing.XL),
        verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
    ) {
        Text(
            stringResource(R.string.station_register_new),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.primary,
        )
        DXInput(
            config = DXInputConfig.TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.station_member_name_placeholder),
                header = HeaderConfig(label = stringResource(R.string.station_member_name_label)),
            ),
        )
        DXButton(
            onClick = { viewModel.prepareRegister(name) },
            text = stringResource(R.string.station_submit),
            variant = ButtonVariant.Primary.Large,
            state = if (name.isNotBlank()) ButtonState.Default else ButtonState.Disabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopUpSheetContent(viewModel: StationViewModel) {
    val amounts = (5_000..100_000 step 5_000).toList()

    Column(
        modifier = Modifier
            .padding(horizontal = DX.Spacing.L)
            .padding(bottom = DX.Spacing.XL),
        verticalArrangement = Arrangement.spacedBy(DX.Spacing.M),
    ) {
        Text(
            stringResource(R.string.station_top_up),
            style = DX.Font.subHeadingSemiBold,
            color = DX.Color.text.primary,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(DX.Spacing.S)) {
            amounts.forEach { amount ->
                FilterChip(
                    selected = false,
                    onClick = { viewModel.prepareTopUp(amount) },
                    label = { Text(amount.toRupiah()) },
                )
            }
        }
    }
}

@Composable
private fun SheetNfcTap() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = DX.Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NfcPulseAnimation()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(stringResource(R.string.station_tap_nfc), style = DX.Font.subHeadingSemiBold, color = DX.Color.text.primary)
        Text(stringResource(R.string.station_hold_steady), style = DX.Font.caption, color = DX.Color.text.secondary)
    }
}

@Composable
private fun SheetProcessing() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DX.Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(DX.Spacing.L))
        Text(stringResource(R.string.station_processing), style = DX.Font.bodySemiBold, color = DX.Color.text.primary)
        Text(
            stringResource(R.string.station_processing_hint),
            style = DX.Font.caption,
            color = DX.Color.text.secondary,
        )
    }
}

@Composable
private fun SheetSuccess(title: String, subtitle: String? = null) {
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

@Composable
private fun SheetError(error: StationError, onRetry: () -> Unit) {
    val message = when (error) {
        is StationError.AlreadyRegistered -> stringResource(R.string.station_error_already_registered)
        is StationError.InvalidMemberId -> stringResource(R.string.station_error_invalid_member_id)
        is StationError.CardNotRecognized -> stringResource(R.string.station_error_card_not_recognized)
        is StationError.MaxBalanceExceeded -> stringResource(R.string.station_error_max_balance, error.currentBalance)
        is StationError.WriteFailed -> stringResource(R.string.station_error_write_failed)
    }
    LaunchedEffect(error) {
        val reason = when (error) {
            is StationError.CardNotRecognized -> error.reason
            is StationError.WriteFailed -> error.reason
            else -> null
        }
        ErrorLogger.log("Station", message, reason)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DX.Spacing.L)
            .padding(bottom = DX.Spacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Error,
            contentDescription = stringResource(R.string.station_cd_error),
            modifier = Modifier.size(48.dp),
            tint = DX.Color.text.red,
        )
        Spacer(Modifier.height(DX.Spacing.M))
        Text(stringResource(R.string.station_error), style = DX.Font.subHeadingSemiBold, color = DX.Color.text.red)
        Spacer(Modifier.height(DX.Spacing.S))
        Text(message, style = DX.Font.body, color = DX.Color.text.secondary)
        Spacer(Modifier.height(DX.Spacing.XL))
        DXButton(
            onClick = onRetry,
            text = stringResource(R.string.station_try_again),
            variant = ButtonVariant.Secondary.Large,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
