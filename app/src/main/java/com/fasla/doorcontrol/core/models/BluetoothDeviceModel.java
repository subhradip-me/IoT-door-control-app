package com.fasla.doorcontrol.core.models;

/**
 * BluetoothDeviceModel — POJO representing a discovered or paired Bluetooth device.
 *
 * Fields:
 *   name    — human-readable device name (e.g., "DoorControl", "Sony WH-1000XM4")
 *   address — MAC address (e.g., "AA:BB:CC:DD:EE:FF")
 *   bonded  — true if the device is already paired with the phone
 */
public class BluetoothDeviceModel {

    private String  name;
    private String  address;
    private boolean bonded;

    /** Constructor for a discovered (not yet paired) device */
    public BluetoothDeviceModel(String name, String address) {
        this.name    = name;
        this.address = address;
        this.bonded  = false;
    }

    /** Constructor for a device with explicit bonded flag */
    public BluetoothDeviceModel(String name, String address, boolean bonded) {
        this.name    = name;
        this.address = address;
        this.bonded  = bonded;
    }

    public String  getName()    { return name;    }
    public String  getAddress() { return address; }
    public boolean isBonded()   { return bonded;  }

    public void setName(String name)       { this.name    = name;    }
    public void setAddress(String address) { this.address = address; }
    public void setBonded(boolean bonded)  { this.bonded  = bonded;  }
}
