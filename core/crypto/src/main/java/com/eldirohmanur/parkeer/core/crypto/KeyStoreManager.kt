package com.eldirohmanur.parkeer.core.crypto

object KeyStoreManager {

    /**
     * Returns a 32-byte master key derived from NDK native layer.
     * The native key is obfuscated (XOR-masked, split parts) and protected
     * by anti-debug (ptrace) and anti-Frida checks.
     *
     * No Android Keystore dependency — key survives app reinstall,
     * ensuring existing NFC cards remain readable across installations.
     */
    fun getMasterKey(): ByteArray = NativeCipher.getMasterKey()
}
