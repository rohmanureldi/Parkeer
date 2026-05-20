package com.eldirohmanur.parkeer.core.firebase

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject
import javax.inject.Singleton

interface PerfTracer {
    fun startTrace(name: String): Trace
}

@Singleton
class FirebasePerfTracer @Inject constructor(private val performance: FirebasePerformance) : PerfTracer {

    override fun startTrace(name: String): Trace = performance.newTrace(name).also { it.start() }
}
