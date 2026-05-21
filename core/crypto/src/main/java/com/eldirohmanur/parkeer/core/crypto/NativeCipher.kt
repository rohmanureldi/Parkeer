package com.eldirohmanur.parkeer.core.crypto

object NativeCipher {

    init {
        System.loadLibrary("parkeer_crypto")
    }

    external fun getMasterKey(): ByteArray

    external fun checkIntegrity(): Boolean
}
