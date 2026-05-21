package com.eldirohmanur.parkeer.feature.scout

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.nfc.CardReader
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoutViewModel @Inject constructor(
    private val cardReader: CardReader,
    private val nfcTagHolder: NfcTagHolder,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScoutUiState>(ScoutUiState.Ready)
    val uiState: StateFlow<ScoutUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            nfcTagHolder.tags.collect { tag ->
                if (_uiState.value is ScoutUiState.Ready || _uiState.value is ScoutUiState.Loaded) {
                    onTagDiscovered(tag)
                }
            }
        }
    }

    private fun onTagDiscovered(tag: Tag) {
        viewModelScope.launch {
            _uiState.value = ScoutUiState.Reading
            cardReader.read(tag)
                .onSuccess {
                    analyticsHelper.logEvent(
                        "nfc_read_success",
                        mapOf(
                            "event_category" to "scout",
                            "screen_name" to "Scout",
                            "user_id" to it.memberId.toString(),
                            "user_name" to it.memberName,
                        ),
                    )
                    _uiState.value = ScoutUiState.Loaded(it)
                }
                .onFailure { _uiState.value = ScoutUiState.Error(it.message) }
        }
    }

    fun reset() {
        _uiState.value = ScoutUiState.Ready
    }
}
