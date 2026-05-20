package com.eldirohmanur.parkeer.core.nfc

import android.nfc.Tag
import com.eldirohmanur.parkeer.core.model.CardData

interface CardReader {
    suspend fun read(tag: Tag): Result<CardData>
    suspend fun write(tag: Tag, data: CardData): Result<Unit>
}
