package com.kdx.parkeer.core.crypto

/**
 * Handles encryption/decryption and HMAC for card data.
 */
interface CardCipher {
    fun encrypt(cardUid: ByteArray, writeCounter: Int, plaintext: ByteArray): ByteArray
    fun decrypt(cardUid: ByteArray, writeCounter: Int, ciphertext: ByteArray): ByteArray
    fun computeHmac(cardUid: ByteArray, data: ByteArray): ByteArray
    fun verifyHmac(cardUid: ByteArray, data: ByteArray, hmac: ByteArray): Boolean
}
