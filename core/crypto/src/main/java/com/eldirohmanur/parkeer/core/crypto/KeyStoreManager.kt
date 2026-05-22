package com.eldirohmanur.parkeer.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

object KeyStoreManager {

    private const val KEY_ALIAS = "parkeer_master_key"
    private const val KEYSTORE = "AndroidKeyStore"

    /**
     * Returns a 32-byte master key derived from:
     * 1. NDK native key (obfuscated in .so, anti-debug protected)
     * 2. Android Keystore key (hardware-backed, non-exportable)
     *
     * Both are combined via HMAC to produce the final key.
     * An attacker needs BOTH to derive card keys.
     */
    fun getMasterKey(): ByteArray {
        val nativeKey = NativeCipher.getMasterKey()
        val keystoreKey = getOrCreateKeystoreKey()
        // Combine: HMAC(keystoreKey, nativeKey) → final 32-byte master
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(keystoreKey)
        return mac.doFinal(nativeKey)
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN,
        )
            .setKeySize(256)
            .build()

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, KEYSTORE)
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
