package com.eldirohmanur.parkeer.core.model

/**
 * Represents the complete data stored on an NFC membership card.
 */
data class CardData(
    val memberId: Int = 0,
    val memberName: String = "",
    val balance: Int = 0,
    val visitState: VisitState = VisitState.Idle,
    val logs: List<TransactionLog> = emptyList(),
)

sealed interface VisitState {
    data object Idle : VisitState
    data class CheckedIn(val timestamp: Long) : VisitState
}

data class TransactionLog(val amount: Int, val timestamp: Long, val activity: Activity)

enum class Activity(val code: Byte) {
    PARKING(0x01),
    TOP_UP(0x02),
    REGISTRATION(0x03),
}
