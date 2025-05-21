package com.exxili.capacitornfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.os.Bundle
import android.util.Log
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techListsArray: Array<Array<String>>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(NFCPlugin::class.java)
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Log.e("NFC", "NFC is not available on this device.")
            // You might want to show a Toast or dialog to the user here
            return
        }

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        techListsArray = arrayOf(
            arrayOf(Ndef::class.java.name), // NDEF formatted tags
            arrayOf(NfcA::class.java.name), // NFC-A (most common)
            arrayOf(NfcB::class.java.name),
            arrayOf(NfcF::class.java.name),
            arrayOf(NfcV::class.java.name)
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.let {
            if (it.isEnabled) {
                it.disableForegroundDispatch(this)
                Log.d("NFC", "Foreground dispatch disabled.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let {
            if (it.isEnabled && pendingIntent != null) {
                it.enableForegroundDispatch(this, pendingIntent, null, techListsArray)
                Log.d("NFC", "Foreground dispatch enabled.")
            } else {
                Log.w("NFC", "NFC is disabled or PendingIntent not ready. Cannot enable foreground dispatch.")
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val nfcPlugin = bridge.getPlugin("NFC")?.instance as? NFCPlugin
        if (nfcPlugin != null) {
            nfcPlugin.newIntent(intent)
        } else {
            Log.e("NFC", "NFCPlugin instance not found or not of correct type.")
        }
    }
}
