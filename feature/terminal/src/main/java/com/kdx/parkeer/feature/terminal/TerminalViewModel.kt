package com.kdx.parkeer.feature.terminal

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdx.parkeer.core.model.Activity
import com.kdx.parkeer.core.model.TransactionLog
import com.kdx.parkeer.core.model.VisitState
import com.kdx.parkeer.core.nfc.CardReader
import com.kdx.parkeer.core.nfc.NfcTagHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillingResult(
    val memberName: String,
    val checkInTime: Long,
    val checkOutTime: Long,
    val durationMs: Long,
    val hoursCharged: Int,
    val fee: Int,
    val oldBalance: Int,
    val newBalance: Int,
)

sealed interface TerminalUiState {
    data object Ready : TerminalUiState
    data object Processing : TerminalUiState
    data class Success(val billing: BillingResult) : TerminalUiState
    data class InsufficientBalance(val checkInTime: Long, val durationMs: Long, val hoursCharged: Int, val fee: Int, val balance: Int, val deficit: Int) : TerminalUiState
    data class Error(val message: String) : TerminalUiState
}

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val cardReader: CardReader,
    private val nfcTagHolder: NfcTagHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow<TerminalUiState>(TerminalUiState.Ready)
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    companion object {
        const val RATE_PER_HOUR = 2000
    }

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

            val card = cardReader.read(tag).getOrElse {
                _uiState.value = TerminalUiState.Error("Card not recognized: ${it.message}")
                return@launch
            }

            // Sequential loop: reject double tap-out
            val checkedIn = card.visitState as? VisitState.CheckedIn
            if (checkedIn == null) {
                _uiState.value = TerminalUiState.Error("Not checked in. Please check in at Gate first.")
                return@launch
            }

            val now = System.currentTimeMillis()
            val durationMs = now - checkedIn.timestamp
            if (durationMs <= 0) {
                _uiState.value = TerminalUiState.Error("Invalid time detected. Device clock may be incorrect.")
                return@launch
            }

            val durationSeconds = durationMs / 1000
            val hoursCharged = ((durationSeconds + 3599) / 3600).toInt() // ceiling
            val fee = hoursCharged * RATE_PER_HOUR

            if (card.balance < fee) {
                _uiState.value = TerminalUiState.InsufficientBalance(checkedIn.timestamp, durationMs, hoursCharged, fee, card.balance, fee - card.balance)
                return@launch
            }

            val newBalance = (card.balance - fee).coerceAtLeast(0)
            val log = TransactionLog(fee, now, Activity.PARKING)
            val updated = card.copy(
                balance = newBalance,
                visitState = VisitState.Idle,
                logs = (listOf(log) + card.logs).take(5)
            )

            cardReader.write(tag, updated)
                .onSuccess {
                    _uiState.value = TerminalUiState.Success(
                        BillingResult(
                            memberName = card.memberName,
                            checkInTime = checkedIn.timestamp,
                            checkOutTime = now,
                            durationMs = durationMs,
                            hoursCharged = hoursCharged,
                            fee = fee,
                            oldBalance = card.balance,
                            newBalance = newBalance
                        )
                    )
                }
                .onFailure { _uiState.value = TerminalUiState.Error(it.message ?: "Write failed") }
        }
    }

    fun reset() { _uiState.value = TerminalUiState.Ready }
}
