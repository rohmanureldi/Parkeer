package com.eldirohmanur.parkeer.feature.scout

import android.nfc.Tag
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.VisitState
import com.eldirohmanur.parkeer.core.nfc.CardReader
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import io.mockk.coEvery
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
class ScoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val cardReader = mockk<CardReader>()
    private val nfcTagHolder = NfcTagHolder()
    private val analyticsHelper = mockk<AnalyticsHelper>(relaxed = true)
    private val tag = mockk<Tag>()

    private lateinit var vm: ScoutViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vm = ScoutViewModel(cardReader, nfcTagHolder, analyticsHelper)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Ready`() = runTest(testDispatcher) {
        assertEquals(ScoutUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `read succeeds emits Loaded`() = runTest(testDispatcher) {
        val card = CardData(
            memberId = 42,
            memberName = "Scout",
            balance = 75_000,
            visitState = VisitState.Idle,
        )
        coEvery { cardReader.read(tag) } returns Result.success(card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as ScoutUiState.Loaded
        assertEquals(card, state.card)
    }

    @Test
    fun `read failure emits Error`() = runTest(testDispatcher) {
        coEvery { cardReader.read(tag) } returns Result.failure(Exception("Corrupt"))

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        val state = vm.uiState.value as ScoutUiState.Error
        assertEquals("Corrupt", state.reason)
    }

    @Test
    fun `re-read allowed from Loaded state`() = runTest(testDispatcher) {
        val card1 = CardData(memberId = 1, memberName = "A", balance = 10_000)
        val card2 = CardData(memberId = 2, memberName = "B", balance = 20_000)
        coEvery { cardReader.read(tag) } returnsMany listOf(
            Result.success(card1),
            Result.success(card2),
        )

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        assertEquals(card1, (vm.uiState.value as ScoutUiState.Loaded).card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        assertEquals(card2, (vm.uiState.value as ScoutUiState.Loaded).card)
    }

    @Test
    fun `reset returns to Ready`() = runTest(testDispatcher) {
        val card = CardData(memberId = 1)
        coEvery { cardReader.read(tag) } returns Result.success(card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is ScoutUiState.Loaded)

        vm.reset()
        assertEquals(ScoutUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `successful read logs analytics`() = runTest(testDispatcher) {
        val card = CardData(memberId = 7, memberName = "Test")
        coEvery { cardReader.read(tag) } returns Result.success(card)

        nfcTagHolder.dispatch(tag)
        advanceUntilIdle()

        verify { analyticsHelper.logEvent("nfc_read_success", any()) }
    }
}
