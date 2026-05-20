package com.eldirohmanur.parkeer.core.cardprotocol

import com.eldirohmanur.parkeer.core.crypto.AesGcmCardCipher
import com.eldirohmanur.parkeer.core.model.Activity
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.TransactionLog
import com.eldirohmanur.parkeer.core.model.VisitState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CardProtocolTest {

    private val masterKey = "KDX-MBC-PARKEER-2026-MASTER-KEY!".toByteArray()
    private val cipher = AesGcmCardCipher(masterKey)
    private val cardUid = byteArrayOf(0x04, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66)

    @Test
    fun `serialize and deserialize round-trip preserves data`() {
        val data = CardData(
            memberId = 12345,
            memberName = "Alice",
            balance = 50000,
            visitState = VisitState.CheckedIn(1700000000000L),
            logs = listOf(
                TransactionLog(10000, 1700000000000L, Activity.PARKING),
                TransactionLog(50000, 1699999000000L, Activity.TOP_UP),
            ),
        )

        val raw = CardProtocol.serialize(data, cipher, cardUid, previousWriteCounter = 0)
        val restored = CardProtocol.deserialize(raw, cipher, cardUid)

        assertEquals(data.memberId, restored.memberId)
        assertEquals(data.memberName, restored.memberName)
        assertEquals(data.balance, restored.balance)
        assertEquals(data.visitState, restored.visitState)
        assertEquals(data.logs.size, restored.logs.size)
        assertEquals(data.logs[0].amount, restored.logs[0].amount)
        assertEquals(data.logs[0].activity, restored.logs[0].activity)
    }

    @Test
    fun `serialize produces correct slot size`() {
        val data = CardData(memberId = 1, memberName = "Bob", balance = 0)
        val raw = CardProtocol.serialize(data, cipher, cardUid)
        assertEquals(CardProtocol.SLOT_SIZE, raw.size)
    }

    @Test
    fun `deserialize rejects tampered HMAC`() {
        val data = CardData(memberId = 1, memberName = "Test", balance = 100)
        val raw = CardProtocol.serialize(data, cipher, cardUid)

        // Tamper with a byte in the state area (covered by HMAC)
        raw[44] = 0x01

        assertThrows<IllegalArgumentException> {
            CardProtocol.deserialize(raw, cipher, cardUid)
        }
    }

    @Test
    fun `deserialize rejects wrong card UID`() {
        val data = CardData(memberId = 1, memberName = "Test", balance = 100)
        val raw = CardProtocol.serialize(data, cipher, cardUid)

        val wrongUid = byteArrayOf(
            0x04,
            0xAA.toByte(),
            0xBB.toByte(),
            0xCC.toByte(),
            0xDD.toByte(),
            0xEE.toByte(),
            0xFF.toByte(),
        )

        assertThrows<Exception> {
            CardProtocol.deserialize(raw, cipher, wrongUid)
        }
    }

    @Test
    fun `isValidCard checks magic and version`() {
        val data = CardData(memberId = 1, memberName = "X", balance = 0)
        val raw = CardProtocol.serialize(data, cipher, cardUid)

        assertTrue(CardProtocol.isValidCard(raw))
        assertFalse(CardProtocol.isValidCard(byteArrayOf(0x00, 0x00, 0x00, 0x00)))
        assertFalse(CardProtocol.isValidCard(byteArrayOf(0x01)))
    }

    @Test
    fun `serialize rejects name exceeding 12 bytes UTF-8`() {
        val data = CardData(memberId = 1, memberName = "VeryLongNameThatExceeds", balance = 0)

        assertThrows<IllegalArgumentException> {
            CardProtocol.serialize(data, cipher, cardUid)
        }
    }

    @Test
    fun `write counter increments on each serialize`() {
        val data = CardData(memberId = 1, memberName = "A", balance = 0)

        val raw1 = CardProtocol.serialize(data, cipher, cardUid, previousWriteCounter = 0)
        val raw2 = CardProtocol.serialize(data, cipher, cardUid, previousWriteCounter = 1)

        // Encrypted blocks should differ due to different IVs
        val enc1 = raw1.copyOfRange(8, 44)
        val enc2 = raw2.copyOfRange(8, 44)
        assertFalse(enc1.contentEquals(enc2))
    }

    @Test
    fun `idle visit state round-trips correctly`() {
        val data = CardData(
            memberId = 99,
            memberName = "Idle",
            balance = 25000,
            visitState = VisitState.Idle,
        )
        val raw = CardProtocol.serialize(data, cipher, cardUid)
        val restored = CardProtocol.deserialize(raw, cipher, cardUid)
        assertEquals(VisitState.Idle, restored.visitState)
    }
}
