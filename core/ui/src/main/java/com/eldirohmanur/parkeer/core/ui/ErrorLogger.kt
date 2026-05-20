package com.eldirohmanur.parkeer.core.ui

import android.util.Log

/**
 * Centralized error logger. Currently logs to Logcat.
 * TODO: Integrate with Firebase Crashlytics — replace Log.e with FirebaseCrashlytics.getInstance().recordException()
 */
object ErrorLogger {
    private const val TAG = "Parkeer"

    fun log(screen: String, error: String, technicalReason: String?) {
        Log.e(TAG, "[$screen] $error | reason: $technicalReason")
        // TODO: Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().log("[$screen] $error")
        // technicalReason?.let { FirebaseCrashlytics.getInstance().recordException(Exception(it)) }
    }
}
