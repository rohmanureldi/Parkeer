package com.eldirohmanur.parkeer

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import com.eldirohmanur.parkeer.core.firebase.AnalyticsHelper
import com.eldirohmanur.parkeer.core.firebase.LocalAnalytics
import com.eldirohmanur.parkeer.core.nfc.NfcTagHolder
import com.eldirohmanur.parkeer.navigation.ParkeerNavHost
import com.eldirohmanur.parkeer.security.IntegrityChecker
import com.telkomsel.dexterity.theme.DexterityTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nfcTagHolder: NfcTagHolder

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var integrityChecker: IntegrityChecker

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            when (integrityChecker.check()) {
                is IntegrityChecker.Result.Untrusted -> {
                    Toast.makeText(this@MainActivity, "Device tidak aman", Toast.LENGTH_LONG).show()
                    finish()
                }

                is IntegrityChecker.Result.Error -> {
                    /* best-effort, allow */
                }

                is IntegrityChecker.Result.Trusted -> {
                    /* proceed */
                }
            }
        }
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
                    ParkeerNavHost()
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
