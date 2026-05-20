package com.eldirohmanur.parkeer.feature.gate

sealed interface GateError {
    data class CardNotRecognized(val reason: String?) : GateError
    data object AlreadyCheckedIn : GateError
    data class WriteFailed(val reason: String?) : GateError
}

sealed interface GateUiState {
    data object Ready : GateUiState
    data object Processing : GateUiState
    data class Success(val memberName: String, val checkInTime: Long) : GateUiState
    data class Error(val error: GateError) : GateUiState
}
