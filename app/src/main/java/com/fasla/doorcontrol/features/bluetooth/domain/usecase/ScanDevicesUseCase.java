package com.fasla.doorcontrol.features.bluetooth.domain.usecase;

import com.fasla.doorcontrol.core.callbacks.ScanResultListener;
import com.fasla.doorcontrol.features.bluetooth.domain.repository.BluetoothRepository;

/**
 * ScanDevicesUseCase — triggers a Classic Bluetooth device discovery.
 *
 * The ViewModel calls execute() and passes a ScanResultListener to receive
 * each discovered device and the scan-finished event.
 */
public class ScanDevicesUseCase {

    private final BluetoothRepository repository;

    public ScanDevicesUseCase(BluetoothRepository repository) {
        this.repository = repository;
    }

    public void execute(ScanResultListener listener) {
        repository.startScan(listener);
    }

    public void stopScan() {
        repository.stopScan();
    }
}
