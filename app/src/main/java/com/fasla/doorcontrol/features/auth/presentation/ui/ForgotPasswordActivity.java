package com.fasla.doorcontrol.features.auth.presentation.ui;

import android.os.Bundle;
import android.widget.ImageButton;

import com.fasla.doorcontrol.R;
import com.fasla.doorcontrol.core.base.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends BaseActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnReset;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        tilEmail = findViewById(R.id.tilEmail);
        etEmail  = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnReset);
        btnBack  = findViewById(R.id.btnBack);

        if (btnBack != null)  btnBack.setOnClickListener(v -> finish());
        if (btnReset != null) btnReset.setOnClickListener(v -> handleReset());
    }

    private void handleReset() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (email.isEmpty()) {
            tilEmail.setError("Email required");
            return;
        } else {
            tilEmail.setError(null);
        }

        // TODO: Replace with real reset via AuthViewModel -> AuthRepository
        showToast("Reset Link Sent to " + email);
        finish();
    }
}
