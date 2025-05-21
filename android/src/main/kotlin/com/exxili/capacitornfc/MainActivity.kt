package com.exxili.capacitornfc

import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(NFCPlugin::class.java)
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        NfcAdapter.getDefaultAdapter(this)?.let {
            if (it.isEnabled) { // Check if NFC is enabled before disabling
                it.disableForegroundDispatch(this)
                Log.d("NFC", "Foreground dispatch disabled.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NfcAdapter.getDefaultAdapter(this)?.let {
            if (it.isEnabled) { // Check if NFC is enabled on the device
                it.enableForegroundDispatch(this, null, null, null)
                Log.d("NFC", "Foreground dispatch enabled.")
            } else {
                Log.w("NFC", "NFC is disabled. Cannot enable foreground dispatch.")
                // Optionally prompt user to enable NFC
            }
        }
    }
}
