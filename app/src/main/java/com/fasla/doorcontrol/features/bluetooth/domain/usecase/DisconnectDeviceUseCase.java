package com.fasla.doorcontrol.features.bluetooth.domain.usecase;

import com.fasla.doorcontrol.features.bluetooth.domain.repository.BluetoothRepository;

/**
 * DisconnectDeviceUseCase — cleanly closes the active Bluetooth connection
 * and releases underlying socket resources.
 */
public class DisconnectDeviceUseCase {

    private final BluetoothRepository repository;

    public DisconnectDeviceUseCase(BluetoothRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.disconnect();
    }
}
