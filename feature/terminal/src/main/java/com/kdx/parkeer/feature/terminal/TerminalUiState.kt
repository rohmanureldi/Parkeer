package com.kdx.parkeer.feature.terminal

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

sealed interface TerminalError {
    data class CardNotRecognized(val reason: String?) : TerminalError
    data object NotCheckedIn : TerminalError
    data object InvalidTime : TerminalError
    data class WriteFailed(val reason: String?) : TerminalError
}

sealed interface TerminalUiState {
    data object Ready : TerminalUiState
    data object Processing : TerminalUiState
    data class Success(val billing: BillingResult) : TerminalUiState
    data class InsufficientBalance(
        val checkInTime: Long,
        val durationMs: Long,
        val hoursCharged: Int,
        val fee: Int,
        val balance: Int,
        val deficit: Int,
    ) : TerminalUiState
    data class Error(val error: TerminalError) : TerminalUiState
}
