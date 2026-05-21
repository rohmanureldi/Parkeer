package com.eldirohmanur.parkeer

import android.app.Application
import com.eldirohmanur.parkeer.core.firebase.AnalyticsInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class ParkeerApp : Application() {
    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default,
    )

    override fun onCreate() {
        super.onCreate()
        AnalyticsInitializer.initialize(
            application = this,
            appCoroutineScope = applicationScope,
        )
    }
}
