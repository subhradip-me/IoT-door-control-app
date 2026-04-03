package com.fasla.doorcontrol;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.fasla.doorcontrol.navigation.AppNavigator;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(v ->
                AppNavigator.goToLogin(this)
        );
    }
}
