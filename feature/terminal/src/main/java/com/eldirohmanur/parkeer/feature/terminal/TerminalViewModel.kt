package com.eldirohmanur.parkeer.feature.terminal

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.PerfTracer
import com.eldirohmanur.parkeer.core.model.Activity
import com.eldirohmanur.parkeer.core.model.AppConfig
import com.eldirohmanur.parkeer.core.model.TransactionLog
import com.eldirohmanur.parkeer.core.model.VisitState
import com.eldirohmanur.parkeer.core.nfc.CardReader
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val cardReader: CardReader,
    private val nfcTagHolder: NfcTagHolder,
    private val appConfig: AppConfig,
    private val perfTracer: PerfTracer,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TerminalUiState>(TerminalUiState.Ready)
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    val ratePerHour: Int get() = appConfig.ratePerHour

    init {
        viewModelScope.launch {
            nfcTagHolder.tags.collect { tag ->
                if (_uiState.value == TerminalUiState.Ready) {
                    onTagDiscovered(tag)
                } else {
                    nfcTagHolder.notifyBusy()
                }
            }
        }
    }

    private fun onTagDiscovered(tag: Tag) {
        viewModelScope.launch {
            _uiState.value = TerminalUiState.Processing
            val trace = perfTracer.startTrace("nfc_checkout")
            try {
                val card = cardReader.read(tag).getOrElse {
                    _uiState.value =
                        TerminalUiState.Error(TerminalError.CardNotRecognized(it.message))
                    return@launch
                }

                // Sequential loop: reject double tap-out
                val checkedIn = card.visitState as? VisitState.CheckedIn
                if (checkedIn == null) {
                    _uiState.value = TerminalUiState.Error(TerminalError.NotCheckedIn)
                    return@launch
                }

                val now = System.currentTimeMillis()
                val durationMs = now - checkedIn.timestamp
                if (durationMs <= 0) {
                    _uiState.value = TerminalUiState.Error(TerminalError.InvalidTime)
                    return@launch
                }

                val durationSeconds = durationMs / 1000
                val hoursCharged = ((durationSeconds + 3599) / 3600).toInt() // ceiling
                val fee = hoursCharged * appConfig.ratePerHour

                if (card.balance < fee) {
                    _uiState.value =
                        TerminalUiState.InsufficientBalance(checkedIn.timestamp, durationMs, hoursCharged, fee, card.balance, fee - card.balance)
                    return@launch
                }

                val newBalance = (card.balance - fee).coerceAtLeast(0)
                val log = TransactionLog(fee, now, Activity.PARKING)
                val updated = card.copy(
                    balance = newBalance,
                    visitState = VisitState.Idle,
                    logs = (listOf(log) + card.logs).take(5),
                )

                cardReader.write(tag, updated)
                    .onSuccess {
                        analyticsHelper.logEvent(
                            "check_out_success",
                            mapOf(
                                "event_category" to "check_out",
                                "screen_name" to "Terminal",
                                "checkin_timestamp" to checkedIn.timestamp.toString(),
                                "checkout_timestamp" to now.toString(),
                                "price" to fee.toString(),
                                "duration" to durationMs.toString(),
                                "billed_hours" to hoursCharged.toString(),
                                "user_id" to card.memberId.toString(),
                                "user_name" to card.memberName,
                            ),
                        )
                        _uiState.value = TerminalUiState.Success(
                            BillingResult(
                                memberName = card.memberName,
                                checkInTime = checkedIn.timestamp,
                                checkOutTime = now,
                                durationMs = durationMs,
                                hoursCharged = hoursCharged,
                                fee = fee,
                                oldBalance = card.balance,
                                newBalance = newBalance,
                            ),
                        )
                    }
                    .onFailure { _uiState.value = TerminalUiState.Error(TerminalError.WriteFailed(it.message)) }
            } finally {
                trace.stop()
            }
        }
    }

    fun reset() {
        _uiState.value = TerminalUiState.Ready
    }
}
