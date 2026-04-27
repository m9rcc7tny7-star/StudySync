package com.morad.studdybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvEmptyMyModules;
    private TextView tvEmptyAvailableModules;

    private RecyclerView recyclerMyModules;
    private RecyclerView recyclerAvailableModules;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Module> myModuleList;
    private List<Module> availableModuleList;

    private ModuleAdapter myModuleAdapter;
    private ModuleAdapter availableModuleAdapter;

    private String currentUserId;
    private String currentUserSubjectArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmptyMyModules = findViewById(R.id.tvEmptyMyModules);
        tvEmptyAvailableModules = findViewById(R.id.tvEmptyAvailableModules);

        recyclerMyModules = findViewById(R.id.recyclerMyModules);
        recyclerAvailableModules = findViewById(R.id.recyclerAvailableModules);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        myModuleList = new ArrayList<>();
        availableModuleList = new ArrayList<>();

        myModuleAdapter = new ModuleAdapter(myModuleList, module -> {
            Intent intent = new Intent(HomeActivity.this, ModuleDetailsActivity.class);
            intent.putExtra("moduleCode", module.getModuleCode());
            intent.putExtra("moduleName", module.getModuleName());
            intent.putExtra("moduleDescription", module.getModuleDescription());
            startActivity(intent);
        });

        availableModuleAdapter = new ModuleAdapter(availableModuleList, module -> {
            Intent intent = new Intent(HomeActivity.this, ModuleInfoActivity.class);
            intent.putExtra("moduleCode", module.getModuleCode());
            intent.putExtra("moduleName", module.getModuleName());
            intent.putExtra("moduleDescription", module.getModuleDescription());
            intent.putExtra("subjectArea", module.getSubjectArea());
            startActivity(intent);
        });

        recyclerMyModules.setLayoutManager(new LinearLayoutManager(this));
        recyclerMyModules.setAdapter(myModuleAdapter);

        recyclerAvailableModules.setLayoutManager(new LinearLayoutManager(this));
        recyclerAvailableModules.setAdapter(availableModuleAdapter);

        setupBottomNavigation();
        loadUserProfileAndModules();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_add_module) {
                startActivity(new Intent(HomeActivity.this, AddModuleActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadUserProfileAndModules() {
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String fullName = documentSnapshot.getString("fullName");
                    currentUserSubjectArea = documentSnapshot.getString("subjectArea");

                    if (fullName != null) {
                        tvWelcome.setText("Welcome, " + fullName);
                    }

                    loadMyModulesAndAvailableModules();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadMyModulesAndAvailableModules() {
        db.collection("users")
                .document(currentUserId)
                .collection("modules")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myModuleList.clear();

                    Set<String> joinedModuleCodes = new HashSet<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Module module = document.toObject(Module.class);
                        myModuleList.add(module);
                        joinedModuleCodes.add(module.getModuleCode());
                    }

                    myModuleAdapter.notifyDataSetChanged();
                    updateMyModulesEmptyState();

                    loadAvailableModules(joinedModuleCodes);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load your modules", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadAvailableModules(Set<String> joinedModuleCodes) {
        availableModuleList.clear();

        db.collection("modules")
                .whereEqualTo("subjectArea", currentUserSubjectArea)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Module module = document.toObject(Module.class);

                        if (!joinedModuleCodes.contains(module.getModuleCode())) {
                            availableModuleList.add(module);
                        }
                    }

                    availableModuleAdapter.notifyDataSetChanged();
                    updateAvailableModulesEmptyState();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load available modules", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateMyModulesEmptyState() {
        if (myModuleList.isEmpty()) {
            recyclerMyModules.setVisibility(View.GONE);
            tvEmptyMyModules.setVisibility(View.VISIBLE);
        } else {
            recyclerMyModules.setVisibility(View.VISIBLE);
            tvEmptyMyModules.setVisibility(View.GONE);
        }
    }

    private void updateAvailableModulesEmptyState() {
        if (availableModuleList.isEmpty()) {
            recyclerAvailableModules.setVisibility(View.GONE);
            tvEmptyAvailableModules.setVisibility(View.VISIBLE);
        } else {
            recyclerAvailableModules.setVisibility(View.VISIBLE);
            tvEmptyAvailableModules.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentUserId != null && currentUserSubjectArea != null) {
            loadUserProfileAndModules();
        }
    }
}