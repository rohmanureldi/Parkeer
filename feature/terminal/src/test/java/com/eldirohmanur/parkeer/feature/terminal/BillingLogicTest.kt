package com.eldirohmanur.parkeer.feature.terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BillingLogicTest {

    companion object {
        const val RATE_PER_HOUR = 2000
    }

    private fun calculateFee(durationMs: Long): Pair<Int, Int> {
        val durationSeconds = durationMs / 1000
        val hoursCharged = ((durationSeconds + 3599) / 3600).toInt()
        val fee = hoursCharged * RATE_PER_HOUR
        return hoursCharged to fee
    }

    @Test
    fun `1 minute charges 1 hour`() {
        val (hours, fee) = calculateFee(60_000L)
        assertEquals(1, hours)
        assertEquals(2000, fee)
    }

    @Test
    fun `exactly 1 hour charges 1 hour`() {
        val (hours, fee) = calculateFee(3_600_000L)
        assertEquals(1, hours)
        assertEquals(2000, fee)
    }

    @Test
    fun `1 hour 1 second charges 2 hours`() {
        val (hours, fee) = calculateFee(3_601_000L)
        assertEquals(2, hours)
        assertEquals(4000, fee)
    }

    @Test
    fun `3 hours 30 minutes charges 4 hours`() {
        val (hours, fee) = calculateFee(3 * 3_600_000L + 30 * 60_000L)
        assertEquals(4, hours)
        assertEquals(8000, fee)
    }

    @Test
    fun `balance subtraction with coerceAtLeast prevents negative`() {
        val balance = 1000
        val fee = 5000
        val newBalance = (balance - fee).coerceAtLeast(0)
        assertEquals(0, newBalance)
    }

    @Test
    fun `normal balance subtraction`() {
        val balance = 50000
        val fee = 4000
        val newBalance = (balance - fee).coerceAtLeast(0)
        assertEquals(46000, newBalance)
    }

    @Test
    fun `insufficient balance detected correctly`() {
        val balance = 3000
        val fee = 4000
        assertTrue(balance < fee)
        assertEquals(1000, fee - balance)
    }
}
