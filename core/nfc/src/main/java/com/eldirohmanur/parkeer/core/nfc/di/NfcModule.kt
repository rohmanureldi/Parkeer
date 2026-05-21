package com.eldirohmanur.parkeer.core.nfc.di

import com.eldirohmanur.parkeer.core.crypto.AesGcmCardCipher
import com.eldirohmanur.parkeer.core.crypto.CardCipher
import com.eldirohmanur.parkeer.core.crypto.KeyStoreManager
import com.eldirohmanur.parkeer.core.model.AppConfig
import com.eldirohmanur.parkeer.core.nfc.CardReader
import com.eldirohmanur.parkeer.core.nfc.NtagCardReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NfcModule {

    @Provides
    @Singleton
    fun provideCipher(): CardCipher = AesGcmCardCipher(KeyStoreManager.getMasterKey())

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
