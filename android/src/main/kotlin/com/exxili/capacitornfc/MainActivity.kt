package com.exxili.capacitornfc

import android.app.PendingIntent
import android.nfc.NfcAdapter
import android.os.Bundle
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    var pendingIntent: PendingIntent? = null

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
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, null, null)
    }
}
