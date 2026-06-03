package com.eldirohmanur.parkeer.blackbox

import android.nfc.Tag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.eldirohmanur.parkeer.MainActivity
import com.eldirohmanur.parkeer.core.model.CardData
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

/**
 * Station black-box tests.
 * Note: bb101 (register success), bb201 (topup success), bb702 (reset unrecognized)
 * require manual verification due to Lottie animation blocking compose idle in sheets.
 */
@HiltAndroidTest
class StationBlackBoxTest {

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
        fakeCardReader.reset()
        composeRule.mainClock.autoAdvance = false
    }

    private fun pump() = repeat(50) { composeRule.mainClock.advanceTimeBy(100) }

    private fun dispatch() {
        nfcTagHolder.dispatch(fakeTag)
        Thread.sleep(1000)
        pump()
    }

    private fun navigateToStation() {
        pump()
        composeRule.onNodeWithText("Admin").performClick()
        pump()
    }

    // BB-202: Top-up exceeds max (error path — no Lottie sheet)
    @Test
    fun bb202_topUpExceedsMax() {
        fakeCardReader.readResult =
            Result.success(CardData(memberId = 1, memberName = "Max", balance = 960_000))
        navigateToStation()
        Thread.sleep(300)
        composeRule.onNodeWithText("Rp 50.000").performClick()
        Thread.sleep(500)
        pump()
        dispatch()
        composeRule.onNodeWithText("Saldo akan melebihi", substring = true).assertExists()
    }

    // BB-102: Reject already registered (error path — no Lottie)
    @Test
    fun bb102_rejectRegisteredCard() {
        fakeCardReader.readResult = Result.success(CardData(memberId = 1, memberName = "Existing"))
        navigateToStation()
        composeRule.onNodeWithText("Registrasi").performClick()
        Thread.sleep(500)
        pump()
        composeRule.onNodeWithText("Masukkan nama anggota").performTextInput("New")
        pump()
        composeRule.onNodeWithText("Daftarkan").performClick()
        Thread.sleep(500)
        pump()
        dispatch()
        composeRule.onNodeWithText("Kartu ini sudah terdaftar", substring = true).assertExists()
    }

    // BB-701: Reset success (inline state — no bottom sheet Lottie)
    @Test
    fun bb701_resetSuccess() {
        fakeCardReader.readResult = Result.success(CardData(memberId = 1, memberName = "X"))
        fakeCardReader.wipeResult = Result.success(Unit)
        navigateToStation()
        composeRule.onNodeWithText("Reset").performClick()
        Thread.sleep(500)
        pump()
        dispatch()
        composeRule.onNodeWithText("Kartu berhasil direset").assertExists()
    }
}
