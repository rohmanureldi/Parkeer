package com.eldirohmanur.parkeer.feature.station

sealed interface StationError {
    data object AlreadyRegistered : StationError
    data object InvalidMemberId : StationError
    data class CardNotRecognized(val reason: String?) : StationError
    data class MaxBalanceExceeded(val currentBalance: Int) : StationError
    data class WriteFailed(val reason: String?) : StationError
}

sealed interface StationUiState {
    data object Idle : StationUiState
    data object WaitingForTap : StationUiState
    data object Processing : StationUiState
    data class RegisterSuccess(val name: String, val id: Int) : StationUiState
    data class TopUpSuccess(val name: String, val oldBalance: Int, val added: Int, val newBalance: Int) : StationUiState
    data class Error(val error: StationError) : StationUiState
}

enum class StationMode { REGISTER, TOP_UP }
