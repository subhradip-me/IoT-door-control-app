package com.fasla.doorcontrol.core.callbacks;

import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;

/**
 * ScanResultListener — callback interface for Bluetooth device discovery.
 * Implemented by BluetoothViewModel and passed into BluetoothRepository.
 */
public interface ScanResultListener {

    /** Called each time a new device is discovered during scan */
    void onDeviceFound(BluetoothDeviceModel device);

    /** Called when the discovery scan has completed */
    void onScanFinished();

    /** Called if the scan could not be started (e.g., BT disabled, no permission) */
    void onScanError(String error);
}
