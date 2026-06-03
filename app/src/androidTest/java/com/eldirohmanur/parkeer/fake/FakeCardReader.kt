package com.eldirohmanur.parkeer.fake

import android.nfc.Tag
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.nfc.CardReader

/**
 * Fake CardReader for instrumentation tests. Pre-program responses
 * to simulate various NFC scenarios without real hardware.
 */
class FakeCardReader : CardReader {

    var readResult: Result<CardData> = Result.failure(Exception("No card data configured"))
    var writeResult: Result<Unit> = Result.success(Unit)
    var wipeResult: Result<Unit> = Result.success(Unit)

    var lastWrittenData: CardData? = null
        private set

    fun reset() {
        readResult = Result.failure(Exception("No card data configured"))
        writeResult = Result.success(Unit)
        wipeResult = Result.success(Unit)
        lastWrittenData = null
    }

    override suspend fun read(tag: Tag): Result<CardData> = readResult

    override suspend fun write(tag: Tag, data: CardData): Result<Unit> {
        lastWrittenData = data
        return writeResult
    }

    override suspend fun wipe(tag: Tag): Result<Unit> = wipeResult
}
