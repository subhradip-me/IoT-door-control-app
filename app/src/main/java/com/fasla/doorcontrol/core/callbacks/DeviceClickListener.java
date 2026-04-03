package com.fasla.doorcontrol.core.callbacks;

import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;

/**
 * DeviceClickListener — callback for Bluetooth device list item clicks.
 * TODO: Wire into DeviceListAdapter and BluetoothFragment.
 */
public interface DeviceClickListener {

    // TODO: Called when user taps a device in the list
    void onDeviceClicked(BluetoothDeviceModel device);
}
