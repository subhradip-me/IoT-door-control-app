package com.fasla.doorcontrol.core.bluetooth;

/**
 * ConnectionState — all possible states of the Bluetooth connection lifecycle.
 * Observed by the UI via BluetoothViewModel LiveData.
 */
public enum ConnectionState {

    /** No device connected, not scanning */
    DISCONNECTED,

    /** Actively scanning for nearby devices */
    SCANNING,

    /** Socket connection in progress (blocking connect() call) */
    CONNECTING,

    /** Socket open, streams ready — commands can be sent */
    CONNECTED,

    /** An error occurred (see error message in BluetoothViewModel) */
    ERROR
}
