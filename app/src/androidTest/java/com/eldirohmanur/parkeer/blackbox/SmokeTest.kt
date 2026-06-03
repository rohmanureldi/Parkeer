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
class SmokeTest {

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
        // Disable auto-advance to prevent infinite Lottie animations from blocking idle
        composeRule.mainClock.autoAdvance = false
    }

    private fun advanceAndWait() {
        // Advance compose clock in steps to allow coroutines + recomposition to settle
        repeat(20) {
            composeRule.mainClock.advanceTimeBy(100)
        }
    }

    @Test
    fun smokeTest_scoutDispatchShowsCardData() {
        fakeCardReader.readResult = Result.success(
            CardData(
                memberId = 123,
                memberName = "Smoke",
                balance = 99_000,
                visitState = VisitState.Idle,
            ),
        )

        advanceAndWait() // let initial composition happen

        // Navigate to Scout
        composeRule.onNodeWithText("Cek Kartu").performClick()
        advanceAndWait()

        // Verify ready state
        composeRule.onNodeWithText("Tempelkan kartu untuk melihat info").assertExists()

        // Dispatch tag
        nfcTagHolder.dispatch(fakeTag)
        advanceAndWait()

        // Verify card data is shown
        composeRule.onNodeWithText("SMOKE").assertExists()
    }
}
