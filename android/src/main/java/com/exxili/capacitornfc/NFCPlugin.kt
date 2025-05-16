package com.exxili.capacitornfc

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "NFCPlugin")
class NFCPlugin : Plugin() {
    var identifier: String = "NFCPlugin"
    var jsName: String = "NFC"

    @PluginMethod
    fun startScan(call: PluginCall) {
        val ret = JSObject()
        ret.put("error", "Android NFC scanning does not require 'startScan' method.")
        call.resolve(ret)
    }

    @PluginMethod
    fun writeNDEF(call: PluginCall) {
        call.resolve()
    }
}