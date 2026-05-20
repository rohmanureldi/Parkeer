package com.eldirohmanur.parkeer.core.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsHelper {
    fun logButtonClick(buttonText: String, screen: String)
    fun logScreen(screen: String)
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}

@Singleton
class FirebaseAnalyticsHelper @Inject constructor(private val analytics: FirebaseAnalytics) : AnalyticsHelper {

    override fun logButtonClick(buttonText: String, screen: String) {
        analytics.logEvent("button_click") {
            param("button_text", buttonText)
            param("screen_name", screen)
        }
    }

    override fun logScreen(screen: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screen)
        }
    }

    override fun logEvent(name: String, params: Map<String, String>) {
        analytics.logEvent(name) {
            params.forEach { (key, value) -> param(key, value) }
        }
    }
}
