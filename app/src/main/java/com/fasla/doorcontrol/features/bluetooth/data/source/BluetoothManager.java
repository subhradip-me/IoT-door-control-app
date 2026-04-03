package com.fasla.doorcontrol.features.bluetooth.data.source;

import android.content.Context;

/**
 * BluetoothManager — wraps Android's BluetoothAdapter for scanning and connecting.
 * TODO: Implement startScan(), stopScan(), connect(address), disconnect(), sendData(bytes).
 */
public class BluetoothManager {

    private final Context context;

    public BluetoothManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // TODO: public void startScan(ScanCallback callback) { ... }
    // TODO: public void stopScan() { ... }
    // TODO: public void connect(String address) { ... }
    // TODO: public void disconnect() { ... }
    // TODO: public void sendData(byte[] data) { ... }
}
