package com.fasla.doorcontrol.features.bluetooth.presentation.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasla.doorcontrol.core.bluetooth.CommandProtocol;
import com.fasla.doorcontrol.core.bluetooth.ConnectionState;
import com.fasla.doorcontrol.core.callbacks.BluetoothStateListener;
import com.fasla.doorcontrol.core.callbacks.ScanResultListener;
import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;
import com.fasla.doorcontrol.features.bluetooth.data.repository.BluetoothRepositoryImpl;
import com.fasla.doorcontrol.features.bluetooth.data.source.BluetoothManager;
import com.fasla.doorcontrol.features.bluetooth.domain.repository.BluetoothRepository;
import com.fasla.doorcontrol.features.bluetooth.domain.usecase.ConnectDeviceUseCase;
import com.fasla.doorcontrol.features.bluetooth.domain.usecase.DisconnectDeviceUseCase;
import com.fasla.doorcontrol.features.bluetooth.domain.usecase.GetBondedDevicesUseCase;
import com.fasla.doorcontrol.features.bluetooth.domain.usecase.ScanDevicesUseCase;
import com.fasla.doorcontrol.features.bluetooth.domain.usecase.SendDataUseCase;

import java.util.ArrayList;
import java.util.List;

/**
 * BluetoothViewModel — single source of truth for all Bluetooth UI state.
 *
 * LiveData streams:
 *   connectionState  — DISCONNECTED / SCANNING / CONNECTING / CONNECTED / ERROR
 *   scannedDevices   — accumulated list of discovered + bonded devices
 *   connectedAddress — MAC of the currently connected device
 *   connectedName    — display name of the currently connected device
 *   lastResponse     — most recent raw string from ESP32
 *   doorState        — parsed door status (OPEN / CLOSED / UNKNOWN)
 *   errorMessage     — user-facing error string
 *
 * Testing without ESP32:
 *   Call loadBondedDevices() on screen open → populates scannedDevices with
 *   all already-paired devices (headphones, laptops, other phones) immediately.
 *   Then startScan() to discover additional nearby devices.
 *   Connection to non-SPP devices will fail with a friendly error message —
 *   the rest of the UI (scan, list, state display) works correctly.
 */
public class BluetoothViewModel extends AndroidViewModel {

    // ── Door state enum ───────────────────────────────────────────────────

    public enum DoorState { OPEN, CLOSED, UNKNOWN }

    // ── LiveData ──────────────────────────────────────────────────────────

    private final MutableLiveData<ConnectionState>            _connectionState  = new MutableLiveData<>(ConnectionState.DISCONNECTED);
    private final MutableLiveData<List<BluetoothDeviceModel>> _scannedDevices   = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String>                     _connectedAddress = new MutableLiveData<>(null);
    private final MutableLiveData<String>                     _connectedName    = new MutableLiveData<>(null);
    private final MutableLiveData<String>                     _lastResponse     = new MutableLiveData<>(null);
    private final MutableLiveData<DoorState>                  _doorState        = new MutableLiveData<>(DoorState.UNKNOWN);
    private final MutableLiveData<String>                     _errorMessage     = new MutableLiveData<>(null);

    public LiveData<ConnectionState>            getConnectionState()  { return _connectionState;  }
    public LiveData<List<BluetoothDeviceModel>> getScannedDevices()   { return _scannedDevices;   }
    public LiveData<String>                     getConnectedAddress() { return _connectedAddress; }
    public LiveData<String>                     getConnectedName()    { return _connectedName;    }
    public LiveData<String>                     getLastResponse()     { return _lastResponse;     }
    public LiveData<DoorState>                  getDoorState()        { return _doorState;        }
    public LiveData<String>                     getErrorMessage()     { return _errorMessage;     }

    // ── Use Cases ─────────────────────────────────────────────────────────

    private final GetBondedDevicesUseCase   getBondedDevicesUseCase;
    private final ScanDevicesUseCase        scanDevicesUseCase;
    private final ConnectDeviceUseCase      connectDeviceUseCase;
    private final DisconnectDeviceUseCase   disconnectDeviceUseCase;
    private final SendDataUseCase           sendDataUseCase;

    // ── Constructor ───────────────────────────────────────────────────────

    public BluetoothViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();

        // Manual DI — swap for injected instances when Hilt is added
        BluetoothRepository repository = new BluetoothRepositoryImpl(
                new BluetoothManager(context)
        );

        getBondedDevicesUseCase  = new GetBondedDevicesUseCase(repository);
        scanDevicesUseCase       = new ScanDevicesUseCase(repository);
        connectDeviceUseCase     = new ConnectDeviceUseCase(repository);
        disconnectDeviceUseCase  = new DisconnectDeviceUseCase(repository);
        sendDataUseCase          = new SendDataUseCase(repository);
    }

    // ── Bonded Devices ────────────────────────────────────────────────────

    /**
     * Loads already-paired devices and populates the device list immediately.
     *
     * Call this in onViewCreated() of BluetoothFragment so the user sees a
     * useful list right away, even before tapping "Scan".
     *
     * Testing tip: any device paired in Android Settings → Bluetooth appears here.
     * This lets you test the full list + connect flow without an ESP32.
     */
    public void loadBondedDevices() {
        List<BluetoothDeviceModel> bonded = getBondedDevicesUseCase.execute();
        _scannedDevices.setValue(bonded);
    }

    // ── Scan ──────────────────────────────────────────────────────────────

    /**
     * Starts discovery. Pre-loads bonded devices, then appends newly discovered
     * ones as the scan runs — giving best of both worlds in one list.
     */
    public void startScan() {
        // Seed the list with already-bonded devices so they appear immediately
        List<BluetoothDeviceModel> bonded = getBondedDevicesUseCase.execute();
        _scannedDevices.setValue(new ArrayList<>(bonded));
        _connectionState.setValue(ConnectionState.SCANNING);
        _errorMessage.setValue(null);

        scanDevicesUseCase.execute(new ScanResultListener() {
            @Override
            public void onDeviceFound(BluetoothDeviceModel device) {
                List<BluetoothDeviceModel> current = _scannedDevices.getValue();
                if (current == null) current = new ArrayList<>();

                // Deduplicate by MAC address
                for (BluetoothDeviceModel existing : current) {
                    if (existing.getAddress().equals(device.getAddress())) return;
                }
                current.add(device);
                _scannedDevices.setValue(current);
            }

            @Override
            public void onScanFinished() {
                if (_connectionState.getValue() == ConnectionState.SCANNING) {
                    _connectionState.setValue(ConnectionState.DISCONNECTED);
                }
            }

            @Override
            public void onScanError(String error) {
                _connectionState.setValue(ConnectionState.ERROR);
                _errorMessage.setValue(error);
            }
        });
    }

    /** Stops an active scan. */
    public void stopScan() {
        scanDevicesUseCase.stopScan();
        if (_connectionState.getValue() == ConnectionState.SCANNING) {
            _connectionState.setValue(ConnectionState.DISCONNECTED);
        }
    }

    // ── Connect ───────────────────────────────────────────────────────────

    /**
     * Connects to a device by MAC address and display name.
     * Stops any in-progress scan first.
     *
     * @param address  MAC address
     * @param name     display name shown in the connected UI
     */
    public void connect(String address, String name) {
        stopScan();
        _connectionState.setValue(ConnectionState.CONNECTING);
        _connectedName.setValue(name);
        _errorMessage.setValue(null);

        connectDeviceUseCase.execute(address, new BluetoothStateListener() {
            @Override
            public void onConnected(String deviceAddress) {
                _connectedAddress.setValue(deviceAddress);
                _connectionState.setValue(ConnectionState.CONNECTED);
            }

            @Override
            public void onDisconnected(String reason) {
                _connectedAddress.setValue(null);
                _connectedName.setValue(null);
                _connectionState.setValue(ConnectionState.DISCONNECTED);
                _doorState.setValue(DoorState.UNKNOWN);
            }

            @Override
            public void onError(String errorMessage) {
                _connectedName.setValue(null);
                _connectionState.setValue(ConnectionState.ERROR);
                _errorMessage.setValue(errorMessage);
            }

            @Override
            public void onDataReceived(String data) {
                _lastResponse.setValue(data);
                parseResponse(data);
            }
        });
    }

    // ── Disconnect ────────────────────────────────────────────────────────

    public void disconnect() {
        disconnectDeviceUseCase.execute();
    }

    // ── Commands ──────────────────────────────────────────────────────────

    public void sendOpen()   { sendDataUseCase.execute(CommandProtocol.CMD_OPEN);   }
    public void sendClose()  { sendDataUseCase.execute(CommandProtocol.CMD_CLOSE);  }
    public void sendStatus() { sendDataUseCase.execute(CommandProtocol.CMD_STATUS); }
    public void sendPing()   { sendDataUseCase.execute(CommandProtocol.CMD_PING);   }

    // ── Response Parsing ──────────────────────────────────────────────────

    /**
     * Parses incoming ESP32 responses and updates door state / error LiveData.
     */
    private void parseResponse(String response) {
        switch (response) {
            case CommandProtocol.RESP_DOOR_OPEN:
                _doorState.setValue(DoorState.OPEN);
                break;
            case CommandProtocol.RESP_DOOR_CLOSED:
                _doorState.setValue(DoorState.CLOSED);
                break;
            case CommandProtocol.RESP_ERROR:
                _errorMessage.setValue("Device reported an error");
                break;
            // RESP_OK, RESP_PONG — acknowledged, no state change needed
            default:
                break;
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────

    @Override
    protected void onCleared() {
        super.onCleared();
        // Ensure connection is closed when the ViewModel is destroyed
        disconnectDeviceUseCase.execute();
    }
}
