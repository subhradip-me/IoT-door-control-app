package com.fasla.doorcontrol.features.bluetooth.presentation.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * BluetoothService — background service managing the BT GATT connection.
 * TODO: Implement connection lifecycle, data read/write, notifications.
 */
public class BluetoothService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: Implement
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return binder for bound service pattern
        return null;
    }
}
