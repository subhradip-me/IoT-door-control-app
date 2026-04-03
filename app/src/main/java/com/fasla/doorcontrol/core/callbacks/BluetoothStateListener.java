package com.fasla.doorcontrol.core.callbacks;

/**
 * BluetoothStateListener — callback for Bluetooth connection state changes.
 * TODO: Wire into BluetoothService and BluetoothViewModel.
 */
public interface BluetoothStateListener {

    // TODO: Called when BT connection is established
    void onConnected(String deviceAddress);

    // TODO: Called when BT connection is lost or closed
    void onDisconnected(String deviceAddress);

    // TODO: Called when a connection error occurs
    void onError(String errorMessage);
}
