package com.fasla.doorcontrol.core.base;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity — shared base for all Activities in the app.
 * TODO: Add common lifecycle helpers, loading dialogs, keyboard utils, etc.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
