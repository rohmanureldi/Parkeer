package com.eldirohmanur.parkeer.blackbox

import android.nfc.Tag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eldirohmanur.parkeer.MainActivity
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.VisitState
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import com.eldirohmanur.parkeer.fake.FakeCardReader
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class TerminalBlackBoxTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var fakeCardReader: FakeCardReader

    @Inject
    lateinit var nfcTagHolder: NfcTagHolder

    private val fakeTag: Tag = mockk(relaxed = true) {
        every { id } returns byteArrayOf(0x04, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
    }

    @Before
    fun setup() {
        hiltRule.inject()
        composeRule.mainClock.autoAdvance = false
    }

    private fun pump() = repeat(30) { composeRule.mainClock.advanceTimeBy(100) }

    private fun navigateToTerminal() {
        pump()
        composeRule.onNodeWithText("Portal Keluar").performClick()
        pump()
    }

    @Test
    fun bb501_checkoutSuccess_showsReceipt() {
        fakeCardReader.readResult = Result.success(
            CardData(
                memberId = 1,
                memberName = "Budi Santoso",
                balance = 50_000,
                visitState = VisitState.CheckedIn(System.currentTimeMillis() - 7_200_000L),
            ),
        )
        fakeCardReader.writeResult = Result.success(Unit)
        navigateToTerminal()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Checkout Selesai!", substring = true).assertExists()
        composeRule.onNodeWithText("Budi Santoso").assertExists()
    }

    @Test
    fun bb504_insufficientBalance() {
        fakeCardReader.readResult = Result.success(
            CardData(
                memberId = 1,
                balance = 5_000,
                visitState = VisitState.CheckedIn(System.currentTimeMillis() - 21_600_000L),
            ),
        )
        navigateToTerminal()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Saldo Tidak Cukup").assertExists()
    }

    @Test
    fun bb506_notCheckedIn() {
        fakeCardReader.readResult =
            Result.success(CardData(memberId = 1, visitState = VisitState.Idle))
        navigateToTerminal()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Kartu ini belum check-in", substring = true).assertExists()
    }

    @Test
    fun bb508_writeFailed() {
        fakeCardReader.readResult = Result.success(
            CardData(
                memberId = 1,
                balance = 50_000,
                visitState = VisitState.CheckedIn(System.currentTimeMillis() - 3_600_000L),
            ),
        )
        fakeCardReader.writeResult = Result.failure(Exception("lost"))
        navigateToTerminal()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Gagal menyimpan ke kartu", substring = true).assertExists()
    }

    @Test
    fun bb503_unrecognizedCard() {
        fakeCardReader.readResult = Result.failure(Exception("bad"))
        navigateToTerminal()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Kartu tidak dapat dibaca", substring = true).assertExists()
    }
}
