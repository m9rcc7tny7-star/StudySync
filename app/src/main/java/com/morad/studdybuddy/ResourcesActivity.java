package com.morad.studdybuddy;

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

public class ResourcesActivity extends AppCompatActivity {

    private TextView tvResourcesTitle;
    private TextView tvEmptyResources;

    private EditText etResourceTitle, etResourceLink, etResourceDescription;
    private MaterialButton btnAddResource;
    private RecyclerView recyclerResources;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private List<ResourceItem> resourceList;
    private ResourceAdapter resourceAdapter;

    private String moduleCode;
    private String currentUserName = "Unknown User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        tvResourcesTitle = findViewById(R.id.tvResourcesTitle);
        tvEmptyResources = findViewById(R.id.tvEmptyResources);

        etResourceTitle = findViewById(R.id.etResourceTitle);
        etResourceLink = findViewById(R.id.etResourceLink);
        etResourceDescription = findViewById(R.id.etResourceDescription);
        btnAddResource = findViewById(R.id.btnAddResource);
        recyclerResources = findViewById(R.id.recyclerResources);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");
        tvResourcesTitle.setText(moduleCode + " Resources");

        resourceList = new ArrayList<>();

        recyclerResources.setLayoutManager(new LinearLayoutManager(this));

        loadCurrentUserNameAndSetup();

        btnAddResource.setOnClickListener(v -> {
            String title = etResourceTitle.getText().toString().trim();
            String link = etResourceLink.getText().toString().trim();
            String description = etResourceDescription.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Enter resource title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (link.isEmpty()) {
                Toast.makeText(this, "Enter resource link", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "Enter resource description", Toast.LENGTH_SHORT).show();
                return;
            }

            ResourceItem resource = new ResourceItem(title, link, description, currentUserName);

            db.collection("modules")
                    .document(moduleCode)
                    .collection("resources")
                    .add(resource)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Resource added", Toast.LENGTH_SHORT).show();

                        etResourceTitle.setText("");
                        etResourceLink.setText("");
                        etResourceDescription.setText("");

                        loadResources();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error adding resource", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadCurrentUserNameAndSetup() {
        if (mAuth.getCurrentUser() == null) {
            setupAdapter();
            loadResources();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            currentUserName = fullName;
                        }
                    }

                    setupAdapter();
                    loadResources();
                })
                .addOnFailureListener(e -> {
                    setupAdapter();
                    loadResources();
                });
    }

    private void setupAdapter() {
        resourceAdapter = new ResourceAdapter(resourceList, currentUserName, resource -> {
            if (resource.getId() == null || resource.getId().isEmpty()) {
                Toast.makeText(this, "Cannot delete this resource", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("modules")
                    .document(moduleCode)
                    .collection("resources")
                    .document(resource.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Resource deleted", Toast.LENGTH_SHORT).show();
                        loadResources();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to delete resource", Toast.LENGTH_SHORT).show()
                    );
        });

        recyclerResources.setAdapter(resourceAdapter);
    }

    private void loadResources() {
        db.collection("modules")
                .document(moduleCode)
                .collection("resources")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    resourceList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ResourceItem resource = doc.toObject(ResourceItem.class);
                        resource.setId(doc.getId());
                        resourceList.add(resource);
                    }

                    resourceAdapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load resources", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (resourceList.isEmpty()) {
            recyclerResources.setVisibility(View.GONE);
            tvEmptyResources.setVisibility(View.VISIBLE);
        } else {
            recyclerResources.setVisibility(View.VISIBLE);
            tvEmptyResources.setVisibility(View.GONE);
        }
    }
}