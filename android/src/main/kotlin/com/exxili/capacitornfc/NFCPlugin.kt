package com.exxili.capacitornfc

import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.io.Serializable

@CapacitorPlugin(name = "NFC")
class NFCPlugin : Plugin() {
    private var intent: Intent? = null
    private var ndefFilter: IntentFilter? = null

    override fun load() {
        super.load()

        intent = Intent(this.activity, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }
    }

    override fun handleOnNewIntent(intent: Intent?) {
        super.handleOnNewIntent(intent)

        if (intent?.action.isNullOrBlank()) {
            return
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            val jsResponse = JSObject()

            val ndefMessages: MutableList<Map<String, Any>> = mutableListOf()
            intent.getParcelableArrayExtra(null, NfcAdapter.EXTRA_NDEF_MESSAGES.javaClass)?.also { rawMessages ->
                for(message in rawMessages.map { it as NdefMessage }) {
                    val ndefRecords: MutableList<Map<String, Serializable?>> = mutableListOf()
                    for(record in message.records) {
                        mapOf(
                            "type" to record.type?.toHexString(),
                            "payload" to record.payload
                        ).let { ndefRecords.add(it) }
                    }

                    ndefMessages.add(mapOf(
                        "records" to ndefRecords
                    ))
                }
            }

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

    private fun ByteArray.toHexString(): String {
        return joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}