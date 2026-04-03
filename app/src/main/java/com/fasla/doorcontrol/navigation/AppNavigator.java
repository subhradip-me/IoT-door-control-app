package com.fasla.doorcontrol.navigation;

import android.content.Context;
import android.content.Intent;

import com.fasla.doorcontrol.features.auth.presentation.ui.ForgotPasswordActivity;
import com.fasla.doorcontrol.features.auth.presentation.ui.LoginActivity;
import com.fasla.doorcontrol.features.auth.presentation.ui.RegisterActivity;
import com.fasla.doorcontrol.features.home.presentation.ui.HomeActivity;

/**
 * AppNavigator — centralized navigation helper.
 *
 * Use these static methods instead of scattering raw Intent calls around the app.
 * Add new destinations here as each feature screen is built.
 */
public final class AppNavigator {

    private AppNavigator() { /* no instances */ }

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

    /** Navigate to HomeActivity, clearing the back stack (post-login). */
    public static void goToHome(Context context) {
        // TODO: Uncomment when HomeActivity layout is ready
        // Intent intent = new Intent(context, HomeActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // context.startActivity(intent);
    }
}
