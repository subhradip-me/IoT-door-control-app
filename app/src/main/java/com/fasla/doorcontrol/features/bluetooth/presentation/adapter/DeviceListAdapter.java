package com.fasla.doorcontrol.features.bluetooth.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fasla.doorcontrol.R;
import com.fasla.doorcontrol.core.callbacks.DeviceClickListener;
import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * DeviceListAdapter — RecyclerView adapter for the Bluetooth device scan list.
 *
 * Displays device name, MAC address, and a "Paired" badge for already-bonded devices.
 * Delegates item click to DeviceClickListener (implemented by BluetoothActivity/Fragment).
 */
public class DeviceListAdapter
        extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {

    private List<BluetoothDeviceModel> devices = new ArrayList<>();
    private final DeviceClickListener  clickListener;

    public DeviceListAdapter(DeviceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // ── Data Update ───────────────────────────────────────────────────────

    public void setDevices(List<BluetoothDeviceModel> newDevices) {
        this.devices = newDevices != null ? newDevices : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDeviceModel device = devices.get(position);
        holder.bind(device, clickListener);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDeviceName;
        private final TextView tvDeviceAddress;
        private final TextView tvPairedBadge;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName    = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceAddress = itemView.findViewById(R.id.tvDeviceAddress);
            tvPairedBadge   = itemView.findViewById(R.id.tvPairedBadge);
        }

        public void bind(BluetoothDeviceModel device, DeviceClickListener listener) {
            tvDeviceName.setText(device.getName());
            tvDeviceAddress.setText(device.getAddress());
            tvPairedBadge.setVisibility(device.isBonded() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDeviceClicked(device);
            });
        }
    }
}
