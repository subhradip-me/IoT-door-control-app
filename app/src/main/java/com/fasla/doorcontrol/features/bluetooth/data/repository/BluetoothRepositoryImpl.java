package com.fasla.doorcontrol.features.bluetooth.data.repository;

import com.fasla.doorcontrol.core.callbacks.BluetoothStateListener;
import com.fasla.doorcontrol.core.callbacks.ScanResultListener;
import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;
import com.fasla.doorcontrol.features.bluetooth.data.source.BluetoothManager;
import com.fasla.doorcontrol.features.bluetooth.domain.repository.BluetoothRepository;

import java.util.List;

/**
 * BluetoothRepositoryImpl — implements BluetoothRepository by delegating
 * all operations to BluetoothManager (the hardware abstraction layer).
 */
public class BluetoothRepositoryImpl implements BluetoothRepository {

    private final BluetoothManager bluetoothManager;

    public BluetoothRepositoryImpl(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    @Override
    public List<BluetoothDeviceModel> getBondedDevices() {
        return bluetoothManager.getBondedDevices();
    }

    @Override
    public void startScan(ScanResultListener listener) {
        bluetoothManager.startScan(listener);
    }

    @Override
    public void stopScan() {
        bluetoothManager.stopScan();
    }

    @Override
    public void connect(String address, BluetoothStateListener listener) {
        bluetoothManager.connect(address, listener);
    }

    @Override
    public void disconnect() {
        bluetoothManager.disconnect();
    }

    @Override
    public void sendCommand(String command) {
        bluetoothManager.sendCommand(command);
    }

    @Override
    public boolean isConnected() {
        return bluetoothManager.isConnected();
    }
}
