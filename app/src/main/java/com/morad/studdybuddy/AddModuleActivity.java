package com.morad.studdybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddModuleActivity extends AppCompatActivity {

    private EditText etModuleCode, etModuleName, etModuleDescription;
    private MaterialButton btnAddModule;
    private BottomNavigationView bottomNavigation;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_module);

        etModuleCode = findViewById(R.id.etModuleCode);
        etModuleName = findViewById(R.id.etModuleName);
        etModuleDescription = findViewById(R.id.etModuleDescription);
        btnAddModule = findViewById(R.id.btnAddModule);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupBottomNavigation();

        btnAddModule.setOnClickListener(v -> {

            String moduleCode = etModuleCode.getText().toString().trim().toUpperCase();
            String moduleName = etModuleName.getText().toString().trim();
            String moduleDescription = etModuleDescription.getText().toString().trim();

            if (moduleCode.isEmpty()) {
                Toast.makeText(this, "Enter module code", Toast.LENGTH_SHORT).show();
                return;
            }

            if (moduleName.isEmpty()) {
                Toast.makeText(this, "Enter module name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (moduleDescription.isEmpty()) {
                Toast.makeText(this, "Enter module description", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(userDocument -> {

                        if (!userDocument.exists()) {
                            Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String subjectArea = userDocument.getString("subjectArea");

                        if (subjectArea == null || subjectArea.isEmpty()) {
                            Toast.makeText(this, "Subject area not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("modules")
                                .document(moduleCode)
                                .get()
                                .addOnSuccessListener(moduleDocument -> {

                                    if (moduleDocument.exists()) {
                                        String existingName = moduleDocument.getString("moduleName");
                                        String existingDescription = moduleDocument.getString("moduleDescription");
                                        String existingSubjectArea = moduleDocument.getString("subjectArea");

                                        if (existingSubjectArea != null && !existingSubjectArea.equals(subjectArea)) {
                                            Toast.makeText(this,
                                                    "This module belongs to a different department",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        joinModuleForUser(
                                                userId,
                                                moduleCode,
                                                existingName,
                                                existingDescription,
                                                subjectArea
                                        );
                                    } else {
                                        Map<String, Object> globalModule = new HashMap<>();
                                        globalModule.put("moduleCode", moduleCode);
                                        globalModule.put("moduleName", moduleName);
                                        globalModule.put("moduleDescription", moduleDescription);
                                        globalModule.put("subjectArea", subjectArea);
                                        globalModule.put("studentCount", 0);

                                        db.collection("modules")
                                                .document(moduleCode)
                                                .set(globalModule)
                                                .addOnSuccessListener(unused ->
                                                        joinModuleForUser(
                                                                userId,
                                                                moduleCode,
                                                                moduleName,
                                                                moduleDescription,
                                                                subjectArea
                                                        )
                                                )
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "Error creating module", Toast.LENGTH_SHORT).show()
                                                );
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error checking module", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_add_module);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(AddModuleActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_add_module) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(AddModuleActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private void joinModuleForUser(String userId,
                                   String moduleCode,
                                   String moduleName,
                                   String moduleDescription,
                                   String subjectArea) {

        db.collection("users")
                .document(userId)
                .collection("modules")
                .document(moduleCode)
                .get()
                .addOnSuccessListener(existingJoin -> {
                    if (existingJoin.exists()) {
                        Toast.makeText(this, "You already joined this module", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> joinedModule = new HashMap<>();
                    joinedModule.put("moduleCode", moduleCode);
                    joinedModule.put("moduleName", moduleName);
                    joinedModule.put("moduleDescription", moduleDescription);
                    joinedModule.put("subjectArea", subjectArea);

                    db.collection("users")
                            .document(userId)
                            .collection("modules")
                            .document(moduleCode)
                            .set(joinedModule)
                            .addOnSuccessListener(unused -> {
                                db.collection("modules")
                                        .document(moduleCode)
                                        .update("studentCount", FieldValue.increment(1));

                                Toast.makeText(this, "Module joined successfully", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(AddModuleActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error joining module", Toast.LENGTH_SHORT).show()
                            );
                });
    }

}