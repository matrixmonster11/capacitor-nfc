package com.exxili.capacitornfc

import android.app.PendingIntent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    private var pendingIntent: PendingIntent? = null
    private val intentFilter: IntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
        try {
            addDataType("text/plain")
        }
        catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("failed", e)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(NFCPlugin::class.java)
        super.onCreate(savedInstanceState)

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this)
    }

    override fun onResume() {
        super.onResume()
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, arrayOf(intentFilter), null)
    }
}
