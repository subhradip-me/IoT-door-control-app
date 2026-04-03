package com.fasla.doorcontrol.core.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * BluetoothUtils — low-level Bluetooth adapter checks.
 */
public final class BluetoothUtils {

    private BluetoothUtils() { /* no instances */ }

    /**
     * Returns true if this device has a Bluetooth adapter.
     */
    public static boolean isBluetoothSupported() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    /**
     * Returns true if Bluetooth is currently enabled.
     */
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Returns true if the device hardware supports Classic Bluetooth.
     */
    public static boolean hasClassicBluetooth(Context context) {
        return context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }
}
