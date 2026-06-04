package com.eldirohmanur.parkeer.feature.station

import android.nfc.Tag
import app.cash.turbine.test
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.PerfTracer
import com.eldirohmanur.parkeer.core.model.CardData
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
class StationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val cardReader = mockk<CardReader>()
    private val nfcTagHolder = NfcTagHolder()
    private val perfTracer = mockk<PerfTracer>()
    private val analyticsHelper = mockk<AnalyticsHelper>(relaxed = true)
    private val trace = mockk<Trace>(relaxed = true)
    private val tag = mockk<Tag>()

    private lateinit var vm: StationViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { perfTracer.startTrace(any()) } returns trace
        vm = StationViewModel(cardReader, nfcTagHolder, perfTracer, analyticsHelper)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest(testDispatcher) {
        assertEquals(StationUiState.Idle, vm.uiState.value)
    }

    @Test
    fun `prepareRegister sets WaitingForTap`() = runTest(testDispatcher) {
        vm.prepareRegister("John")
        assertEquals(StationUiState.WaitingForTap, vm.uiState.value)
        assertEquals(StationMode.REGISTER, vm.currentMode)
    }

    @Test
    fun `prepareTopUp sets WaitingForTap and stores amount`() = runTest(testDispatcher) {
        vm.prepareTopUp(50_000)
        assertEquals(StationUiState.WaitingForTap, vm.uiState.value)
        assertEquals(50_000, vm.pendingTopUpAmount)
    }

    @Test
    fun `register succeeds when card is blank`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.failure(Exception("No data"))
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        vm.prepareRegister("Alice")
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is StationUiState.RegisterSuccess)
        assertEquals("Alice", (state as StationUiState.RegisterSuccess).name)
    }

    @Test
    fun `register fails when card already has data`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.success(CardData(memberId = 1, memberName = "Existing"))

        vm.prepareRegister("Alice")
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        assertEquals(StationUiState.Error(StationError.AlreadyRegistered), vm.uiState.value)
    }

    @Test
    fun `topUp succeeds and updates balance`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1, memberName = "Bob", balance = 100_000)
        coEvery { cardReader.read(tag) } returns Result.success(card)
        coEvery { cardReader.write(tag, any()) } returns Result.success(Unit)

        vm.prepareTopUp(50_000)
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as StationUiState.TopUpSuccess
        assertEquals(100_000, state.oldBalance)
        assertEquals(50_000, state.added)
        assertEquals(150_000, state.newBalance)
    }

    @Test
    fun `topUp handles unreadable card`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.failure(Exception("IO error"))

        vm.prepareTopUp(10_000)
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as StationUiState.Error
        assertTrue(state.error is StationError.CardNotRecognized)
    }

    @Test
    fun `reset wipes card successfully`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.success(CardData(memberId = 1))
        coEvery { cardReader.wipe(tag) } returns Result.success(Unit)

        vm.prepareResetTab()
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        assertEquals(StationUiState.ResetSuccess, vm.uiState.value)
    }

    @Test
    fun `reset fails on write error`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.success(CardData(memberId = 1))
        coEvery { cardReader.wipe(tag) } returns Result.failure(Exception("NFC lost"))

        vm.prepareResetTab()
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as StationUiState.Error
        assertTrue(state.error is StationError.WriteFailed)
    }

    @Test
    fun `tag ignored when not WaitingForTap`() = runTest(testDispatcher) {
        vm.uiState.test {
            assertEquals(StationUiState.Idle, awaitItem())
            nfcTagHolder.dispatch(tag)
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `reset returns to Idle`() = runTest(testDispatcher) {
        vm.prepareRegister("X")
        assertEquals(StationUiState.WaitingForTap, vm.uiState.value)
        vm.reset()
        assertEquals(StationUiState.Idle, vm.uiState.value)
    }

    @Test
    fun `retryLastOperation resets to WaitingForTap and logs analytics`() = runTest(testDispatcher) {
        vm.prepareTopUp(10_000)
        vm.retryLastOperation()
        assertEquals(StationUiState.WaitingForTap, vm.uiState.value)
        verify { analyticsHelper.logEvent("nfc_retry", any()) }
    }

    @Test
    fun `write failure during register emits WriteFailed`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.failure(Exception("blank"))
        coEvery { cardReader.write(tag, any()) } returns Result.failure(Exception("Tag lost"))

        vm.prepareRegister("Eve")
        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as StationUiState.Error
        assertTrue(state.error is StationError.WriteFailed)
    }

    @Test
    fun `register with non-numeric memberId emits InvalidMemberId`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.failure(Exception("blank"))

        vm.prepareRegister("Test")
        // Force memberId to non-numeric via reflection to hit the defensive branch
        val field = vm.javaClass.getDeclaredField("memberId")
        field.isAccessible = true
        field.set(vm, "abc")

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as StationUiState.Error
        assertEquals(StationError.InvalidMemberId, state.error)
    }
}
