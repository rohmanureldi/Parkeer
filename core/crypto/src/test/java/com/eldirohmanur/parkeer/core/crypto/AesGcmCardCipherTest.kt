package com.eldirohmanur.parkeer.core.crypto

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AesGcmCardCipherTest {

    private val masterKey = ByteArray(32) { it.toByte() }
    private val cardUid = byteArrayOf(0x04, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66)
    private val cipher = AesGcmCardCipher(masterKey)

    @Test
    fun `encrypt and decrypt round-trip`() {
        val plaintext = "hello world".toByteArray()
        val encrypted = cipher.encrypt(cardUid, 1, plaintext)
        val decrypted = cipher.decrypt(cardUid, 1, encrypted)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `different write counter produces different ciphertext`() {
        val plaintext = "same data".toByteArray()
        val enc1 = cipher.encrypt(cardUid, 1, plaintext)
        val enc2 = cipher.encrypt(cardUid, 2, plaintext)
        assertFalse(enc1.contentEquals(enc2))
    }

    @Test
    fun `different card UID produces different ciphertext`() {
        val plaintext = "test".toByteArray()
        val uid2 = byteArrayOf(
            0x04,
            0xAA.toByte(),
            0xBB.toByte(),
            0xCC.toByte(),
            0xDD.toByte(),
            0xEE.toByte(),
            0xFF.toByte(),
        )
        val enc1 = cipher.encrypt(cardUid, 1, plaintext)
        val enc2 = cipher.encrypt(uid2, 1, plaintext)
        assertFalse(enc1.contentEquals(enc2))
    }

    @Test
    fun `decrypt with wrong counter fails`() {
        val plaintext = "secret".toByteArray()
        val encrypted = cipher.encrypt(cardUid, 5, plaintext)
        assertThrows<Exception> { cipher.decrypt(cardUid, 6, encrypted) }
    }

    @Test
    fun `decrypt with wrong UID fails`() {
        val plaintext = "secret".toByteArray()
        val encrypted = cipher.encrypt(cardUid, 1, plaintext)
        val wrongUid = byteArrayOf(0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        assertThrows<Exception> { cipher.decrypt(wrongUid, 1, encrypted) }
    }

    @Test
    fun `computeHmac returns 8 bytes`() {
        val data = "some data".toByteArray()
        val hmac = cipher.computeHmac(cardUid, data)
        assertEquals(8, hmac.size)
    }

    @Test
    fun `computeHmac is deterministic`() {
        val data = "deterministic".toByteArray()
        val h1 = cipher.computeHmac(cardUid, data)
        val h2 = cipher.computeHmac(cardUid, data)
        assertArrayEquals(h1, h2)
    }

    @Test
    fun `computeHmac differs for different data`() {
        val h1 = cipher.computeHmac(cardUid, "a".toByteArray())
        val h2 = cipher.computeHmac(cardUid, "b".toByteArray())
        assertFalse(h1.contentEquals(h2))
    }

    @Test
    fun `verifyHmac returns true for correct hmac`() {
        val data = "verify me".toByteArray()
        val hmac = cipher.computeHmac(cardUid, data)
        assertTrue(cipher.verifyHmac(cardUid, data, hmac))
    }

    @Test
    fun `verifyHmac returns false for tampered hmac`() {
        val data = "verify me".toByteArray()
        val hmac = cipher.computeHmac(cardUid, data)
        hmac[0] = (hmac[0].toInt() xor 0xFF).toByte()
        assertFalse(cipher.verifyHmac(cardUid, data, hmac))
    }

    @Test
    fun `verifyHmac returns false for wrong UID`() {
        val data = "check".toByteArray()
        val hmac = cipher.computeHmac(cardUid, data)
        val wrongUid = byteArrayOf(0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        assertFalse(cipher.verifyHmac(wrongUid, data, hmac))
    }

    @Test
    fun `different master key produces different encryption`() {
        val otherKey = ByteArray(32) { (it + 100).toByte() }
        val otherCipher = AesGcmCardCipher(otherKey)
        val plaintext = "test".toByteArray()
        val enc1 = cipher.encrypt(cardUid, 1, plaintext)
        val enc2 = otherCipher.encrypt(cardUid, 1, plaintext)
        assertFalse(enc1.contentEquals(enc2))
    }

    @Test
    fun `encrypt produces ciphertext longer than plaintext due to GCM tag`() {
        val plaintext = "x".toByteArray()
        val encrypted = cipher.encrypt(cardUid, 1, plaintext)
        // GCM adds 16-byte auth tag
        assertEquals(plaintext.size + 16, encrypted.size)
    }

    @Test
    fun `large payload encrypt-decrypt`() {
        val plaintext = ByteArray(144) { (it % 256).toByte() }
        val encrypted = cipher.encrypt(cardUid, 99, plaintext)
        val decrypted = cipher.decrypt(cardUid, 99, encrypted)
        assertArrayEquals(plaintext, decrypted)
    }
}
