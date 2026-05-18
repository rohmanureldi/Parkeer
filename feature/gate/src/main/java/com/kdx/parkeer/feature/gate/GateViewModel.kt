package com.kdx.parkeer.feature.gate

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdx.parkeer.core.model.VisitState
import com.kdx.parkeer.core.nfc.CardReader
import com.kdx.parkeer.core.nfc.NfcTagHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GateUiState {
    data object Ready : GateUiState
    data object Processing : GateUiState
    data class Success(val memberName: String, val checkInTime: Long) : GateUiState
    data class Error(val message: String) : GateUiState
}

@HiltViewModel
class GateViewModel @Inject constructor(private val cardReader: CardReader, private val nfcTagHolder: NfcTagHolder) : ViewModel() {

    private val _uiState = MutableStateFlow<GateUiState>(GateUiState.Ready)
    val uiState: StateFlow<GateUiState> = _uiState.asStateFlow()
    val busyTaps: SharedFlow<Unit> = nfcTagHolder.busyTaps

    var simulationEnabled: Boolean = false
    var simulatedTime: Long = System.currentTimeMillis()

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

            val card = cardReader.read(tag).getOrElse {
                _uiState.value = GateUiState.Error("Card not recognized: ${it.message}")
                return@launch
            }

            // Sequential loop: reject double tap-in
            if (card.visitState is VisitState.CheckedIn) {
                _uiState.value = GateUiState.Error("Already checked in. Please proceed to Terminal.")
                return@launch
            }

            val timestamp = now()
            val updated = card.copy(visitState = VisitState.CheckedIn(timestamp))

            cardReader.write(tag, updated)
                .onSuccess { _uiState.value = GateUiState.Success(card.memberName, timestamp) }
                .onFailure { _uiState.value = GateUiState.Error(it.message ?: "Write failed") }
        }
    }

    fun reset() {
        _uiState.value = GateUiState.Ready
    }
}
