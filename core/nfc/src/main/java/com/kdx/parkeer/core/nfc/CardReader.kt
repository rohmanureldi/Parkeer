package com.kdx.parkeer.core.nfc

import android.nfc.Tag
import com.kdx.parkeer.core.model.CardData

interface CardReader {
    suspend fun read(tag: Tag): Result<CardData>
    suspend fun write(tag: Tag, data: CardData): Result<Unit>
}
