package com.exxili.capacitornfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.ACTION_NDEF_DISCOVERED
import android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED
import android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED
import android.nfc.NfcAdapter.EXTRA_NDEF_MESSAGES
import android.nfc.NfcAdapter.getDefaultAdapter
import android.nfc.Tag
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
import android.util.Log
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Base64
import kotlinx.coroutines.*

@CapacitorPlugin(name = "NFC")
class NFCPlugin : Plugin() {
    private var writeMode = false
    private var recordsBuffer: JSArray? = null
    private var pendingTag: Tag? = null
    private val pluginScope = CoroutineScope(Dispatchers.IO + SupervisorJob()) // Create a scope for coroutines
    private val TAG_NFC_PLUGIN = "NFCPluginDetailed" // Specific TAG for these logs

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
    
    // Call this method to set the tag, e.g., from onNewIntent
    fun setPendingTag(tag: Tag?) {
        this.pendingTag = tag
        Log.d(TAG_NFC_PLUGIN, "setPendingTag: New tag set: ${tag?.id?.toHexString()}")
    }

    @OptIn(kotlin.ExperimentalStdlibApi::class)
    @PluginMethod
    fun lockTag(call: PluginCall) {
        Log.d("NFC", "lockTag() called")

        val tagFromPending = pendingTag // Capture for use in coroutine
        if (tagFromPending == null) {
            Log.e("NFC", "No pending tag available")
            call.reject("Hold tag steady near the device and try again")
            return
        }

        pluginScope.launch { // Launch a coroutine for background work
        Log.d(TAG_NFC_PLUGIN, "lockTag: Coroutine launched on ${Thread.currentThread().name}")
            try {
                Log.d(TAG_NFC_PLUGIN, "lockTag: Attempting to get Ndef for tag.")
                val ndef = Ndef.get(tagFromPending)
                Log.d(TAG_NFC_PLUGIN, "lockTag: Ndef.get(tag) result: ${if (ndef == null) "null" else "Ndef object obtained"}")

                Log.d(TAG_NFC_PLUGIN, "lockTag: Attempting to get NfcA for tag.")
                val nfcA = NfcA.get(tagFromPending)
                Log.d(TAG_NFC_PLUGIN, "lockTag: NfcA.get(tag) result: ${if (nfcA == null) "null" else "NfcA object obtained"}")

                if (ndef == null && nfcA == null) {
                    Log.e(TAG_NFC_PLUGIN, "lockTag: Tag does not support NDEF or NfcA technology.")
                    call.reject("Tag does not support NDEF or NfcA technology.")
                    return@launch
                }

                var ndefLockSuccess = false

                // Try the generic makeReadOnly() first
                if (ndef != null) {
                    Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF branch started.")
                    var ndefConnected = false
                    try {
                        Log.d(TAG_NFC_PLUGIN, "Attempting generic Ndef.makeReadOnly()...")
                        // Timeout for the connect and makeReadOnly operations
                        val makeReadOnlyResult: Boolean? = withTimeoutOrNull(5000) { // 5-second timeout
                            Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: Inside withTimeoutOrNull. Current thread: ${Thread.currentThread().name}")
                            
                            Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: Calling ndef.connect().")
                            ndef.connect()
                            ndefConnected = true // Mark as connected
                            Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: ndef.connect() successful. isConnected: ${ndef.isConnected}")
                            
                            Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: Checking ndef.isWritable.")
                            val isCurrentlyWritable = ndef.isWritable
                            Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: ndef.isWritable result: $isCurrentlyWritable")
                            if (isCurrentlyWritable) {
                                Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: Tag is writable, attempting ndef.makeReadOnly().")
                                val makeReadOnlyOpResult = ndef.makeReadOnly()
                                Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: ndef.makeReadOnly() operation result: $makeReadOnlyOpResult")
                                makeReadOnlyOpResult // Return the result of makeReadOnly
                            } else {
                                Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: Tag was already read-only.")
                                // Already read-only, consider it a success for locking purposes
                                true 
                            }
                        }
                        Log.d(TAG_NFC_PLUGIN, "lockTag: NDEF: After withTimeoutOrNull. makeReadOnlyResult: $makeReadOnlyResult")

                        if (makeReadOnlyResult == true) {
                            if (ndef.isWritable) { // Check again AFTER the operation (if it didn't throw)
                                // This case should ideally not happen if makeReadOnly was true and it was writable before
                                // but as a safeguard, if makeReadOnly returned true but it's still writable,
                                // it implies something went wrong or the tag didn't actually lock.
                                // However, Ndef.makeReadOnly() returning true means it *believes* it locked.
                                // For simplicity, we'll trust the true result.
                                Log.d("NFC", "Generic makeReadOnly() successful.")
                                call.resolve(JSObject().apply {
                                    put("locked", true)
                                    put("message", "Tag locked successfully via generic NDEF command")
                                })
                                ndefLockSuccess = true
                            } else {
                                Log.d("NFC", "Tag was already read-only or successfully made read-only.")
                                call.resolve(JSObject().apply {
                                    put("locked", true)
                                    put("message", "Tag was already read-only or successfully made read-only")
                                })
                                ndefLockSuccess = true
                            }
                        } else if (makeReadOnlyResult == false) {
                            // makeReadOnly() returned false, meaning it couldn't be locked (but didn't timeout)
                            Log.w("NFC", "Generic makeReadOnly() returned false. Tag could not be locked via NDEF.")
                            // We will let it fall through to NTAG attempt
                        } else {
                            // Timeout occurred
                            Log.e("NFC", "Generic makeReadOnly() timed out.")
                            // We will let it fall through to NTAG attempt, or you can reject here:
                            // call.reject("Failed to lock tag: NDEF operation timed out.")
                            // return@launch
                        }
                    } catch (e: TimeoutCancellationException) {
                        Log.e("NFC", "Generic makeReadOnly() operation explicitly timed out: ${e.message}")
                        // Fall through to NTAG attempt or reject
                    } catch (e: Exception) {
                        Log.e("NFC", "Generic makeReadOnly() failed: ${e.message}")
                        // Fall through to NTAG attempt
                    } finally {
                        try {
                            if (ndefConnected && ndef.isConnected) ndef.close()
                        } catch (e: Exception) { /* Ignore */ }
                    }
                }

                if (ndefLockSuccess) {
                    return@launch // Successfully locked with NDEF, so exit
                }

                // If generic method fails or was skipped, try specific NTAG locking commands
                if (nfcA != null) {
                    var nfcAConnected = false
                    try {
                        Log.d("NFC", "Attempting specific NTAG locking...")
                        
                        val ntagLockResult: ByteArray? = withTimeoutOrNull(5000) { // 5-second timeout
                            nfcA.connect()
                            nfcAConnected = true
                            // NTAG locking command
                            val lockCmd = byteArrayOf(0xA2.toByte(), 0x02.toByte(), 0x00.toByte(), 0x00.toByte(), 0xFF.toByte(), 0xFF.toByte())
                            nfcA.transceive(lockCmd)
                        }

                        if (ntagLockResult != null) {
                            Log.d("NFC", "NTAG lock command response: ${ntagLockResult.toHexString()}")
                            // Check for successful response
                            if (ntagLockResult.isNotEmpty() && ntagLockResult[0] == 0x0A.toByte()) { // Common success ACK for NTAGs
                                Log.d("NFC", "NTAG locking successful.")
                                call.resolve(JSObject().apply {
                                    put("locked", true)
                                    put("message", "Tag locked successfully via NTAG-specific command")
                                })
                            } else {
                                call.reject("Failed to lock NTAG. Command response was: ${ntagLockResult.toHexString()}")
                            }
                        } else {
                            Log.e("NFC", "NTAG locking operation timed out.")
                            call.reject("Failed to lock tag: NTAG operation timed out.")
                        }
                    } catch (e: TimeoutCancellationException) {
                        Log.e("NFC", "NTAG locking operation explicitly timed out: ${e.message}")
                        call.reject("Failed to lock tag: NTAG operation timed out. ${e.message}")
                    } catch (e: IOException) {
                        Log.e("NFC", "I/O error during NTAG locking: ${e.message}")
                        call.reject("I/O error during NTAG locking. Hold tag steady. ${e.message}")
                    } catch (e: Exception) {
                        Log.e("NFC", "NTAG locking failed with exception: ${e.message}")
                        call.reject("Failed to lock tag with NTAG command: ${e.message}")
                    } finally {
                        try {
                            if (nfcAConnected && nfcA.isConnected) nfcA.close()
                        } catch (e: Exception) { /* Ignore */ }
                    }
                } else if (!ndefLockSuccess) { 
                    // This else block is reached if NDEF attempt was made and failed (or timed out) 
                    // AND nfcA is null (so no NTAG attempt was possible)
                    call.reject("Failed to lock tag. NDEF operation failed/timed out, and NTAG not supported or also failed.")
                }

            } catch (e: Exception) {
                Log.e("NFC", "Top-level locking error in coroutine: ${e.message}")
                call.reject("Locking failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // Dummy toHexString for ByteArray if you don't have one
    // Add this to your class or as an extension function
    @OptIn(kotlin.ExperimentalStdlibApi::class)
    fun ByteArray.toHexString(): String {
        return this.joinToString("") { String.format("%02X", it) }
    }

    // Call this when your plugin is destroyed to cancel ongoing coroutines
    override fun handleOnDestroy() {
        super.handleOnDestroy()
        pluginScope.cancel()
    }

    public override fun handleOnNewIntent(intent: Intent?) {
        super.handleOnNewIntent(intent)

        if (intent == null || intent.action.isNullOrBlank()) {
            return
        }

        if (writeMode) {
            Log.d("NFC", "WRITE MODE START")
            handleWriteTag(intent)
            writeMode = false
            recordsBuffer = null
        }
        else if (ACTION_NDEF_DISCOVERED == intent.action || ACTION_TAG_DISCOVERED == intent.action) {
            Log.d("NFC", "READ MODE START")
             handleReadTag(intent)
        }
    }

    @PluginMethod
    fun isSupported(call: PluginCall) {
        val adapter = NfcAdapter.getDefaultAdapter(this.activity)
        val ret = JSObject()
        ret.put("supported", adapter != null)
        call.resolve(ret)
    }

    @PluginMethod
    fun cancelWriteAndroid(call: PluginCall) {
        this.writeMode = false
        call.resolve()
    }

    @PluginMethod
    fun startScan(call: PluginCall) {
        print("startScan called")
        call.reject("Android NFC scanning does not require 'startScan' method.")
    }

    @PluginMethod
    fun writeNDEF(call: PluginCall) {
        print("writeNDEF called")

        writeMode = true
        recordsBuffer = call.getArray("records")

        call.resolve()
    }

    override fun handleOnPause() {
        super.handleOnPause()
        getDefaultAdapter(this.activity)?.disableForegroundDispatch(this.activity)
    }

    override fun handleOnResume() {
        super.handleOnResume()
        if(getDefaultAdapter(this.activity) == null) return;

        val intent = Intent(context, this.activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent =
            PendingIntent.getActivity(this.activity, 0, intent, PendingIntent.FLAG_MUTABLE)

        val intentFilter: Array<IntentFilter> =
            arrayOf(
                IntentFilter(ACTION_NDEF_DISCOVERED).apply {
                    try {
                        addDataType("text/plain")
                    } catch (e: IntentFilter.MalformedMimeTypeException) {
                        throw RuntimeException("failed", e)
                    }
                },
                IntentFilter(ACTION_TECH_DISCOVERED),
                IntentFilter(ACTION_TAG_DISCOVERED)
            )

        getDefaultAdapter(this.activity).enableForegroundDispatch(
            this.activity,
            pendingIntent,
            intentFilter,
            techListsArray
        )
    }

    private fun handleWriteTag(intent: Intent) {
        val records = recordsBuffer?.toList<JSONObject>()
        if(records != null) {
            val ndefRecords = mutableListOf<NdefRecord>()

            try {
                for (record in records) {
                    val payload = record.getJSONArray("payload")
                    val type: String? = record.getString("type")

                    if (payload.length() == 0 || type == null) {
                        notifyListeners(
                            "nfcError",
                            JSObject().put(
                                "error",
                                "Invalid record: payload or type is missing."
                            )
                        )
                        return
                    }

                    val typeBytes = type.toByteArray(Charsets.UTF_8)
                    val payloadBytes = ByteArray(payload.length())
                    for(i in 0 until payload.length()) {
                        payloadBytes[i] = payload.getInt(i).toByte()
                    }

                    ndefRecords.add(
                        NdefRecord(
                            NdefRecord.TNF_WELL_KNOWN,
                            typeBytes,
                            ByteArray(0),
                            payloadBytes
                        )
                    )
                }

                val ndefMessage = NdefMessage(ndefRecords.toTypedArray())
                val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                var ndef = Ndef.get(tag)

                if (ndef == null) {
                    val formatable = NdefFormatable.get(tag)
                    if (formatable != null) {
                        try {
                            formatable.connect()
                            val mimeRecord = NdefRecord.createMime("text/plain", "INIT".toByteArray(
                                Charset.forName("US-ASCII")))
                            val msg = NdefMessage(mimeRecord)
                            formatable.format(msg)
                            // Success!
                            // Emit event to Capacitor plugin for success
                            println("Successfully formatted and wrote NDEF message to tag!")
                        } catch (e: IOException) {
                            // Error connecting or formatting
                            // Emit event to Capacitor plugin for error
                            println("Error formatting or writing to NDEF-formatable tag: ${e.message}")
                        } catch (e: Exception) { // Catch other potential exceptions during format, like TagLostException
                            println("Error during NDEF formatting: ${e.message}")
                        } finally {
                            try {
                                formatable.close()
                            } catch (e: IOException) {
                                println("Error closing NdefFormatable connection: ${e.message}")
                            }
                        }

                        ndef = Ndef.get(formatable.tag)
                    } else {
                        notifyListeners(
                            "nfcError",
                            JSObject().put(
                                "error",
                                "Tag does not support NDEF writing."
                            )
                        )
                        return
                    }
                }

                ndef.use { // Use block ensures ndef.close() is called
                    ndef.connect()
                    if (!ndef.isWritable) {
                        notifyListeners(
                            "nfcError",
                            JSObject().put(
                                "error",
                                "NFC tag is not writable"
                            )
                        )
                        return
                    }
                    if (ndef.maxSize < ndefMessage.toByteArray().size) {
                        notifyListeners(
                            "nfcError",
                            JSObject().put(
                                "error",
                                "Message too large for this NFC Tag (max ${ndef.maxSize} bytes)."
                            )
                        )
                        return
                    }

                    ndef.writeNdefMessage(ndefMessage)
                    Log.d("NFC", "NDEF message successfully written to tag.")
                }

                notifyListeners("nfcWriteSuccess", JSObject().put("success", true))
            }
            catch (e: UnsupportedEncodingException) {
                Log.e("NFC", "Encoding error during NDEF record creation: ${e.message}")
                notifyListeners(
                    "nfcError",
                    JSObject().put(
                        "error",
                        "Encoding error: ${e.message}"
                    )
                )
            }
            catch (e: IOException) {
                Log.e("NFC", "I/O error during NFC write: ${e.message}")
                notifyListeners(
                    "nfcError",
                    JSObject().put(
                        "error",
                        "NFC I/O error: ${e.message}"
                    )
                )
            }
            catch (e: Exception) {
                Log.e("NFC", "Error writing NDEF message: ${e.message}", e)
                notifyListeners(
                    "nfcError",
                    JSObject().put(
                        "error",
                        "Failed to write NDEF message: ${e.message}"
                    )
                )
            }
        }
        else {
            notifyListeners("nfcError", JSObject().put("error", "Failed to write NFC tag"))
        }
    }

    private fun handleReadTag(intent: Intent) {
        // Save the latest tag for future operations like lockTag()
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            pendingTag = tag
        }

        val jsResponse = JSObject()

        val ndefMessages = JSArray()

        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val receivedMessages = intent.getParcelableArrayExtra(
                    EXTRA_NDEF_MESSAGES,
                    NdefMessage::class.java
                )

                receivedMessages?.also { rawMessages ->
                    for (message in rawMessages) {
                        val ndefRecords = JSArray()
                        for (record in message.records) {
                            val rec = JSObject()
                            rec.put("type", String(record.type, Charsets.UTF_8))
                            rec.put("payload", Base64.getEncoder().encodeToString(record.payload))
                            ndefRecords.put(rec)
                        }

                        val msg = JSObject()
                        msg.put("records", ndefRecords)
                        ndefMessages.put(msg)
                    }
                }
            }

            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                val result = if (tagId != null) byteArrayToHexString(tagId) else ""

                val rec = JSObject()
                rec.put("type", "ID")
                rec.put("payload", Base64.getEncoder().encodeToString(result.toByteArray()))

                val ndefRecords = JSArray()
                ndefRecords.put(rec)

                val msg = JSObject()
                msg.put("records", ndefRecords)
                ndefMessages.put(msg)
            }
        }

        jsResponse.put("messages", ndefMessages)
        this.notifyListeners("nfcTag", jsResponse)
    }

    private fun byteArrayToHexString(inarray: ByteArray): String {
        val hex = arrayOf("0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F")
        var out = ""

        for (j in inarray.size - 1 downTo 0) {
            val `in` = inarray[j].toInt() and 0xff
            val i1 = (`in` shr 4) and 0x0f
            out += hex[i1]
            val i2 = `in` and 0x0f
            out += hex[i2]
        }
        return out
    }
}