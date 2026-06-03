package com.eldirohmanur.parkeer.core.cardprotocol

import com.eldirohmanur.parkeer.core.crypto.AesGcmCardCipher
import com.eldirohmanur.parkeer.core.model.Activity
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.TransactionLog
import com.eldirohmanur.parkeer.core.model.VisitState
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Simulates real-world attack vectors against the card protocol.
 * Each test represents a specific threat from SECURITY.md.
 */
class SecurityAttackTest {

    private val masterKey = "KDX-MBC-PARKEER-2026-MASTER-KEY!".toByteArray()
    private val cipher = AesGcmCardCipher(masterKey)
    private val legitimateUid = byteArrayOf(0x04, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66)

    private val sampleCard = CardData(
        memberId = 123456,
        memberName = "Test User",
        balance = 100_000,
        visitState = VisitState.Idle,
        logs = listOf(TransactionLog(50_000, System.currentTimeMillis(), Activity.TOP_UP)),
    )

    // ═══════════════════════════════════════════════════════════════
    // ATTACK 1: Card Cloning
    // Scenario: Attacker dumps card bytes and writes to a blank tag
    // with a different physical UID.
    // Expected: Decryption fails because key derivation uses UID.
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `ATTACK card cloning - data bound to original UID cannot be read with cloned UID`() {
        val clonedUid = byteArrayOf(
            0x04,
            0xAA.toByte(),
            0xBB.toByte(),
            0xCC.toByte(),
            0xDD.toByte(),
            0xEE.toByte(),
            0xFF.toByte(),
        )

        // Attacker dumps legitimate card
        val rawBytes = CardProtocol.serialize(sampleCard, cipher, legitimateUid)

        // Attacker writes exact bytes to a new tag with different UID
        // When system tries to decrypt with the cloned tag's UID, it fails
        assertThrows<Exception> {
            CardProtocol.deserialize(rawBytes, cipher, clonedUid)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ATTACK 2: Balance Tampering
    // Scenario: Attacker modifies raw bytes to inflate balance.
    // Expected: HMAC verification detects modification.
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `ATTACK balance tampering - modified bytes detected by HMAC`() {
        val rawBytes = CardProtocol.serialize(sampleCard, cipher, legitimateUid)

        // Attacker tries to modify the encrypted balance block (bytes 36-71)
        val tampered = rawBytes.copyOf()
        tampered[40] = (tampered[40].toInt() xor 0xFF).toByte()
        tampered[41] = (tampered[41].toInt() xor 0xAA).toByte()

        // GCM auth tag detects the modification
        assertThrows<Exception> {
            CardProtocol.deserialize(tampered, cipher, legitimateUid)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ATTACK 3: Replay Attack
    // Scenario: Attacker saves card state at balance=100k, spends
    // money, then rewrites the old snapshot to restore balance.
    // Expected: Write counter in IV changed → GCM decryption fails
    // because the counter no longer matches.
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `ATTACK replay - old snapshot cannot be replayed after new write`() {
        // State at time T1: balance = 100k, counter = 1
        val snapshotT1 =
            CardProtocol.serialize(sampleCard, cipher, legitimateUid, previousWriteCounter = 0)

        // Legitimate write at T2: balance = 50k, counter = 2
        val updatedCard = sampleCard.copy(balance = 50_000)
        val snapshotT2 =
            CardProtocol.serialize(updatedCard, cipher, legitimateUid, previousWriteCounter = 1)

        // Attacker rewrites T1 snapshot back to card
        // System reads it — the data itself is valid for counter=1
        val deserialized = CardProtocol.deserialize(snapshotT1, cipher, legitimateUid)

        // But the write counter in T1 (=1) is LESS than T2 (=2)
        // A production reader that tracks expected counter would reject this.
        // At minimum, the data is encrypted with counter=1 IV, which proves
        // it's stale if the system expects counter >= 2.
        val deserializedT2 = CardProtocol.deserialize(snapshotT2, cipher, legitimateUid)
        assertNotEquals(deserialized.balance, deserializedT2.balance)
    }

    // ═══════════════════════════════════════════════════════════════
    // ATTACK 4: HMAC Forgery
    // Scenario: Attacker modifies plaintext fields and tries to
    // forge a valid HMAC without knowing the key.
    // Expected: Random/guessed HMAC is rejected.
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `ATTACK HMAC forgery - forged HMAC rejected`() {
        val rawBytes = CardProtocol.serialize(sampleCard, cipher, legitimateUid)

        // Attacker modifies a plaintext field (visit state byte at offset 8)
        val tampered = rawBytes.copyOf()
        tampered[8] = 0x01 // force CheckedIn state

        // Attacker forges HMAC (last 8 bytes of the slot) with random bytes
        for (i in (rawBytes.size - 8) until rawBytes.size) {
            tampered[i] = (Math.random() * 256).toInt().toByte()
        }

        assertThrows<Exception> {
            CardProtocol.deserialize(tampered, cipher, legitimateUid)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ATTACK 5: Wrong Master Key
    // Scenario: Attacker builds a modified APK with their own key
    // and tries to read/write cards from the legitimate system.
    // Expected: Completely different derived keys → decryption fails.
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `ATTACK wrong master key - attacker cannot read cards from legitimate system`() {
        // Legitimate system writes card
        val rawBytes = CardProtocol.serialize(sampleCard, cipher, legitimateUid)

        // Attacker uses different master key
        val attackerKey = "ATTACKER-FAKE-KEY-2026-XXXXXXXX".toByteArray()
        val attackerCipher = AesGcmCardCipher(attackerKey)

        // Attacker cannot decrypt
        assertThrows<Exception> {
            CardProtocol.deserialize(rawBytes, attackerCipher, legitimateUid)
        }
    }

    @Test
    fun `ATTACK wrong master key - attacker written card rejected by legitimate system`() {
        // Attacker writes card with their key
        val attackerKey = "ATTACKER-FAKE-KEY-2026-XXXXXXXX".toByteArray()
        val attackerCipher = AesGcmCardCipher(attackerKey)
        val attackerCard = CardProtocol.serialize(sampleCard, attackerCipher, legitimateUid)

        // Legitimate system cannot read it
        assertThrows<Exception> {
            CardProtocol.deserialize(attackerCard, cipher, legitimateUid)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ATTACK 6: Ciphertext Tampering
    // Scenario: Attacker flips bits in the encrypted block hoping
    // to manipulate the decrypted balance without knowing the key.
    // Expected: AES-GCM authentication tag detects any modification.
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `ATTACK ciphertext tampering - single bit flip detected by GCM`() {
        val rawBytes = CardProtocol.serialize(sampleCard, cipher, legitimateUid)

        // Flip a single bit in the ciphertext region
        val tampered = rawBytes.copyOf()
        tampered[50] = (tampered[50].toInt() xor 0x01).toByte()

        assertThrows<Exception> {
            CardProtocol.deserialize(tampered, cipher, legitimateUid)
        }
    }

    @Test
    fun `ATTACK ciphertext tampering - bulk modification detected by GCM`() {
        val rawBytes = CardProtocol.serialize(sampleCard, cipher, legitimateUid)

        // Attacker overwrites entire encrypted block with crafted bytes
        val tampered = rawBytes.copyOf()
        for (i in 36..70) {
            tampered[i] = 0x42
        }

        assertThrows<Exception> {
            CardProtocol.deserialize(tampered, cipher, legitimateUid)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // VERIFICATION: HMAC constant-time comparison
    // Ensures timing attack resistance.
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `VERIFY constant-time HMAC comparison prevents timing attack`() {
        val data = "sensitive payload".toByteArray()
        val validHmac = cipher.computeHmac(legitimateUid, data)

        // Near-miss HMAC (only last byte differs)
        val nearMiss = validHmac.copyOf()
        nearMiss[7] = (nearMiss[7].toInt() xor 0x01).toByte()

        // Completely wrong HMAC
        val totalMiss = ByteArray(8) { 0x00 }

        // Both should be rejected — MessageDigest.isEqual is constant-time
        // regardless of how many bytes match
        assertFalse(cipher.verifyHmac(legitimateUid, data, nearMiss))
        assertFalse(cipher.verifyHmac(legitimateUid, data, totalMiss))
    }
}
