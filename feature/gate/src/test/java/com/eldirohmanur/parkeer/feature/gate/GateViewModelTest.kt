package com.eldirohmanur.parkeer.feature.gate

import android.nfc.Tag
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.PerfTracer
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
class GateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val cardReader = mockk<CardReader>()
    private val nfcTagHolder = NfcTagHolder()
    private val perfTracer = mockk<PerfTracer>()
    private val analyticsHelper = mockk<AnalyticsHelper>(relaxed = true)
    private val trace = mockk<Trace>(relaxed = true)
    private val tag = mockk<Tag>()

    private lateinit var vm: GateViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { perfTracer.startTrace(any()) } returns trace
        vm = GateViewModel(cardReader, nfcTagHolder, perfTracer, analyticsHelper)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Ready`() = runTest(testDispatcher) {
        assertEquals(GateUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `check-in succeeds for idle card`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, memberName = "Alice", visitState = VisitState.Idle)
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as GateUiState.Success
        assertEquals("Alice", state.memberName)
        verify { analyticsHelper.logEvent("check_in_success", any()) }
    }

    @Test
    fun `check-in rejects already checked-in card`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, visitState = VisitState.CheckedIn(System.currentTimeMillis()))
        coEvery { cardReader.read(tag) } returns Result.success(card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as GateUiState.Error
        assertEquals(GateError.AlreadyCheckedIn, state.error)
    }

    @Test
    fun `unreadable card emits CardNotRecognized`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.failure(Exception("IO"))

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as GateUiState.Error
        assertTrue(state.error is GateError.CardNotRecognized)
    }

    @Test
    fun `write failure emits WriteFailed`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, visitState = VisitState.Idle)
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.failure(Exception("Tag lost"))

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as GateUiState.Error
        assertTrue(state.error is GateError.WriteFailed)
    }

    @Test
    fun `simulation mode uses custom timestamp`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, memberName = "Sim", visitState = VisitState.Idle)
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        vm.updateSimulation(enabled = true, hoursAgo = 3)
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as GateUiState.Success
        // Simulated time should be roughly 3 hours ago
        val threeHoursMs = 3 * 3_600_000L
        assertTrue(System.currentTimeMillis() - state.checkInTime in (threeHoursMs - 1000)..(threeHoursMs + 1000))
    }

    @Test
    fun `tag ignored when not Ready`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, visitState = VisitState.Idle)
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        // First tap succeeds
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is GateUiState.Success)

        // Second tap should be ignored (not Ready)
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is GateUiState.Success)
    }

    @Test
    fun `reset returns to Ready`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, visitState = VisitState.Idle)
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        vm.reset()

        assertEquals(GateUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `perf trace starts and stops`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, visitState = VisitState.Idle)
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        verify { perfTracer.startTrace("nfc_checkin") }
        verify { trace.stop() }
    }
}
