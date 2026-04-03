package com.fasla.doorcontrol.core.callbacks;

/**
 * BluetoothStateListener — callbacks for connection lifecycle and incoming data.
 * Implemented by BluetoothViewModel and passed into BluetoothRepository.
 */
public interface BluetoothStateListener {

    /** Called when the socket is open and streams are ready */
    void onConnected(String deviceAddress);

    /** Called when the connection closes (user disconnect or link loss) */
    void onDisconnected(String reason);

    /** Called on any error (connect fail, send fail, unexpected drop) */
    void onError(String errorMessage);

    /** Called each time a complete newline-terminated message arrives from ESP32 */
    void onDataReceived(String data);
}
