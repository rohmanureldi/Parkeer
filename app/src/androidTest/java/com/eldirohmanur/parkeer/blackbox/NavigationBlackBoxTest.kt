package com.eldirohmanur.parkeer.blackbox

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eldirohmanur.parkeer.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationBlackBoxTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        composeRule.mainClock.autoAdvance = false
    }

    private fun pump() = repeat(30) { composeRule.mainClock.advanceTimeBy(100) }

    @Test
    fun bb801_roleSelector_displaysFourRoles() {
        pump()
        composeRule.onNodeWithText("Parkeer").assertExists()
        composeRule.onNodeWithText("Admin").assertExists()
        composeRule.onNodeWithText("Portal Masuk").assertExists()
        composeRule.onNodeWithText("Portal Keluar").assertExists()
        composeRule.onNodeWithText("Cek Kartu").assertExists()
    }

    @Test
    fun bb802_navigateToStation() {
        pump()
        composeRule.onNodeWithText("Admin").performClick()
        pump()
        // Default tab is Isi Saldo — chips are visible
        composeRule.onNodeWithText("Rp 50.000").assertExists()
    }

    @Test
    fun bb802_navigateToGate() {
        pump()
        composeRule.onNodeWithText("Portal Masuk").performClick()
        pump()
        composeRule.onNodeWithText("Tempelkan kartu untuk check-in").assertExists()
    }

    @Test
    fun bb802_navigateToTerminal() {
        pump()
        composeRule.onNodeWithText("Portal Keluar").performClick()
        pump()
        composeRule.onNodeWithText("Tempelkan kartu untuk check-out").assertExists()
    }

    @Test
    fun bb802_navigateToScout() {
        pump()
        composeRule.onNodeWithText("Cek Kartu").performClick()
        pump()
        composeRule.onNodeWithText("Tempelkan kartu untuk melihat info").assertExists()
    }
}
