package com.exxili.capacitornfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter.*
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcBarcode
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import com.getcapacitor.JSObject
import com.getcapacitor.Logger
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.io.Serializable

@CapacitorPlugin(name = "NFC")
class NFCPlugin : Plugin() {
    private val techListsArray = arrayOf(arrayOf<String>(
        IsoDep::class.java.name,
        MifareClassic::class.java.name,
        MifareUltralight::class.java.name,
        Ndef::class.java.name,
        NdefFormatable::class.java.name,
        NfcBarcode::class.java.name,
        NfcA::class.java.name,
        NfcB::class.java.name,
        NfcF::class.java.name,
        NfcV::class.java.name
    ))

    public override fun handleOnNewIntent(intent: Intent?) {
        Logger.info("HANDLING INTENT INTERNAL")

        super.handleOnNewIntent(intent)

        if (intent?.action.isNullOrBlank()) {
            return
        }

        if (ACTION_NDEF_DISCOVERED == intent?.action) {
            val jsResponse = JSObject()

            val ndefMessages: MutableList<Map<String, Any>> = mutableListOf()
            intent.getParcelableArrayExtra(null, EXTRA_NDEF_MESSAGES.javaClass)?.also { rawMessages ->
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

    override fun handleOnPause() {
        super.handleOnPause()
        getDefaultAdapter(this.activity).disableForegroundDispatch(this.activity)
    }

    override fun handleOnResume() {
        super.handleOnResume()

        val intent = Intent(context, this.activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(this.activity, 0, intent, PendingIntent.FLAG_MUTABLE)

        val intentFilter: Array<IntentFilter> = arrayOf(IntentFilter(ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("text/plain")
            }
            catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("failed", e)
            }
        })

        getDefaultAdapter(this.activity).enableForegroundDispatch(this.activity, pendingIntent, intentFilter, techListsArray)
    }

    private fun ByteArray.toHexString(): String {
        return joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}