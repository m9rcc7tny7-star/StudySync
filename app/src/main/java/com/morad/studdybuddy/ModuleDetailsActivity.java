package com.morad.studdybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class ModuleDetailsActivity extends AppCompatActivity {

    private TextView tvModuleTitle, tvModuleDescription;
    private Button btnLeaveModule, btnStudyGroups, btnStudySessions, btnResources, btnDiscussion;

    private String moduleCode;
    private String moduleName;
    private String moduleDescription;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_details);

        tvModuleTitle = findViewById(R.id.tvModuleTitle);
        tvModuleDescription = findViewById(R.id.tvModuleDescription);
        btnLeaveModule = findViewById(R.id.btnLeaveModule);
        btnStudyGroups = findViewById(R.id.btnStudyGroups);
        btnStudySessions = findViewById(R.id.btnStudySessions);
        btnResources = findViewById(R.id.btnResources);
        btnDiscussion = findViewById(R.id.btnDiscussion);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");
        moduleName = getIntent().getStringExtra("moduleName");
        moduleDescription = getIntent().getStringExtra("moduleDescription");

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        tvModuleTitle.setText(moduleCode + " - " + moduleName);

        if (moduleDescription != null && !moduleDescription.isEmpty()) {
            tvModuleDescription.setText(moduleDescription);
        } else {
            tvModuleDescription.setText("No description available.");
        }

        btnLeaveModule.setOnClickListener(v -> showLeaveModuleDialog());

        btnStudyGroups.setOnClickListener(v -> {
            Intent intent = new Intent(ModuleDetailsActivity.this, StudyGroupsActivity.class);
            intent.putExtra("moduleCode", moduleCode);
            startActivity(intent);
        });

        btnStudySessions.setOnClickListener(v -> {
            Intent intent = new Intent(ModuleDetailsActivity.this, StudySessionsActivity.class);
            intent.putExtra("moduleCode", moduleCode);
            startActivity(intent);
        });

        btnResources.setOnClickListener(v -> {
            Intent intent = new Intent(ModuleDetailsActivity.this, ResourcesActivity.class);
            intent.putExtra("moduleCode", moduleCode);
            startActivity(intent);
        });

        btnDiscussion.setOnClickListener(v -> {
            Intent intent = new Intent(ModuleDetailsActivity.this, DiscussionActivity.class);
            intent.putExtra("moduleCode", moduleCode);
            startActivity(intent);
        });
    }

    private void showLeaveModuleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Module")
                .setMessage("Are you sure you want to leave this module?")
                .setPositiveButton("Leave", (dialog, which) -> leaveModule())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveModule() {
        if (currentUserId == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .collection("modules")
                .document(moduleCode)
                .delete()
                .addOnSuccessListener(unused -> {
                    db.collection("modules")
                            .document(moduleCode)
                            .update("studentCount", FieldValue.increment(-1));

                    Toast.makeText(this, "Module left", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ModuleDetailsActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave module", Toast.LENGTH_SHORT).show()
                );
    }
}