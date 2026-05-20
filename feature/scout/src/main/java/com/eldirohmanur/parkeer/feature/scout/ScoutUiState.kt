package com.eldirohmanur.parkeer.feature.scout

import com.eldirohmanur.parkeer.core.model.CardData

sealed interface ScoutUiState {
    data object Ready : ScoutUiState
    data object Reading : ScoutUiState
    data class Loaded(val card: CardData) : ScoutUiState
    data class Error(val reason: String?) : ScoutUiState
}
