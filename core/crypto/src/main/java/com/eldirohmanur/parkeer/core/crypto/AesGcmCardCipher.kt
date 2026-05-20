package com.eldirohmanur.parkeer.core.crypto

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesGcmCardCipher(private val masterKey: ByteArray) : CardCipher {

    override fun encrypt(cardUid: ByteArray, writeCounter: Int, plaintext: ByteArray): ByteArray {
        val key = deriveKey(cardUid)
        val iv = buildIv(cardUid, writeCounter)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        return cipher.doFinal(plaintext)
    }

    override fun decrypt(cardUid: ByteArray, writeCounter: Int, ciphertext: ByteArray): ByteArray {
        val key = deriveKey(cardUid)
        val iv = buildIv(cardUid, writeCounter)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    override fun computeHmac(cardUid: ByteArray, data: ByteArray): ByteArray {
        val key = deriveKey(cardUid)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data).copyOf(8) // truncated to 8 bytes
    }

    override fun verifyHmac(cardUid: ByteArray, data: ByteArray, hmac: ByteArray): Boolean {
        val computed = computeHmac(cardUid, data)
        return MessageDigest.isEqual(computed, hmac)
    }

    /**
     * Builds a 12-byte IV from cardUid (first 7 bytes) + writeCounter (4 bytes) + padding.
     * Guarantees uniqueness per card per write operation.
     */
    private fun buildIv(cardUid: ByteArray, writeCounter: Int): ByteArray {
        val iv = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN)
        iv.put(cardUid.copyOf(7))
        iv.put(0x00) // separator
        iv.putInt(writeCounter)
        return iv.array()
    }

    private fun deriveKey(cardUid: ByteArray): ByteArray {
        val prk = hmacSha256(cardUid, masterKey)
        val info = "kdx-mbc-v1".toByteArray()
        return hmacSha256(prk, info + byteArrayOf(0x01))
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }
}
