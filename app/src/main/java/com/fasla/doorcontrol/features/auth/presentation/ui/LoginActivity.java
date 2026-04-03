package com.fasla.doorcontrol.features.auth.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fasla.doorcontrol.R;
import com.fasla.doorcontrol.core.base.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends BaseActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvForgot, tvRegister;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect views
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        btnGoogle   = findViewById(R.id.btnGoogle);
        tvForgot    = findViewById(R.id.tvForgot);
        tvRegister  = findViewById(R.id.tvRegister);
        btnBack     = findViewById(R.id.btnBack);

        // Listeners
        btnBack.setOnClickListener(v -> finish());

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnGoogle.setOnClickListener(v ->
                showToast("Google Sign In coming soon!")
        );

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean valid = true;

        if (email.isEmpty()) {
            tilEmail.setError("Enter your email or username");
            valid = false;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Enter your password");
            valid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!valid) return;

        // TODO: Replace with real auth via AuthViewModel -> LoginUseCase -> AuthRepository
        if (email.equals("admin") && password.equals("admin123")) {
            showToast("Login Successful! Welcome Admin");
            // TODO: AppNavigator.goToHome(this);
        } else {
            tilPassword.setError("Wrong credentials");
            etPassword.getText().clear();
        }
    }
}
