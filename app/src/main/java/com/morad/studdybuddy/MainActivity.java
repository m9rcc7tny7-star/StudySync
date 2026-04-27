package com.morad.studdybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail, layoutPassword;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            layoutEmail.setError(null);
            layoutPassword.setError(null);

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                layoutEmail.setError("Enter your email");
                return;
            }

            if (password.isEmpty()) {
                layoutPassword.setError("Enter your password");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "";

                            if (errorMessage.contains("badly formatted")) {
                                layoutEmail.setError("Enter a valid email address");
                            } else if (errorMessage.contains("no user record")
                                    || errorMessage.contains("password is invalid")
                                    || errorMessage.contains("INVALID_LOGIN_CREDENTIALS")
                                    || errorMessage.contains("The supplied auth credential is incorrect")) {
                                layoutPassword.setError("Incorrect email or password");
                            } else {
                                layoutPassword.setError("Login failed");
                            }
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}