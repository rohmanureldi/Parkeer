package com.eldirohmanur.parkeer.blackbox

import android.nfc.Tag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eldirohmanur.parkeer.MainActivity
import com.eldirohmanur.parkeer.core.model.Activity
import com.eldirohmanur.parkeer.core.model.CardData
import com.eldirohmanur.parkeer.core.model.TransactionLog
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
class ScoutBlackBoxTest {

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

    private fun navigateToScout() {
        pump()
        composeRule.onNodeWithText("Cek Kartu").performClick()
        pump()
    }

    @Test
    fun bb601_displayCardData() {
        fakeCardReader.readResult = Result.success(
            CardData(
                memberId = 839201,
                memberName = "Budi Santoso",
                balance = 64_000,
                visitState = VisitState.Idle,
                logs = listOf(
                    TransactionLog(6_000, System.currentTimeMillis(), Activity.PARKING),
                    TransactionLog(50_000, System.currentTimeMillis(), Activity.TOP_UP),
                ),
            ),
        )
        navigateToScout()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("PARKEER").assertExists()
        composeRule.onNodeWithText("BUDI SANTOSO").assertExists()
        composeRule.onNodeWithText("Parkir", substring = true).assertExists()
    }

    @Test
    fun bb602_checkedIn_showsPill() {
        fakeCardReader.readResult = Result.success(
            CardData(
                memberId = 1,
                memberName = "Test",
                balance = 30_000,
                visitState = VisitState.CheckedIn(System.currentTimeMillis() - 7_200_000L),
            ),
        )
        navigateToScout()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Sedang Parkir", substring = true).assertExists()
    }

    @Test
    fun bb605_unrecognizedCard() {
        fakeCardReader.readResult = Result.failure(Exception("bad"))
        navigateToScout()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Kartu tidak dapat dibaca", substring = true).assertExists()
    }

    @Test
    fun bb607_noTransactions() {
        fakeCardReader.readResult = Result.success(
            CardData(memberId = 1, memberName = "New", balance = 0, logs = emptyList()),
        )
        navigateToScout()
        nfcTagHolder.dispatch(fakeTag)
        pump()
        composeRule.onNodeWithText("Belum ada transaksi").assertExists()
    }
}
