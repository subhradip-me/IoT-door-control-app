package com.fasla.doorcontrol;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);
        
        btnGetStarted.setOnClickListener(v -> {
            // Traverse from Home onboarding to Login Flow
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
    }
}
