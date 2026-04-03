package com.fasla.doorcontrol.features.bluetooth.presentation.ui;

import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasla.doorcontrol.R;
import com.fasla.doorcontrol.core.base.BaseActivity;
import com.fasla.doorcontrol.core.bluetooth.ConnectionState;
import com.fasla.doorcontrol.core.callbacks.DeviceClickListener;
import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;
import com.fasla.doorcontrol.core.utils.PermissionUtils;
import com.fasla.doorcontrol.features.bluetooth.presentation.adapter.DeviceListAdapter;
import com.fasla.doorcontrol.features.bluetooth.presentation.viewmodel.BluetoothViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * BluetoothActivity — scan, discover, and connect to Classic Bluetooth devices.
 *
 * Testing without ESP32:
 *   - Bonded devices (headphones, laptops, etc.) appear instantly on open
 *   - Tap "Scan" to discover additional nearby devices
 *   - Tap any device to attempt RFCOMM connection
 *   - Non-SPP devices (headphones) will fail gracefully with a clear toast message
 *   - State banner at the top reflects: Disconnected / Scanning / Connecting / Connected
 */
public class BluetoothActivity extends BaseActivity implements DeviceClickListener {

    // ── Views ─────────────────────────────────────────────────────────────
    private RecyclerView      rvDevices;
    private MaterialButton    btnScan;
    private MaterialButton    btnDisconnect;
    private View              layoutEmpty;
    private TextView          tvSectionLabel;
    private TextView          tvBannerStatus;
    private View              viewBannerDot;
    private ImageButton       btnBack;

    // ── ViewModel + Adapter ───────────────────────────────────────────────
    private BluetoothViewModel viewModel;
    private DeviceListAdapter  adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bindViews();
        setupRecyclerView();
        setupListeners();
        setupViewModel();
        checkPermissionsAndLoad();
    }

    // ── Setup ─────────────────────────────────────────────────────────────

    private void bindViews() {
        rvDevices      = findViewById(R.id.rvDevices);
        btnScan        = findViewById(R.id.btnScan);
        btnDisconnect  = findViewById(R.id.btnDisconnect);
        layoutEmpty    = findViewById(R.id.layoutEmpty);
        tvSectionLabel = findViewById(R.id.tvSectionLabel);
        tvBannerStatus = findViewById(R.id.tvBannerStatus);
        viewBannerDot  = findViewById(R.id.viewBannerDot);
        btnBack        = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new DeviceListAdapter(this);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(adapter);
        rvDevices.setHasFixedSize(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnScan.setOnClickListener(v -> {
            ConnectionState state = viewModel.getConnectionState().getValue();
            if (state == ConnectionState.SCANNING) {
                viewModel.stopScan();
                btnScan.setText("Scan");
            } else {
                viewModel.startScan();
                btnScan.setText("Stop");
                tvSectionLabel.setText("NEARBY DEVICES");
            }
        });

        btnDisconnect.setOnClickListener(v -> {
            viewModel.disconnect();
            btnDisconnect.setVisibility(View.GONE);
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BluetoothViewModel.class);

        // Device list
        viewModel.getScannedDevices().observe(this, devices -> {
            adapter.setDevices(devices);
            boolean empty = devices == null || devices.isEmpty();
            rvDevices.setVisibility(empty ? View.GONE : View.VISIBLE);
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        // Connection state
        viewModel.getConnectionState().observe(this, state -> {
            updateBanner(state);

            boolean isConnected = state == ConnectionState.CONNECTED;
            btnDisconnect.setVisibility(isConnected ? View.VISIBLE : View.GONE);

            if (state != ConnectionState.SCANNING) {
                btnScan.setText("Scan");
            }
        });

        // Error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showToast(error);
            }
        });

        // Connected device name  
        viewModel.getConnectedName().observe(this, name -> {
            if (name != null) {
                tvSectionLabel.setText("CONNECTED: " + name.toUpperCase());
            }
        });
    }

    // ── Permission Check + Load Bonded ────────────────────────────────────

    private void checkPermissionsAndLoad() {
        if (!PermissionUtils.hasBluetoothPermissions(this)) {
            PermissionUtils.requestBluetoothPermissions(this);
        } else {
            loadBondedDevices();
        }
    }

    private void loadBondedDevices() {
        tvSectionLabel.setText("PAIRED DEVICES");
        viewModel.loadBondedDevices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQUEST_CODE_BLUETOOTH) {
            if (PermissionUtils.allGranted(grantResults)) {
                loadBondedDevices();
            } else {
                showToast("Bluetooth permissions are required to scan for devices");
            }
        }
    }

    // ── DeviceClickListener ───────────────────────────────────────────────

    @Override
    public void onDeviceClicked(BluetoothDeviceModel device) {
        ConnectionState state = viewModel.getConnectionState().getValue();
        if (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) {
            showToast("Already " + (state == ConnectionState.CONNECTED ? "connected" : "connecting") + ". Disconnect first.");
            return;
        }
        showToast("Connecting to " + device.getName() + "…");
        viewModel.connect(device.getAddress(), device.getName());
    }

    // ── Banner UI ─────────────────────────────────────────────────────────

    private void updateBanner(ConnectionState state) {
        int dotColor;
        String text;

        switch (state) {
            case CONNECTED:
                dotColor = ContextCompat.getColor(this, R.color.bt_connected);
                text     = "Connected";
                break;
            case CONNECTING:
                dotColor = ContextCompat.getColor(this, R.color.bt_connecting);
                text     = "Connecting…";
                break;
            case SCANNING:
                dotColor = ContextCompat.getColor(this, R.color.bt_scanning);
                text     = "Scanning for devices…";
                break;
            case ERROR:
                dotColor = ContextCompat.getColor(this, R.color.bt_error);
                text     = "Error — tap a device to retry";
                break;
            default:
                dotColor = ContextCompat.getColor(this, R.color.bt_disconnected);
                text     = "Not connected";
                break;
        }

        GradientDrawable dot = (GradientDrawable) viewBannerDot.getBackground().mutate();
        dot.setColor(dotColor);
        tvBannerStatus.setText(text);
    }
}
