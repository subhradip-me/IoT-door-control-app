package com.fasla.doorcontrol;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.fasla.doorcontrol.navigation.AppNavigator;

/**
 * MainActivity — app entry point.
 *
 * AUTH BYPASS (dev mode): redirects directly to HomeActivity.
 * To re-enable auth: replace goToHome() with goToLogin().
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── DEV MODE: Skip auth, go straight to Home ──────────────────────
        AppNavigator.goToHome(this);
        finish(); // Remove MainActivity from the back stack

        // ── PRODUCTION: Uncomment to restore auth flow ────────────────────
        // AppNavigator.goToLogin(this);
        // finish();
    }
}
