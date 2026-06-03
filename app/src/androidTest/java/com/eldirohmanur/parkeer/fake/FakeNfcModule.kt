package com.eldirohmanur.parkeer.fake

import com.eldirohmanur.parkeer.core.model.AppConfig
import com.eldirohmanur.parkeer.core.nfc.CardReader
import com.eldirohmanur.parkeer.core.nfc.di.NfcModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NfcModule::class])
object FakeNfcModule {

    private val fakeCardReader = FakeCardReader()

    @Provides
    @Singleton
    fun provideCardReader(): CardReader = fakeCardReader

    @Provides
    @Singleton
    fun provideFakeCardReader(): FakeCardReader = fakeCardReader

    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = object : AppConfig {}
}
