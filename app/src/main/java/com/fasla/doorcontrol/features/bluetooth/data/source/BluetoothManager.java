package com.fasla.doorcontrol.features.bluetooth.data.source;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fasla.doorcontrol.core.callbacks.BluetoothStateListener;
import com.fasla.doorcontrol.core.callbacks.ScanResultListener;
import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * BluetoothManager — wraps Android's Classic Bluetooth (SPP) API.
 *
 * Key design decisions:
 * ─────────────────────
 * 1. BONDED DEVICES FIRST
 *    getBondedDevices() returns already-paired devices immediately (no scan needed).
 *    This lets you test connectivity with headphones, phones, or any paired device
 *    without needing the real ESP32 hardware.
 *
 * 2. SECURE + INSECURE SOCKET FALLBACK
 *    connect() tries a secure RFCOMM socket first. If the device refuses (common with
 *    non-SPP devices or certain ESP32 firmware configs), it falls back to an insecure
 *    socket. This maximises compatibility during development and testing.
 *
 * 3. NEWLINE-AWARE RECEIVE LOOP
 *    The read loop accumulates raw bytes into a StringBuilder and emits only complete
 *    '\n'-terminated messages. This correctly handles TCP-style fragmentation where
 *    a single read() may return a partial message.
 *
 * 4. ALL CALLBACKS ON MAIN THREAD
 *    Every callback to ScanResultListener / BluetoothStateListener is posted via
 *    mainHandler so UI observers never need runOnUiThread().
 */
public class BluetoothManager {

    private static final String TAG = "BT-Manager";

    /**
     * Standard SPP UUID — matches what ESP32's SerialBT uses by default.
     * If your ESP32 sketch sets a custom UUID, change this constant to match.
     */
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // ── State ─────────────────────────────────────────────────────────────

    private final Context         context;
    private final BluetoothAdapter adapter;
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    private BluetoothSocket  socket;
    private InputStream      inputStream;
    private OutputStream     outputStream;
    private Thread           connectThread;
    private Thread           receiveThread;

    private volatile boolean isConnected = false;

    private ScanResultListener     scanListener;
    private BluetoothStateListener stateListener;

    // ── Discovery Receiver ────────────────────────────────────────────────

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null || scanListener == null) return;

                String  name    = device.getName() != null ? device.getName() : "Unknown Device";
                boolean bonded  = device.getBondState() == BluetoothDevice.BOND_BONDED;

                BluetoothDeviceModel model =
                        new BluetoothDeviceModel(name, device.getAddress(), bonded);

                mainHandler.post(() -> scanListener.onDeviceFound(model));

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mainHandler.post(() -> {
                    if (scanListener != null) scanListener.onScanFinished();
                });
            }
        }
    };

    // ── Constructor ───────────────────────────────────────────────────────

    public BluetoothManager(Context context) {
        this.context = context.getApplicationContext();
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    // ── Bonded Devices ────────────────────────────────────────────────────

    /**
     * Returns all devices currently paired (bonded) with the phone.
     *
     * Testing without ESP32:
     *   Call this to instantly populate the device list with any device
     *   already paired in Android Settings → Bluetooth (headphones, laptops,
     *   other phones).  Connection attempts to non-SPP devices will fail gracefully
     *   with a clear error message — the scan + list UI still works perfectly.
     */
    public List<BluetoothDeviceModel> getBondedDevices() {
        List<BluetoothDeviceModel> result = new ArrayList<>();
        if (adapter == null) return result;

        Set<BluetoothDevice> bonded = adapter.getBondedDevices();
        for (BluetoothDevice device : bonded) {
            String name = device.getName() != null ? device.getName() : "Unknown Device";
            result.add(new BluetoothDeviceModel(name, device.getAddress(), true));
        }
        return result;
    }

    // ── Scan ──────────────────────────────────────────────────────────────

    /**
     * Starts Classic Bluetooth device discovery (~12 seconds).
     * Also discover devices not yet paired. Results come via ScanResultListener.
     *
     * Note: Requires ACCESS_FINE_LOCATION on API ≤ 28, BLUETOOTH_SCAN on API 31+.
     */
    public void startScan(ScanResultListener listener) {
        if (adapter == null || !adapter.isEnabled()) {
            if (listener != null) listener.onScanError("Bluetooth is not enabled");
            return;
        }

        this.scanListener = listener;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(discoveryReceiver, filter);

        if (adapter.isDiscovering()) adapter.cancelDiscovery();
        adapter.startDiscovery();
    }

    /** Stops discovery and unregisters the BroadcastReceiver. */
    public void stopScan() {
        try {
            context.unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException ignored) {
            // Receiver not registered — safe to ignore
        }
        if (adapter != null && adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        scanListener = null;
    }

    // ── Connect ───────────────────────────────────────────────────────────

    /**
     * Opens an RFCOMM socket to the device at the given MAC address.
     *
     * Connection strategy (two-step fallback):
     *   Step 1 — Secure RFCOMM socket (standard, required for ESP32 SPP)
     *   Step 2 — Insecure RFCOMM socket (fallback for devices that reject
     *             encrypted pairing, useful for testing with other phones/tablets)
     *
     * Runs on a background thread. All results posted to main thread.
     *
     * @param address  MAC address ("AA:BB:CC:DD:EE:FF")
     * @param listener receives onConnected / onDisconnected / onError / onDataReceived
     */
    public void connect(String address, BluetoothStateListener listener) {
        this.stateListener = listener;

        // Discovery interferes with connect — cancel it first
        if (adapter.isDiscovering()) adapter.cancelDiscovery();

        connectThread = new Thread(() -> {
            try {
                BluetoothDevice device = adapter.getRemoteDevice(address);
                socket = tryConnect(device);

                inputStream  = socket.getInputStream();
                outputStream = socket.getOutputStream();
                isConnected  = true;

                Log.d(TAG, "Connected to " + address);
                mainHandler.post(() -> {
                    if (stateListener != null) stateListener.onConnected(address);
                });

                startReceiveLoop();

            } catch (IOException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                isConnected = false;
                closeSocket();

                // Friendly message for non-SPP devices (headphones etc.)
                String msg = buildConnectionErrorMessage(e.getMessage());
                mainHandler.post(() -> {
                    if (stateListener != null) stateListener.onError(msg);
                });
            }
        }, "bt-connect-thread");

        connectThread.start();
    }

    /**
     * Attempts a secure connection first; falls back to insecure if it fails.
     * Using insecure socket is perfectly safe on a trusted local network —
     * it just skips the encryption handshake.
     */
    private BluetoothSocket tryConnect(BluetoothDevice device) throws IOException {
        // Step 1 — Secure RFCOMM (standard for ESP32 SPP)
        try {
            BluetoothSocket secureSocket =
                    device.createRfcommSocketToServiceRecord(SPP_UUID);
            secureSocket.connect();
            Log.d(TAG, "Secure RFCOMM connected");
            return secureSocket;
        } catch (IOException secureEx) {
            Log.w(TAG, "Secure RFCOMM failed, trying insecure: " + secureEx.getMessage());
        }

        // Step 2 — Insecure RFCOMM (fallback for non-ESP32 testing)
        BluetoothSocket insecureSocket =
                device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
        insecureSocket.connect();
        Log.d(TAG, "Insecure RFCOMM connected");
        return insecureSocket;
    }

    /** Returns a user-friendly error message based on the raw IOException message. */
    private String buildConnectionErrorMessage(String rawMessage) {
        if (rawMessage == null) return "Connection failed";
        if (rawMessage.contains("refused"))
            return "Device refused connection — it may not support SPP (e.g., headphones use A2DP, not SPP). Try an SPP-capable device.";
        if (rawMessage.contains("timeout") || rawMessage.contains("timed out"))
            return "Connection timed out — device may be out of range or busy";
        if (rawMessage.contains("Host is down"))
            return "Device is off or out of range";
        return "Connection failed: " + rawMessage;
    }

    // ── Send ──────────────────────────────────────────────────────────────

    /**
     * Sends a command string to the connected ESP32 over the RFCOMM socket.
     * Commands must end with '\n' (use CommandProtocol constants).
     * Runs on a short-lived background thread — never blocks the caller.
     */
    public void sendCommand(String command) {
        if (!isConnected || outputStream == null) {
            Log.w(TAG, "sendCommand() called but not connected");
            return;
        }

        new Thread(() -> {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
                Log.d(TAG, "Sent: " + command.trim());
            } catch (IOException e) {
                Log.e(TAG, "Send failed: " + e.getMessage());
                mainHandler.post(() -> {
                    if (stateListener != null)
                        stateListener.onError("Send failed: " + e.getMessage());
                });
            }
        }, "bt-send-thread").start();
    }

    // ── Receive Loop ──────────────────────────────────────────────────────

    /**
     * Runs on a dedicated background thread for the lifetime of the connection.
     *
     * Why a StringBuilder buffer?
     *   Bluetooth Classic delivers data in chunks. A single read() call may return
     *   a fragment like "DO" and the next "OR_OPEN\n". The buffer accumulates chunks
     *   and only emits complete '\n'-terminated messages to the listener.
     */
    private void startReceiveLoop() {
        receiveThread = new Thread(() -> {
            byte[]        buffer        = new byte[1024];
            StringBuilder messageBuffer = new StringBuilder();

            while (isConnected) {
                try {
                    int    bytes = inputStream.read(buffer);
                    String chunk = new String(buffer, 0, bytes);
                    messageBuffer.append(chunk);

                    // Emit all complete '\n'-terminated messages
                    int newlineIdx;
                    while ((newlineIdx = messageBuffer.indexOf("\n")) != -1) {
                        String message = messageBuffer.substring(0, newlineIdx).trim();
                        messageBuffer.delete(0, newlineIdx + 1);

                        if (!message.isEmpty()) {
                            Log.d(TAG, "Received: " + message);
                            mainHandler.post(() -> {
                                if (stateListener != null)
                                    stateListener.onDataReceived(message);
                            });
                        }
                    }

                } catch (IOException e) {
                    if (isConnected) {
                        isConnected = false;
                        Log.e(TAG, "Connection lost: " + e.getMessage());
                        mainHandler.post(() -> {
                            if (stateListener != null)
                                stateListener.onDisconnected("Connection lost unexpectedly");
                        });
                    }
                    break;
                }
            }
        }, "bt-receive-thread");

        receiveThread.start();
    }

    // ── Disconnect ────────────────────────────────────────────────────────

    /** Cleanly closes the socket and releases all stream resources. */
    public void disconnect() {
        isConnected = false;
        closeSocket();
        mainHandler.post(() -> {
            if (stateListener != null)
                stateListener.onDisconnected("Disconnected by user");
        });
        Log.d(TAG, "Disconnected");
    }

    private void closeSocket() {
        try { if (outputStream != null) outputStream.close(); } catch (IOException ignored) {}
        try { if (inputStream  != null) inputStream.close();  } catch (IOException ignored) {}
        try { if (socket       != null) socket.close();       } catch (IOException ignored) {}
        outputStream = null;
        inputStream  = null;
        socket       = null;
    }

    // ── Status ────────────────────────────────────────────────────────────

    public boolean isBluetoothEnabled() {
        return adapter != null && adapter.isEnabled();
    }

    public boolean isConnected() {
        return isConnected;
    }
}
