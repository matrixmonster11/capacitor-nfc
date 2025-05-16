package com.exxili.capacitornfc;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity  extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(NFCPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
