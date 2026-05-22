package com.eldirohmanur.parkeer

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import com.eldirohmanur.parkeer.core.ui.ArcBackground
import com.eldirohmanur.parkeer.navigation.ParkeerNavHost
import com.telkomsel.dexterity.theme.DexterityTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nfcTagHolder: NfcTagHolder

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        // FLAG_MUTABLE is required here because the NFC foreground dispatch system
        // needs to fill in EXTRA_TAG and EXTRA_NDEF_MESSAGES into this PendingIntent.
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE,
        )

        setContent {
            CompositionLocalProvider(LocalAnalytics provides analyticsHelper) {
                DexterityTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRect(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF1F40C2), Color(0xFF1F40C2)),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, 0f),
                                    ),
                                )
                            },
                    ) {
                        ArcBackground {
                            ParkeerNavHost()
                        }
                    }
                }
            }
        }

        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        val techLists = arrayOf(arrayOf("android.nfc.tech.MifareUltralight"))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, techLists)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val tag: Tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        } ?: return
        nfcTagHolder.dispatch(tag)
    }
}
