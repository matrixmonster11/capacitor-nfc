package com.exxili.capacitornfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "NFC")
class NFCPlugin : Plugin() {
    override fun handleOnNewIntent(intent: Intent?) {
        super.handleOnNewIntent(intent)

        if (intent?.action.isNullOrBlank()) {
            return
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            val ndefMessages: MutableList<Map<String, Any>> = mutableListOf()
            intent.getParcelableArrayExtra(null, NfcAdapter.EXTRA_NDEF_MESSAGES.javaClass)?.also { rawMessages ->
                for(message in rawMessages.map { it as NdefMessage }) {
                    val ndefRecords: MutableList<Map<String, Any>> = mutableListOf()
                    for(record in message.records) {
                        ndefRecords.add(mapOf(
                           "type" to "TEXT",
                           "payload" to record.payload
                        ))
                    }

                    ndefMessages.add(mapOf(
                        "records" to ndefRecords
                    ))
                }
            }

            val jsResponse = JSObject()
            jsResponse.put("messages", ndefMessages)
            this.notifyListeners("nfcTag", jsResponse)
        }
    }

    @PluginMethod
    fun startScan(call: PluginCall) {
        print("startScan called")
        call.reject("Android NFC scanning does not require 'startScan' method.")
    }

    @PluginMethod
    fun writeNDEF(call: PluginCall) {
        print("writeNDEF called")
        call.resolve()
    }
}