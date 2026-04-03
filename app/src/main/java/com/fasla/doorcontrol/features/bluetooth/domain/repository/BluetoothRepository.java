package com.fasla.doorcontrol.features.bluetooth.domain.repository;

import com.fasla.doorcontrol.core.callbacks.BluetoothStateListener;
import com.fasla.doorcontrol.core.callbacks.ScanResultListener;
import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;

import java.util.List;

/**
 * BluetoothRepository — domain contract for all Bluetooth operations.
 */
public interface BluetoothRepository {

    /**
     * Returns devices already paired with the phone via Android Settings.
     * Available instantly — no scan required. Use for testing without ESP32.
     */
    List<BluetoothDeviceModel> getBondedDevices();

    /** Start Classic BT device discovery. Results arrive via ScanResultListener. */
    void startScan(ScanResultListener listener);

    /** Stop an in-progress scan. */
    void stopScan();

    /**
     * Open an RFCOMM socket to the given device (secure first, insecure fallback).
     * @param address  MAC address (e.g., "AA:BB:CC:DD:EE:FF")
     * @param listener callbacks for connect, disconnect, data, errors
     */
    void connect(String address, BluetoothStateListener listener);

    /** Close the active connection cleanly. */
    void disconnect();

    /**
     * Send a command to the connected ESP32.
     * Use CommandProtocol constants (e.g., CommandProtocol.CMD_OPEN).
     */
    void sendCommand(String command);

    /** Returns true if a socket is currently open and streams are ready. */
    boolean isConnected();
}
