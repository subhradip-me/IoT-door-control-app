package com.fasla.doorcontrol.features.auth.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * AuthPreferences — manages auth token storage in SharedPreferences.
 * TODO: Add save/get/clear token methods, remember-me flag, etc.
 */
public class AuthPreferences {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN  = "auth_token";

    private final SharedPreferences prefs;

    public AuthPreferences(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // TODO: public void saveToken(String token) { ... }
    // TODO: public String getToken() { ... }
    // TODO: public void clearToken() { ... }
}
