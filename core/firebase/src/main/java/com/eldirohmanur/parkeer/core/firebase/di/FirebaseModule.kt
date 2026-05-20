package com.eldirohmanur.parkeer.core.firebase.di

import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.CrashLogger
import com.eldirohmanur.parkeer.core.firebase.FirebaseAnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.FirebaseCrashLogger
import com.eldirohmanur.parkeer.core.firebase.FirebasePerfTracer
import com.eldirohmanur.parkeer.core.firebase.PerfTracer
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.ktx.performance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = Firebase.crashlytics

    @Provides
    @Singleton
    fun provideFirebasePerformance(): FirebasePerformance = Firebase.performance

    @Provides
    @Singleton
    fun provideAnalyticsHelper(analytics: FirebaseAnalytics): AnalyticsHelper = FirebaseAnalyticsHelper(analytics)

    @Provides
    @Singleton
    fun provideCrashLogger(crashlytics: FirebaseCrashlytics): CrashLogger = FirebaseCrashLogger(crashlytics)

    @Provides
    @Singleton
    fun providePerfTracer(performance: FirebasePerformance): PerfTracer = FirebasePerfTracer(performance)
}
