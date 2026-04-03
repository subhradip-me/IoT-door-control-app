package com.fasla.doorcontrol.core.models;

/**
 * BluetoothDeviceModel — POJO representing a discovered Bluetooth device.
 * TODO: Add RSSI, bonding state, device type fields.
 */
public class BluetoothDeviceModel {

    private String name;
    private String address;

    public BluetoothDeviceModel(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
}
