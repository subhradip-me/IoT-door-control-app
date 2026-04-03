package com.fasla.doorcontrol.features.bluetooth.domain.usecase;

import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;
import com.fasla.doorcontrol.features.bluetooth.domain.repository.BluetoothRepository;

import java.util.List;

/**
 * GetBondedDevicesUseCase — returns the list of devices already paired
 * with the phone via Android's Bluetooth settings.
 *
 * This is the primary way to test without an ESP32 — you can see
 * headphones, other phones, etc. in the list immediately without scanning.
 */
public class GetBondedDevicesUseCase {

    private final BluetoothRepository repository;

    public GetBondedDevicesUseCase(BluetoothRepository repository) {
        this.repository = repository;
    }

    public List<BluetoothDeviceModel> execute() {
        return repository.getBondedDevices();
    }
}
