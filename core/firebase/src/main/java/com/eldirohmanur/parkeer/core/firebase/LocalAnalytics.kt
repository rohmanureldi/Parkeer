package com.eldirohmanur.parkeer.core.firebase

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAnalytics = staticCompositionLocalOf<AnalyticsHelper> {
    error("No AnalyticsHelper provided")
}
