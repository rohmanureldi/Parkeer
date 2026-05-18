package com.kdx.parkeer.core.nfc

import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import com.kdx.parkeer.core.cardprotocol.CardProtocol
import com.kdx.parkeer.core.crypto.CardCipher
import com.kdx.parkeer.core.model.CardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class NtagCardReader @Inject constructor(private val cipher: CardCipher) : CardReader {

    override suspend fun read(tag: Tag): Result<CardData> = withContext(Dispatchers.IO) {
        runCatching {
            val ultralight = MifareUltralight.get(tag)
                ?: error("Not a MifareUltralight tag")
            ultralight.connect()
            try {
                val raw = readAllPages(ultralight)
                // Verify commit byte before trusting data
                require(raw[CardProtocol.TOTAL_SIZE - 1] == 0x01.toByte()) {
                    "Card has uncommitted write — data may be corrupt"
                }
                CardProtocol.deserialize(raw, cipher, tag.id)
            } finally {
                ultralight.close()
            }
        }
    }

    override suspend fun write(tag: Tag, data: CardData): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val ultralight = MifareUltralight.get(tag)
                ?: error("Not a MifareUltralight tag")
            ultralight.connect()
            try {
                // Read current write counter from card (bytes 4..7)
                val header = ultralight.readPages(4) // pages 4-7 = bytes 0-15 of user memory
                val previousCounter = ByteBuffer.wrap(header, 4, 4).order(ByteOrder.BIG_ENDIAN).int

                val raw = CardProtocol.serialize(data, cipher, tag.id, previousCounter)

                // Step 1: Write data with commit byte = 0x00 (already 0x00 from serialize)
                writeAllPages(ultralight, raw)

                // Step 2: Read back and verify integrity (excluding commit byte)
                val readBack = readAllPages(ultralight)
                val dataMatch = raw.copyOfRange(0, CardProtocol.TOTAL_SIZE - 1)
                    .contentEquals(readBack.copyOfRange(0, CardProtocol.TOTAL_SIZE - 1))
                require(dataMatch) { "Read-back verification failed — card data mismatch" }

                // Step 3: Write commit byte = 0x01 to finalize
                val commitPage = (CardProtocol.TOTAL_SIZE - 1) / 4 + 4 // page offset in user memory
                val commitPageOffset = (CardProtocol.TOTAL_SIZE - 1) % 4
                val commitPageData = readBack.copyOfRange(
                    (commitPage - 4) * 4,
                    (commitPage - 4) * 4 + 4,
                ).also { it[commitPageOffset] = 0x01 }
                ultralight.writePage(commitPage, commitPageData)
            } finally {
                ultralight.close()
            }
        }
    }

    private fun readAllPages(ultralight: MifareUltralight): ByteArray {
        val result = ByteArray(CardProtocol.TOTAL_SIZE)
        var offset = 0
        var page = 4
        while (offset < CardProtocol.TOTAL_SIZE) {
            val data = ultralight.readPages(page)
            val toCopy = minOf(16, CardProtocol.TOTAL_SIZE - offset)
            System.arraycopy(data, 0, result, offset, toCopy)
            offset += 16
            page += 4
        }
        return result
    }

    private fun writeAllPages(ultralight: MifareUltralight, data: ByteArray) {
        var offset = 0
        var page = 4
        while (offset < data.size) {
            val pageData = data.copyOfRange(offset, minOf(offset + 4, data.size))
                .let { if (it.size < 4) it + ByteArray(4 - it.size) else it }
            ultralight.writePage(page, pageData)
            offset += 4
            page++
        }
    }
}
