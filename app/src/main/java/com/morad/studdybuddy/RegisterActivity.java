package com.morad.studdybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout layoutFullName, layoutEmail, layoutPassword;
    private EditText etFullName, etEmail, etPassword;
    private Spinner spinnerSubjectArea;
    private Button btnCreateAccount;
    private TextView tvGoToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final String[] subjectAreas = {
            "Select Subject Area",
            "Computer Science & Informatics",
            "Engineering",
            "Business & Management",
            "Law",
            "Health & Life Sciences",
            "Arts & Media",
            "Education & Sport",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        layoutFullName = findViewById(R.id.layoutFullName);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        spinnerSubjectArea = findViewById(R.id.spinnerSubjectArea);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                subjectAreas
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjectArea.setAdapter(adapter);

        btnCreateAccount.setOnClickListener(v -> {
            layoutFullName.setError(null);
            layoutEmail.setError(null);
            layoutPassword.setError(null);

            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String subjectArea = spinnerSubjectArea.getSelectedItem().toString();

            if (fullName.isEmpty()) {
                layoutFullName.setError("Enter your full name");
                return;
            }

            if (email.isEmpty()) {
                layoutEmail.setError("Enter your university email");
                return;
            }

            if (!email.endsWith("@uni.brighton.ac.uk")) {
                layoutEmail.setError("Use your university email");
                return;
            }

            if (password.isEmpty()) {
                layoutPassword.setError("Enter your password");
                return;
            }

            if (password.length() < 6) {
                layoutPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (subjectArea.equals("Select Subject Area")) {
                Toast.makeText(this, "Please choose a subject area", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            String userId = mAuth.getCurrentUser().getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("uid", userId);
                            user.put("fullName", fullName);
                            user.put("email", email);
                            user.put("subjectArea", subjectArea);

                            db.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to save user profile", Toast.LENGTH_SHORT).show()
                                    );

                        } else {
                            String errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "";

                            if (errorMessage.contains("already in use")) {
                                layoutEmail.setError("This email is already registered");
                            } else if (errorMessage.contains("badly formatted")) {
                                layoutEmail.setError("Enter a valid email address");
                            } else if (errorMessage.contains("least 6 characters")) {
                                layoutPassword.setError("Password must be at least 6 characters");
                            } else {
                                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }
}
