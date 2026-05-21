package com.eldirohmanur.parkeer.core.firebase

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.telkomsel.analytics.TselAnalytics
import com.telkomsel.analytics.TselAnalyticsSDK
import com.telkomsel.analytics.channels.CrashlyticsChannel
import com.telkomsel.analytics.channels.GoogleAnalyticsChannel
import com.telkomsel.analytics.core.AnalyticsConfiguration
import com.telkomsel.analytics.logging.AnalyticsLogLevel
import kotlinx.coroutines.CoroutineScope

object AnalyticsInitializer {
    fun initialize(application: Application, appCoroutineScope: CoroutineScope) {
        val analytics = TselAnalytics(
            application = application,
            analyticsConfiguration =
            AnalyticsConfiguration
                .builder()
                .analyticsId("parkeer")
                .enabledChannels(
                    listOf(
                        GoogleAnalyticsChannel(
                            firebaseAnalytics = FirebaseAnalytics.getInstance(application),
                            rules = emptyList(),
                            whitelistedParamKeys = setOf(),
                            appScope = appCoroutineScope,
                        ),
                        CrashlyticsChannel(FirebaseCrashlytics.getInstance()),
                    ),
                )
                .setLogLevel(AnalyticsLogLevel.DEBUG)
                .build(),
            appScope = appCoroutineScope,

        )

        TselAnalyticsSDK.initialize(analytics)
    }
}
