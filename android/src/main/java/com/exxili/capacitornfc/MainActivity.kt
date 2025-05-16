package com.exxili.capacitornfc

import android.os.Bundle
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        registerPlugin(NFCPlugin::class.java)
        super.onCreate(savedInstanceState)
    }
}
