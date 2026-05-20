package com.kdx.parkeer.core.nfc.di

import com.kdx.parkeer.core.crypto.AesGcmCardCipher
import com.kdx.parkeer.core.crypto.CardCipher
import com.kdx.parkeer.core.model.AppConfig
import com.kdx.parkeer.core.nfc.CardReader
import com.kdx.parkeer.core.nfc.NtagCardReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NfcModule {

    // Hardcoded 32-byte key. Production would use Android Keystore.
    private val MASTER_KEY = "KDX-MBC-PARKEER-2026-MASTER-KEY!".toByteArray()

    @Provides
    @Singleton
    fun provideCipher(): CardCipher = AesGcmCardCipher(MASTER_KEY)

    @Provides
    @Singleton
    fun provideCardReader(cipher: CardCipher): CardReader = NtagCardReader(cipher)

    /**
     * Future: replace with
     *     fun provideAppConfig(remoteConfig: FirebaseRemoteConfig): AppConfig = object : AppConfig {
     *         override val ratePerHour: Int get() = remoteConfig.getLong("rate_per_hour").toInt()
     *     }
     */
    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = object : AppConfig {}
}
