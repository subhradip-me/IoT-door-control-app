package com.fasla.doorcontrol;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.fasla.doorcontrol.navigation.AppNavigator;

/**
 * MainActivity — app entry point.
 *
 * Directs the user to the Login screen. After successful authentication,
 * LoginActivity will navigate to HomeActivity.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── AUTH FLOW: Navigate to Login, clear back stack ─────────────────
        AppNavigator.goToLogin(this);
        finish(); // Remove MainActivity from the back stack
    }
}
