package com.fasla.doorcontrol.features.home.presentation.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fasla.doorcontrol.R;
import com.fasla.doorcontrol.core.base.BaseActivity;
import com.fasla.doorcontrol.core.bluetooth.ConnectionState;
import com.fasla.doorcontrol.features.bluetooth.presentation.ui.BluetoothActivity;
import com.fasla.doorcontrol.features.bluetooth.presentation.viewmodel.BluetoothViewModel;
import com.fasla.doorcontrol.features.settings.presentation.ui.SettingsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

public class HomeActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────────
    private TextView           tvGreeting;
    private TextView           tvDoorState;
    private TextView           tvConnectionStatus;
    private TextView           tvDeviceName;
    private View               viewStatusDot;
    private MaterialButton     btnOpen;
    private MaterialButton     btnClose;
    private MaterialCardView   cardBluetooth;
    private ImageButton        btnSettings;

    // ── ViewModel ─────────────────────────────────────────────────────────
    private BluetoothViewModel btViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bindViews();
        setGreeting();
        setupListeners();
        observeBluetoothState();
    }

    private void bindViews() {
        tvGreeting         = findViewById(R.id.tvGreeting);
        tvDoorState        = findViewById(R.id.tvDoorState);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvDeviceName       = findViewById(R.id.tvDeviceName);
        viewStatusDot      = findViewById(R.id.viewStatusDot);
        btnOpen            = findViewById(R.id.btnOpen);
        btnClose           = findViewById(R.id.btnClose);
        cardBluetooth      = findViewById(R.id.cardBluetooth);
        btnSettings        = findViewById(R.id.btnSettings);
    }

    // ── Greeting based on time of day ─────────────────────────────────────
    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if      (hour < 12) greeting = "Good morning 👋";
        else if (hour < 17) greeting = "Good afternoon 👋";
        else                greeting = "Good evening 👋";
        tvGreeting.setText(greeting);
    }

    // ── Listeners ─────────────────────────────────────────────────────────
    private void setupListeners() {
        // Navigate to Bluetooth scanner
        cardBluetooth.setOnClickListener(v ->
                startActivity(new Intent(this, BluetoothActivity.class))
        );

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );

        // Commands — only active when ViewModel is connected
        btnOpen.setOnClickListener(v -> {
            if (btViewModel != null) btViewModel.sendOpen();
            showToast("OPEN command sent");
        });

        btnClose.setOnClickListener(v -> {
            if (btViewModel != null) btViewModel.sendClose();
            showToast("CLOSE command sent");
        });
    }

    // ── Observe Bluetooth State ───────────────────────────────────────────
    /**
     * BluetoothViewModel is scoped to the Application process via BluetoothActivity.
     * HomeActivity observes the same shared ViewModel so it reflects live BT state
     * when the user navigates back from BluetoothActivity.
     *
     * Note: In a full DI setup, the ViewModel would be injected application-scoped.
     * For now, it's re-created here — state won't persist across the back stack
     * until BluetoothService is implemented. For testing, this is sufficient.
     */
    private void observeBluetoothState() {
        btViewModel = new ViewModelProvider(this).get(BluetoothViewModel.class);

        btViewModel.getConnectionState().observe(this, state -> {
            updateConnectionUI(state);
        });

        btViewModel.getDoorState().observe(this, doorState -> {
            updateDoorStateUI(doorState);
        });

        btViewModel.getConnectedName().observe(this, name -> {
            if (name != null && !name.isEmpty()) {
                tvDeviceName.setText(name);
            } else {
                tvDeviceName.setText("Tap to scan and connect");
            }
        });
    }

    // ── UI Updates ────────────────────────────────────────────────────────

    private void updateConnectionUI(ConnectionState state) {
        int dotColor;
        String statusText;
        boolean actionsEnabled;

        switch (state) {
            case CONNECTED:
                dotColor       = ContextCompat.getColor(this, R.color.bt_connected);
                statusText     = "Connected";
                actionsEnabled = true;
                break;
            case CONNECTING:
                dotColor       = ContextCompat.getColor(this, R.color.bt_connecting);
                statusText     = "Connecting…";
                actionsEnabled = false;
                break;
            case SCANNING:
                dotColor       = ContextCompat.getColor(this, R.color.bt_scanning);
                statusText     = "Scanning…";
                actionsEnabled = false;
                break;
            case ERROR:
                dotColor       = ContextCompat.getColor(this, R.color.bt_error);
                statusText     = "Connection error";
                actionsEnabled = false;
                break;
            default: // DISCONNECTED
                dotColor       = ContextCompat.getColor(this, R.color.bt_disconnected);
                statusText     = "Not connected";
                actionsEnabled = false;
                break;
        }

        // Tint the status dot
        GradientDrawable dot = (GradientDrawable) viewStatusDot.getBackground().mutate();
        dot.setColor(dotColor);

        tvConnectionStatus.setText(statusText);

        // Enable / disable action buttons
        btnOpen.setEnabled(actionsEnabled);
        btnClose.setEnabled(actionsEnabled);
        btnOpen.setAlpha(actionsEnabled ? 1.0f : 0.38f);
        btnClose.setAlpha(actionsEnabled ? 1.0f : 0.38f);
    }

    private void updateDoorStateUI(BluetoothViewModel.DoorState doorState) {
        switch (doorState) {
            case OPEN:
                tvDoorState.setText("OPEN");
                tvDoorState.setTextColor(ContextCompat.getColor(this, R.color.status_open));
                break;
            case CLOSED:
                tvDoorState.setText("LOCKED");
                tvDoorState.setTextColor(ContextCompat.getColor(this, R.color.status_locked));
                break;
            default:
                tvDoorState.setText("UNKNOWN");
                tvDoorState.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
                break;
        }
    }
}
