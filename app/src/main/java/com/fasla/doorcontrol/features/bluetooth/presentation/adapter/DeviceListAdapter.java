package com.fasla.doorcontrol.features.bluetooth.presentation.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fasla.doorcontrol.core.callbacks.DeviceClickListener;
import com.fasla.doorcontrol.core.models.BluetoothDeviceModel;
import java.util.List;

/**
 * DeviceListAdapter — RecyclerView adapter for the discovered devices list.
 * TODO: Implement ViewHolder, onBindViewHolder, diff util.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {

    private List<BluetoothDeviceModel> devices;
    private final DeviceClickListener clickListener;

    public DeviceListAdapter(List<BluetoothDeviceModel> devices, DeviceClickListener clickListener) {
        this.devices = devices;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // TODO: Inflate item_device.xml
        throw new UnsupportedOperationException("TODO: implement");
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        // TODO: Bind device data and set click listener
    }

    @Override
    public int getItemCount() {
        return devices != null ? devices.size() : 0;
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        public DeviceViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            // TODO: Bind views
        }
    }
}
