package com.eldirohmanur.parkeer.feature.gate

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldirohmanur.parkeer.core.firebase.PerfTracer
import com.eldirohmanur.parkeer.core.model.VisitState
import com.eldirohmanur.parkeer.core.nfc.CardReader
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GateViewModel @Inject constructor(
    private val cardReader: CardReader,
    private val nfcTagHolder: NfcTagHolder,
    private val perfTracer: PerfTracer,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GateUiState>(GateUiState.Ready)
    val uiState: StateFlow<GateUiState> = _uiState.asStateFlow()
    val busyTaps: SharedFlow<Unit> = nfcTagHolder.busyTaps

    private var simulationEnabled: Boolean = false
    private var simulatedTime: Long = System.currentTimeMillis()

    fun updateSimulation(enabled: Boolean, hoursAgo: Long) {
        simulationEnabled = enabled
        simulatedTime = System.currentTimeMillis() - (hoursAgo * 3_600_000L)
    }

    init {
        viewModelScope.launch {
            nfcTagHolder.tags.collect { tag ->
                if (_uiState.value == GateUiState.Ready) {
                    onTagDiscovered(tag)
                } else {
                    nfcTagHolder.notifyBusy()
                }
            }
        }
    }

    private fun now(): Long = if (simulationEnabled) simulatedTime else System.currentTimeMillis()

    private fun onTagDiscovered(tag: Tag) {
        viewModelScope.launch {
            _uiState.value = GateUiState.Processing
            val trace = perfTracer.startTrace("nfc_checkin")
            try {
                val card = cardReader.read(tag).getOrElse {
                    _uiState.value = GateUiState.Error(GateError.CardNotRecognized(it.message))
                    return@launch
                }

                if (card.visitState is VisitState.CheckedIn) {
                    _uiState.value = GateUiState.Error(GateError.AlreadyCheckedIn)
                    return@launch
                }

                val timestamp = now()
                val updated = card.copy(visitState = VisitState.CheckedIn(timestamp))

                cardReader.write(tag, updated)
                    .onSuccess { _uiState.value = GateUiState.Success(card.memberName, timestamp) }
                    .onFailure {
                        _uiState.value = GateUiState.Error(GateError.WriteFailed(it.message))
                    }
            } finally {
                trace.stop()
            }
        }
    }

    fun reset() {
        _uiState.value = GateUiState.Ready
    }
}
