package com.fasla.doorcontrol.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * PermissionUtils — runtime permission helpers for Bluetooth and Location.
 *
 * Android 12+ (API 31+) uses BLUETOOTH_SCAN + BLUETOOTH_CONNECT.
 * Below API 31 uses BLUETOOTH + BLUETOOTH_ADMIN + ACCESS_FINE_LOCATION.
 */
public final class PermissionUtils {

    private PermissionUtils() { /* no instances */ }

    public static final int REQUEST_CODE_BLUETOOTH = 1001;

    /**
     * Returns the correct set of Bluetooth permissions for the running API level.
     */
    public static String[] getRequiredBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+ — Android 12+
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            // API 23–30
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }

    /**
     * Returns true if all required Bluetooth permissions are already granted.
     */
    public static boolean hasBluetoothPermissions(Context context) {
        for (String permission : getRequiredBluetoothPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests all required Bluetooth permissions from the user.
     * Handle the result in Activity.onRequestPermissionsResult().
     */
    public static void requestBluetoothPermissions(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                getRequiredBluetoothPermissions(),
                REQUEST_CODE_BLUETOOTH
        );
    }

    /**
     * Checks whether results from onRequestPermissionsResult() were all granted.
     */
    public static boolean allGranted(int[] grantResults) {
        if (grantResults.length == 0) return false;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }
}
