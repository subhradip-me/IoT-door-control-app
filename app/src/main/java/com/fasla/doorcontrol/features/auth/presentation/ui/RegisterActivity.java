package com.fasla.doorcontrol.features.auth.presentation.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fasla.doorcontrol.R;
import com.fasla.doorcontrol.core.base.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends BaseActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnGoogle;
    private TextView tvLogin;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Bind Views
        tilName            = findViewById(R.id.tilName);
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etName             = findViewById(R.id.etName);
        etEmail            = findViewById(R.id.etEmail);
        etPassword         = findViewById(R.id.etPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);

        btnRegister        = findViewById(R.id.btnRegister);
        btnGoogle          = findViewById(R.id.btnGoogle);
        tvLogin            = findViewById(R.id.tvLogin);
        btnBack            = findViewById(R.id.btnBack);

        // Event Listeners
        if (btnBack != null)    btnBack.setOnClickListener(v -> finish());
        if (tvLogin != null)    tvLogin.setOnClickListener(v -> finish());

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v ->
                showToast("Google Sign-Up coming soon!")
            );
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> handleRegistration());
        }
    }

    private void handleRegistration() {
        String name        = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email       = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password    = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        boolean valid = true;

        if (name.isEmpty()) {
            tilName.setError("Name required");
            valid = false;
        } else tilName.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError("Email required");
            valid = false;
        } else tilEmail.setError(null);

        if (password.isEmpty()) {
            tilPassword.setError("Password required");
            valid = false;
        } else tilPassword.setError(null);

        if (!password.equals(confirmPass)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else tilConfirmPassword.setError(null);

        if (!valid) return;

        // TODO: Replace with real registration via AuthViewModel -> RegisterUseCase -> AuthRepository
        showToast("Simulating Registration for " + email);
    }
}
