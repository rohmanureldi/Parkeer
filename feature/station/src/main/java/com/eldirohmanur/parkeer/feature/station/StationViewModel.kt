package com.eldirohmanur.parkeer.feature.station

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldirohmanur.parkeer.core.model.Activity
import com.eldirohmanur.parkeer.core.model.CardData
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
class StationViewModel @Inject constructor(private val cardReader: CardReader, private val nfcTagHolder: NfcTagHolder) : ViewModel() {

    private val _uiState = MutableStateFlow<StationUiState>(StationUiState.Idle)
    val uiState: StateFlow<StationUiState> = _uiState.asStateFlow()

    private var mode: StationMode = StationMode.REGISTER
    private var memberName: String = ""
    private var memberId: String = ""
    private var topUpAmount: Int = 0

    val pendingTopUpAmount: Int get() = topUpAmount

    init {
        viewModelScope.launch {
            nfcTagHolder.tags.collect { tag ->
                if (_uiState.value == StationUiState.WaitingForTap) {
                    onTagDiscovered(tag)
                } else {
                    nfcTagHolder.notifyBusy()
                }
            }
        }
    }

    fun prepareRegister(name: String) {
        mode = StationMode.REGISTER
        memberName = name
        memberId = (System.currentTimeMillis() % 1_000_000).toString().padStart(6, '0')
        _uiState.value = StationUiState.WaitingForTap
    }

    fun retryLastOperation() {
        _uiState.value = StationUiState.WaitingForTap
    }

    fun prepareTopUp(amount: Int) {
        mode = StationMode.TOP_UP
        topUpAmount = amount
        _uiState.value = StationUiState.WaitingForTap
    }

    private fun onTagDiscovered(tag: Tag) {
        viewModelScope.launch {
            _uiState.value = StationUiState.Processing
            when (mode) {
                StationMode.REGISTER -> doRegister(tag)
                StationMode.TOP_UP -> doTopUp(tag)
            }
        }
    }

    private suspend fun doRegister(tag: Tag) {
        // Check if card already has data
        val readResult = cardReader.read(tag)
        if (readResult.isSuccess) {
            _uiState.value = StationUiState.Error(StationError.AlreadyRegistered)
            return
        }

        val id = memberId.toIntOrNull() ?: run {
            _uiState.value = StationUiState.Error(StationError.InvalidMemberId)
            return
        }

        val newCard = CardData(
            memberId = id,
            memberName = memberName,
            balance = 0,
            visitState = VisitState.Idle,
            logs = listOf(TransactionLog(0, System.currentTimeMillis(), Activity.REGISTRATION)),
        )

        cardReader.write(tag, newCard)
            .onSuccess { _uiState.value = StationUiState.RegisterSuccess(memberName, id) }
            .onFailure { _uiState.value = StationUiState.Error(StationError.WriteFailed(it.message)) }
    }

    private suspend fun doTopUp(tag: Tag) {
        val card = cardReader.read(tag).getOrElse {
            _uiState.value = StationUiState.Error(StationError.CardNotRecognized(it.message))
            return
        }

        val oldBalance = card.balance
        val newBalance = oldBalance + topUpAmount
        if (newBalance > 1_000_000) {
            _uiState.value = StationUiState.Error(StationError.MaxBalanceExceeded(oldBalance))
            return
        }

        val log = TransactionLog(topUpAmount, System.currentTimeMillis(), Activity.TOP_UP)
        val updated = card.copy(
            balance = newBalance,
            logs = (listOf(log) + card.logs).take(5),
        )

        cardReader.write(tag, updated)
            .onSuccess { _uiState.value = StationUiState.TopUpSuccess(card.memberName, oldBalance, topUpAmount, newBalance) }
            .onFailure { _uiState.value = StationUiState.Error(StationError.WriteFailed(it.message)) }
    }

    fun reset() {
        _uiState.value = StationUiState.Idle
    }
}
