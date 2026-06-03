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
class GateBlackBoxTest {

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

    private fun navigateToGate() {
        pump()
        composeRule.onNodeWithText("Portal Masuk").performClick()
        pump()
    }

    @Test
    fun bb301_checkInNormal_showsWelcome() {
        fakeCardReader.readResult = Result.success(
            CardData(
                memberId = 1,
                memberName = "Budi Santoso",
                balance = 50_000,
                visitState = VisitState.Idle,
            ),
        )
        fakeCardReader.writeResult = Result.success(Unit)
        navigateToGate()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Selamat datang, Budi Santoso!", substring = true).assertExists()
    }

    @Test
    fun bb302_alreadyCheckedIn_showsError() {
        fakeCardReader.readResult = Result.success(
            CardData(memberId = 1, visitState = VisitState.CheckedIn(System.currentTimeMillis())),
        )
        navigateToGate()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Kartu ini sudah check-in", substring = true).assertExists()
    }

    @Test
    fun bb303_unrecognizedCard_showsError() {
        fakeCardReader.readResult = Result.failure(Exception("fail"))
        navigateToGate()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Kartu tidak dapat dibaca", substring = true).assertExists()
    }

    @Test
    fun bb304_writeFailed_showsError() {
        fakeCardReader.readResult =
            Result.success(CardData(memberId = 1, visitState = VisitState.Idle))
        fakeCardReader.writeResult = Result.failure(Exception("Tag lost"))
        navigateToGate()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Gagal menyimpan ke kartu", substring = true).assertExists()
    }
}
