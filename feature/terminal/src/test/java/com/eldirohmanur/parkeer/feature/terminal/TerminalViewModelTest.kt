package com.eldirohmanur.parkeer.feature.terminal

import android.nfc.Tag
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.PerfTracer
import com.eldirohmanur.parkeer.core.model.AppConfig
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.VisitState
import com.eldirohmanur.parkeer.core.nfc.CardReader
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import com.google.firebase.perf.metrics.Trace
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TerminalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val cardReader = mockk<CardReader>()
    private val nfcTagHolder = NfcTagHolder()
    private val appConfig = object : AppConfig {
        override val ratePerHour: Int get() = 2000
    }
    private val perfTracer = mockk<PerfTracer>()
    private val analyticsHelper = mockk<AnalyticsHelper>(relaxed = true)
    private val trace = mockk<Trace>(relaxed = true)
    private val tag = mockk<Tag>()

    private lateinit var vm: TerminalViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { perfTracer.startTrace(any()) } returns trace
        vm = TerminalViewModel(cardReader, nfcTagHolder, appConfig, perfTracer, analyticsHelper)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Ready`() = runTest(testDispatcher) {
        assertEquals(TerminalUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `checkout succeeds with correct billing`() = runTest(testDispatcher) {
        val checkInTime = System.currentTimeMillis() - 7_200_000 // 2 hours ago
        val card = CardData(
            memberId = 1,
            memberName = "Bob",
            balance = 50_000,
            visitState = VisitState.CheckedIn(checkInTime),
        )
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as TerminalUiState.Success
        assertEquals(2, state.billing.hoursCharged)
        assertEquals(4_000, state.billing.fee)
        assertEquals(50_000, state.billing.oldBalance)
        assertEquals(46_000, state.billing.newBalance)
    }

    @Test
    fun `partial hour charges full hour (ceiling)`() = runTest(testDispatcher) {
        // 1 hour 1 second ago
        val checkInTime = System.currentTimeMillis() - 3_601_000
        val card = CardData(
            memberId = 1,
            memberName = "X",
            balance = 100_000,
            visitState = VisitState.CheckedIn(checkInTime),
        )
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as TerminalUiState.Success
        assertEquals(2, state.billing.hoursCharged)
        assertEquals(4_000, state.billing.fee)
    }

    @Test
    fun `insufficient balance emits InsufficientBalance`() = runTest(testDispatcher) {
        val checkInTime = System.currentTimeMillis() - 7_200_000 // 2 hours
        val card =
            CardData(memberId = 1, balance = 3_000, visitState = VisitState.CheckedIn(checkInTime))
        coEvery { cardReader.read(tag) } returns Result.success(card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as TerminalUiState.InsufficientBalance
        assertEquals(4_000, state.fee)
        assertEquals(3_000, state.balance)
        assertEquals(1_000, state.deficit)
    }

    @Test
    fun `not checked-in card emits NotCheckedIn`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, visitState = VisitState.Idle)
        coEvery { cardReader.read(tag) } returns Result.success(card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as TerminalUiState.Error
        assertEquals(TerminalError.NotCheckedIn, state.error)
    }

    @Test
    fun `unreadable card emits CardNotRecognized`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.failure(Exception("IO"))

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as TerminalUiState.Error
        assertTrue(state.error is TerminalError.CardNotRecognized)
    }

    @Test
    fun `write failure emits WriteFailed`() = runTest(testDispatcher) {
        val checkInTime = System.currentTimeMillis() - 3_600_000
        val card =
            CardData(memberId = 1, balance = 50_000, visitState = VisitState.CheckedIn(checkInTime))
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.failure(Exception("Tag lost"))

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as TerminalUiState.Error
        assertTrue(state.error is TerminalError.WriteFailed)
    }

    @Test
    fun `tag ignored when Processing`() = runTest(testDispatcher) {
        val checkInTime = System.currentTimeMillis() - 3_600_000
        val card =
            CardData(memberId = 1, balance = 50_000, visitState = VisitState.CheckedIn(checkInTime))
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is TerminalUiState.Success)

        // Second tap ignored
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is TerminalUiState.Success)
    }

    @Test
    fun `reset returns to Ready`() = runTest(testDispatcher) {
        vm.reset()
        assertEquals(TerminalUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `ratePerHour reflects appConfig`() {
        assertEquals(2000, vm.ratePerHour)
    }

    @Test
    fun `checkout logs analytics event`() = runTest(testDispatcher) {
        val checkInTime = System.currentTimeMillis() - 3_600_000
        val card = CardData(
            memberId = 1,
            memberName = "Ana",
            balance = 50_000,
            visitState = VisitState.CheckedIn(checkInTime),
        )
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        verify { analyticsHelper.logEvent("check_out_success", any()) }
    }

    @Test
    fun `invalid time emits InvalidTime error`() = runTest(testDispatcher) {
        val futureTime = System.currentTimeMillis() + 100_000
        val card =
            CardData(memberId = 1, balance = 50_000, visitState = VisitState.CheckedIn(futureTime))
        coEvery { cardReader.read(tag) } returns Result.success(card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as TerminalUiState.Error
        assertEquals(TerminalError.InvalidTime, state.error)
    }
}
