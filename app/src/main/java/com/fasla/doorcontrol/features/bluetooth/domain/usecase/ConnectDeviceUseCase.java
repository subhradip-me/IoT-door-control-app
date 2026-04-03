package com.fasla.doorcontrol.features.bluetooth.domain.usecase;

import com.fasla.doorcontrol.core.callbacks.BluetoothStateListener;
import com.fasla.doorcontrol.features.bluetooth.domain.repository.BluetoothRepository;

/**
 * ConnectDeviceUseCase — initiates a Classic BT RFCOMM connection
 * to a device identified by its MAC address.
 */
public class ConnectDeviceUseCase {

    private final BluetoothRepository repository;

    public ConnectDeviceUseCase(BluetoothRepository repository) {
        this.repository = repository;
    }

    /**
     * @param address  Device MAC address (e.g., "AA:BB:CC:DD:EE:FF")
     * @param listener Receives onConnected / onDisconnected / onError / onDataReceived
     */
    public void execute(String address, BluetoothStateListener listener) {
        repository.connect(address, listener);
    }
}
