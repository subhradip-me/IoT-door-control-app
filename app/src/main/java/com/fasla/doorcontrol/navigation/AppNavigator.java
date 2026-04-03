package com.fasla.doorcontrol.navigation;

import android.content.Context;
import android.content.Intent;

import com.fasla.doorcontrol.features.auth.presentation.ui.ForgotPasswordActivity;
import com.fasla.doorcontrol.features.auth.presentation.ui.LoginActivity;
import com.fasla.doorcontrol.features.auth.presentation.ui.RegisterActivity;
import com.fasla.doorcontrol.features.bluetooth.presentation.ui.BluetoothActivity;
import com.fasla.doorcontrol.features.home.presentation.ui.HomeActivity;

/**
 * AppNavigator — single source of truth for all screen transitions.
 */
public final class AppNavigator {

    private AppNavigator() { /* no instances */ }

    /** Navigate to HomeActivity, clearing the back stack. */
    public static void goToHome(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /** Navigate to LoginActivity, clearing the back stack. */
    public static void goToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /** Navigate to RegisterActivity. */
    public static void goToRegister(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
    }

    /** Navigate to ForgotPasswordActivity. */
    public static void goToForgotPassword(Context context) {
        context.startActivity(new Intent(context, ForgotPasswordActivity.class));
    }

    /** Navigate to BluetoothActivity. */
    public static void goToBluetooth(Context context) {
        context.startActivity(new Intent(context, BluetoothActivity.class));
    }
}
