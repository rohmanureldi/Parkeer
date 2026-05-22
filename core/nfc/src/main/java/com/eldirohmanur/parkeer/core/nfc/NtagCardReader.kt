package com.eldirohmanur.parkeer.core.nfc

import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import com.eldirohmanur.parkeer.core.cardprotocol.CardProtocol
import com.eldirohmanur.parkeer.core.crypto.CardCipher
import com.eldirohmanur.parkeer.core.model.CardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

/**
 * Double-buffer NFC reader/writer for NTAG215 (504 bytes user memory).
 *
 * Card layout:
 *   Page 4 (4 bytes):       Slot pointer [activeSlot, 0, 0, 0]
 *   Pages 5–40 (144 bytes): Slot A
 *   Pages 41–76 (144 bytes): Slot B
 *
 * Write strategy:
 *   1. Read active slot pointer
 *   2. Write new data to INACTIVE slot
 *   3. Flip pointer to the newly written slot (single atomic page write)
 *
 * If interrupted before step 3, the active slot is untouched.
 */
class NtagCardReader @Inject constructor(private val cipher: CardCipher) : CardReader {

    companion object {
        private const val POINTER_PAGE = 4 // page 4 = first user memory page
        private const val SLOT_A_START_PAGE = 5 // pages 5–40
        private const val SLOT_B_START_PAGE = 41 // pages 41–76
        private const val SLOT_PAGES = 36 // 144 bytes / 4 bytes per page
    }

    override suspend fun read(tag: Tag): Result<CardData> = withContext(Dispatchers.IO) {
        runCatching {
            val ultralight = MifareUltralight.get(tag)
                ?: error("Not a MifareUltralight tag")
            ultralight.connect()
            try {
                val activeSlot = readActiveSlot(ultralight)
                val startPage = if (activeSlot == 0) SLOT_A_START_PAGE else SLOT_B_START_PAGE
                val raw = readSlot(ultralight, startPage)
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
            ultralight.use { ultralight ->
                val activeSlot = readActiveSlot(ultralight)
                val inactiveSlot = 1 - activeSlot
                val writeStartPage = if (inactiveSlot == 0) SLOT_A_START_PAGE else SLOT_B_START_PAGE

                // Read write counter from active slot
                val activeStartPage = if (activeSlot == 0) SLOT_A_START_PAGE else SLOT_B_START_PAGE
                val activeHeader = ultralight.readPages(activeStartPage) // 16 bytes (4 pages)
                val previousCounter =
                    ByteBuffer.wrap(activeHeader, 4, 4).order(ByteOrder.BIG_ENDIAN).int

                // Serialize new data
                val raw = CardProtocol.serialize(data, cipher, tag.id, previousCounter)

                // Step 1: Write to INACTIVE slot
                writeSlot(ultralight, writeStartPage, raw)

                // Step 2: Verify the write
                val readBack = readSlot(ultralight, writeStartPage)
                require(raw.contentEquals(readBack)) { "Write verification failed" }

                // Step 3: Flip pointer (atomic — single 4-byte page write)
                ultralight.writePage(POINTER_PAGE, byteArrayOf(inactiveSlot.toByte(), 0, 0, 0))
            }
        }
    }

    override suspend fun wipe(tag: Tag): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val ultralight = MifareUltralight.get(tag)
                ?: error("Not a MifareUltralight tag")
            ultralight.connect()
            ultralight.use { ultralight ->
                val emptyPage = ByteArray(4)
                // Wipe pointer
                ultralight.writePage(POINTER_PAGE, emptyPage)
                // Wipe both slots
                for (page in SLOT_A_START_PAGE until SLOT_A_START_PAGE + SLOT_PAGES) {
                    ultralight.writePage(page, emptyPage)
                }
                for (page in SLOT_B_START_PAGE until SLOT_B_START_PAGE + SLOT_PAGES) {
                    ultralight.writePage(page, emptyPage)
                }
            }
        }
    }

    private fun readActiveSlot(ultralight: MifareUltralight): Int {
        val pointerData = ultralight.readPages(POINTER_PAGE) // returns 16 bytes
        return pointerData[0].toInt() and 0x01 // 0 = slot A, 1 = slot B
    }

    private fun readSlot(ultralight: MifareUltralight, startPage: Int): ByteArray {
        val result = ByteArray(CardProtocol.SLOT_SIZE)
        var offset = 0
        var page = startPage
        while (offset < CardProtocol.SLOT_SIZE) {
            val data = ultralight.readPages(page) // reads 16 bytes (4 pages)
            val toCopy = minOf(16, CardProtocol.SLOT_SIZE - offset)
            System.arraycopy(data, 0, result, offset, toCopy)
            offset += 16
            page += 4
        }
        return result
    }

    private fun writeSlot(ultralight: MifareUltralight, startPage: Int, data: ByteArray) {
        // Read existing inactive slot to skip unchanged pages (delta write)
        val existing = readSlot(ultralight, startPage)
        var offset = 0
        var page = startPage
        while (offset < data.size) {
            val pageData = data.copyOfRange(offset, minOf(offset + 4, data.size))
                .let { if (it.size < 4) it + ByteArray(4 - it.size) else it }
            val existingPage = existing.copyOfRange(offset, minOf(offset + 4, existing.size))
                .let { if (it.size < 4) it + ByteArray(4 - it.size) else it }
            if (!pageData.contentEquals(existingPage)) {
                ultralight.writePage(page, pageData)
            }
            offset += 4
            page++
        }
    }
}
