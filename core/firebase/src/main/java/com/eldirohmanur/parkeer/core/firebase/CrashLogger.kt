package com.eldirohmanur.parkeer.core.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

interface CrashLogger {
    fun log(message: String)
    fun recordException(throwable: Throwable)
    fun setUserId(id: String)
}

@Singleton
class FirebaseCrashLogger @Inject constructor(private val crashlytics: FirebaseCrashlytics) : CrashLogger {

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun setUserId(id: String) {
        crashlytics.setUserId(id)
    }
}
