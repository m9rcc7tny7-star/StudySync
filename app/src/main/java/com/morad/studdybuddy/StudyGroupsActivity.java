package com.morad.studdybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudyGroupsActivity extends AppCompatActivity {

    private TextView tvGroupTitle;
    private TextView tvEmptyGroups;
    private EditText etGroupName;
    private MaterialButton btnCreateGroup;
    private RecyclerView recyclerGroups;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private List<StudyGroup> groupList;
    private List<String> groupIds;
    private StudyGroupAdapter groupAdapter;

    private String moduleCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_groups);

        tvGroupTitle = findViewById(R.id.tvGroupTitle);
        tvEmptyGroups = findViewById(R.id.tvEmptyGroups);
        etGroupName = findViewById(R.id.etGroupName);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        recyclerGroups = findViewById(R.id.recyclerGroups);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");
        tvGroupTitle.setText(moduleCode + " Study Groups");

        groupList = new ArrayList<>();
        groupIds = new ArrayList<>();

        groupAdapter = new StudyGroupAdapter(groupList, groupIds, (groupId, group) -> {
            Intent intent = new Intent(StudyGroupsActivity.this, StudyGroupDetailsActivity.class);
            intent.putExtra("moduleCode", moduleCode);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", group.getName());
            startActivity(intent);
        });

        recyclerGroups.setLayoutManager(new LinearLayoutManager(this));
        recyclerGroups.setAdapter(groupAdapter);

        loadGroups();

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            StudyGroup group = new StudyGroup(groupName, userId);

            db.collection("modules")
                    .document(moduleCode)
                    .collection("groups")
                    .add(group)
                    .addOnSuccessListener(documentReference -> {
                        documentReference.collection("members")
                                .document(userId)
                                .set(new Member(userId))
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Group created and joined", Toast.LENGTH_SHORT).show();
                                    etGroupName.setText("");
                                    loadGroups();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Group created, but joining failed", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error creating group", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadGroups() {
        db.collection("modules")
                .document(moduleCode)
                .collection("groups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    groupList.clear();
                    groupIds.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        StudyGroup group = doc.toObject(StudyGroup.class);
                        groupList.add(group);
                        groupIds.add(doc.getId());
                    }

                    groupAdapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load groups", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (groupList.isEmpty()) {
            recyclerGroups.setVisibility(View.GONE);
            tvEmptyGroups.setVisibility(View.VISIBLE);
        } else {
            recyclerGroups.setVisibility(View.VISIBLE);
            tvEmptyGroups.setVisibility(View.GONE);
        }
    }
}