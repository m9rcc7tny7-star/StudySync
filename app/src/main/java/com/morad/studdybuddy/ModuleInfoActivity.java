package com.morad.studdybuddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ModuleInfoActivity extends AppCompatActivity {

    private TextView tvModuleCode, tvModuleName, tvModuleDescription;
    private Button btnJoinLeaveModule;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String moduleCode;
    private String moduleName;
    private String moduleDescription;
    private String subjectArea;
    private String currentUserId;

    private boolean isJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_info);

        tvModuleCode = findViewById(R.id.tvModuleCode);
        tvModuleName = findViewById(R.id.tvModuleName);
        tvModuleDescription = findViewById(R.id.tvModuleDescription);
        btnJoinLeaveModule = findViewById(R.id.btnJoinLeaveModule);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");
        moduleName = getIntent().getStringExtra("moduleName");
        moduleDescription = getIntent().getStringExtra("moduleDescription");
        subjectArea = getIntent().getStringExtra("subjectArea");

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        tvModuleCode.setText(moduleCode);
        tvModuleName.setText(moduleName);
        tvModuleDescription.setText(moduleDescription);

        checkIfJoined();

        btnJoinLeaveModule.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isJoined) {
                leaveModule();
            } else {
                joinModule();
            }
        });
    }

    private void checkIfJoined() {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("modules")
                .document(moduleCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isJoined = documentSnapshot.exists();

                    if (isJoined) {
                        btnJoinLeaveModule.setText("Leave Module");
                    } else {
                        btnJoinLeaveModule.setText("Join Module");
                    }
                });
    }

    private void joinModule() {
        db.collection("users")
                .document(currentUserId)
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
                            .document(currentUserId)
                            .collection("modules")
                            .document(moduleCode)
                            .set(joinedModule)
                            .addOnSuccessListener(unused -> {
                                db.collection("modules")
                                        .document(moduleCode)
                                        .update("studentCount", FieldValue.increment(1));

                                Toast.makeText(this, "Module joined", Toast.LENGTH_SHORT).show();
                                isJoined = true;
                                btnJoinLeaveModule.setText("Leave Module");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to join module", Toast.LENGTH_SHORT).show()
                            );
                });
    }

    private void leaveModule() {
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
                    isJoined = false;
                    btnJoinLeaveModule.setText("Join Module");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave module", Toast.LENGTH_SHORT).show()
                );
    }
}
