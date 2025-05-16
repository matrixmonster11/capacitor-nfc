package com.exxili.capacitornfc;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ScreenOrientation")
public class NFCPlugin extends Plugin {
    public String identifier = "NFCPlugin";
    public String jsName = "NFC";

    @PluginMethod()
    public void startScan(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("error", "Android NFC scanning does not require 'startScan' method.");
        call.resolve(ret);
    }

    @PluginMethod()
    public void writeNDEF(PluginCall call) {
        call.resolve();
    }
}