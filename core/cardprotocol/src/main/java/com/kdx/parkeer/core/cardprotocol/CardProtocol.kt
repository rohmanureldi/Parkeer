package com.kdx.parkeer.core.cardprotocol

import com.kdx.parkeer.core.crypto.CardCipher
import com.kdx.parkeer.core.model.Activity
import com.kdx.parkeer.core.model.CardData
import com.kdx.parkeer.core.model.TransactionLog
import com.kdx.parkeer.core.model.VisitState
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Binary wire format for NFC card memory (per-slot).
 *
 * Layout (144 bytes per slot):
 * [0..1]   Magic 0x4C44
 * [2]      Version 0x01
 * [3]      Flags (bit0=registered, bit1=active)
 * [4..7]   Write counter (monotonic nonce for AES-GCM IV derivation)
 * [8..43]  Encrypted block (ID:4 + Name:12 + Balance:4 = 20B plaintext → 36B with GCM tag)
 * [44]     Visit state (0x00=IDLE, 0x01=CHECKED_IN)
 * [45..47] Padding
 * [48..55] Check-in timestamp (Long)
 * [56..135] Transaction logs (5 × 16B)
 * [136..143] HMAC (8 bytes)
 *
 * NTAG215 card layout (504 bytes user memory, pages 4–129):
 * Page 4 (bytes 0–3):     Slot pointer [0x00=A, 0x01=B, xx, xx]
 * Pages 5–40 (144 bytes): Slot A
 * Pages 41–76 (144 bytes): Slot B
 */
object CardProtocol {

    const val MAGIC = 0x4C44.toShort()
    const val VERSION: Byte = 0x01
    const val SLOT_SIZE = 144
    private const val ENCRYPTED_SIZE = 36
    private const val STATE_OFFSET = 44
    private const val LOG_ENTRY_SIZE = 16
    private const val LOG_COUNT = 5
    private const val HMAC_OFFSET = 136
    private const val HMAC_SIZE = 8

    fun serialize(data: CardData, cipher: CardCipher, cardUid: ByteArray, previousWriteCounter: Int = 0): ByteArray {
        val writeCounter = previousWriteCounter + 1
        val buf = ByteBuffer.allocate(SLOT_SIZE).order(ByteOrder.BIG_ENDIAN)

        // Header
        buf.putShort(MAGIC)
        buf.put(VERSION)
        buf.put(0x03) // registered + active

        // Write counter (monotonic nonce for AES-GCM IV)
        buf.putInt(writeCounter)

        // Validate member name fits in 12 bytes UTF-8
        val nameBytes = data.memberName.toByteArray(Charsets.UTF_8)
        require(nameBytes.size <= 12) { "Member name exceeds 12 bytes in UTF-8: ${nameBytes.size}" }

        // Encrypted block: ID(4) + Name(12) + Balance(4) = 20 bytes plaintext
        val plaintext = ByteBuffer.allocate(20).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(data.memberId)
            put(nameBytes.copyOf(12))
            putInt(data.balance)
        }.array()
        val encrypted = cipher.encrypt(cardUid, writeCounter, plaintext)
        buf.put(encrypted.copyOf(ENCRYPTED_SIZE))

        // Visit state
        val stateFlag: Byte = if (data.visitState is VisitState.CheckedIn) 0x01 else 0x00
        buf.put(stateFlag)
        buf.put(ByteArray(3)) // padding

        // Timestamp
        val timestamp = when (val s = data.visitState) {
            is VisitState.CheckedIn -> s.timestamp
            is VisitState.Idle -> 0L
        }
        buf.putLong(timestamp)

        // Transaction logs (5 × 16 bytes)
        for (i in 0 until LOG_COUNT) {
            if (i < data.logs.size) {
                val log = data.logs[i]
                buf.putInt(log.amount)
                buf.putLong(log.timestamp)
                buf.put(log.activity.code)
                buf.put(ByteArray(3)) // padding
            } else {
                buf.put(ByteArray(LOG_ENTRY_SIZE))
            }
        }

        // Compute HMAC over non-encrypted fields (header + counter + state + timestamp + logs)
        val hmacInput = ByteArray(4 + 4 + 1 + 3 + 8 + (LOG_COUNT * LOG_ENTRY_SIZE))
        val arr = buf.array()
        System.arraycopy(arr, 0, hmacInput, 0, 4) // header
        System.arraycopy(arr, 4, hmacInput, 4, 4) // write counter
        System.arraycopy(arr, STATE_OFFSET, hmacInput, 8, 1 + 3 + 8 + (LOG_COUNT * LOG_ENTRY_SIZE))
        val hmac = cipher.computeHmac(cardUid, hmacInput)
        buf.put(hmac.copyOf(HMAC_SIZE))

        return buf.array()
    }

    fun deserialize(raw: ByteArray, cipher: CardCipher, cardUid: ByteArray): CardData {
        require(raw.size >= SLOT_SIZE) { "Invalid card data size: ${raw.size}" }
        val buf = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN)

        // Verify magic
        val magic = buf.short
        require(magic == MAGIC) { "Invalid magic: ${magic.toString(16)}" }

        // Version
        val version = buf.get()
        require(version == VERSION) { "Unsupported version: $version" }

        // Flags
        val flags = buf.get()
        require(flags.toInt() and 0x01 == 1) { "Card not registered" }

        // Write counter
        val writeCounter = buf.int

        // Decrypt identity + balance
        val encryptedBlock = ByteArray(ENCRYPTED_SIZE)
        buf.get(encryptedBlock)
        val plaintext = cipher.decrypt(cardUid, writeCounter, encryptedBlock)
        val ptBuf = ByteBuffer.wrap(plaintext).order(ByteOrder.BIG_ENDIAN)
        val memberId = ptBuf.int
        val nameBytes = ByteArray(12)
        ptBuf.get(nameBytes)
        val memberName = String(nameBytes, Charsets.UTF_8).trimEnd('\u0000')
        val balance = ptBuf.int

        // Visit state
        val stateFlag = buf.get()
        buf.get(ByteArray(3)) // padding
        val timestamp = buf.long
        val visitState = if (stateFlag == 0x01.toByte()) {
            VisitState.CheckedIn(timestamp)
        } else {
            VisitState.Idle
        }

        // Transaction logs
        val logs = mutableListOf<TransactionLog>()
        for (i in 0 until LOG_COUNT) {
            val amount = buf.int
            val logTimestamp = buf.long
            val activityCode = buf.get()
            buf.get(ByteArray(3)) // padding
            if (amount != 0 || logTimestamp != 0L) {
                val activity = Activity.entries.find { it.code == activityCode } ?: Activity.PARKING
                logs.add(TransactionLog(amount, logTimestamp, activity))
            }
        }

        // Verify HMAC
        val storedHmac = ByteArray(HMAC_SIZE)
        buf.get(storedHmac)
        val hmacInput = ByteArray(4 + 4 + 1 + 3 + 8 + (LOG_COUNT * LOG_ENTRY_SIZE))
        System.arraycopy(raw, 0, hmacInput, 0, 4)
        System.arraycopy(raw, 4, hmacInput, 4, 4)
        System.arraycopy(raw, STATE_OFFSET, hmacInput, 8, 1 + 3 + 8 + (LOG_COUNT * LOG_ENTRY_SIZE))
        require(
            cipher.verifyHmac(
                cardUid,
                hmacInput,
                storedHmac,
            ),
        ) { "Card data corrupted — please re-register at Station" }

        return CardData(
            memberId = memberId,
            memberName = memberName,
            balance = balance,
            visitState = visitState,
            logs = logs,
        )
    }

    fun isValidCard(raw: ByteArray): Boolean {
        if (raw.size < 4) return false
        val buf = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN)
        return buf.short == MAGIC && buf.get() == VERSION
    }
}
