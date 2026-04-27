package com.morad.studdybuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudyGroupDetailsActivity extends AppCompatActivity {

    private TextView tvGroupName, tvModuleCode;
    private Button btnJoinGroup, btnSend;
    private EditText etMessage;

    private RecyclerView recyclerMembers, recyclerMessages;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String moduleCode;
    private String groupId;
    private String groupName;
    private String currentUserId;
    private String currentUserName;

    private List<String> memberList;
    private MemberAdapter memberAdapter;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    private boolean isJoined = false;

    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_group_details);

        tvGroupName = findViewById(R.id.tvGroupName);
        tvModuleCode = findViewById(R.id.tvModuleCode);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);

        recyclerMembers = findViewById(R.id.recyclerMembers);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        tvGroupName.setText(groupName);
        tvModuleCode.setText("Module: " + moduleCode);

        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(memberList);

        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));
        recyclerMembers.setAdapter(memberAdapter);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);

        LinearLayoutManager messagesLayoutManager = new LinearLayoutManager(this);
        recyclerMessages.setLayoutManager(messagesLayoutManager);
        recyclerMessages.setAdapter(messageAdapter);

        loadCurrentUserName();
        checkMembership();
        loadMembers();

        btnJoinGroup.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isJoined) {
                showLeaveGroupDialog();
            } else {
                joinGroup();
            }
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void showLeaveGroupDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this study group?")
                .setPositiveButton("Leave", (dialog, which) -> leaveGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadCurrentUserName() {
        if (currentUserId == null) return;

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("fullName");
                    }
                });
    }

    private void checkMembership() {
        if (currentUserId == null) return;

        db.collection("modules")
                .document(moduleCode)
                .collection("groups")
                .document(groupId)
                .collection("members")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isJoined = documentSnapshot.exists();
                    updateChatAccess();
                });
    }

    private void updateChatAccess() {
        if (isJoined) {
            btnJoinGroup.setText("Leave Group");
            etMessage.setEnabled(true);
            btnSend.setEnabled(true);
            recyclerMessages.setVisibility(View.VISIBLE);
            startMessagesListener();
        } else {
            btnJoinGroup.setText("Join Group");
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);
            recyclerMessages.setVisibility(View.GONE);
            stopMessagesListener();
            messageList.clear();
            messageAdapter.notifyDataSetChanged();
        }
    }

    private void joinGroup() {
        db.collection("modules")
                .document(moduleCode)
                .collection("groups")
                .document(groupId)
                .collection("members")
                .document(currentUserId)
                .set(new Member(currentUserId))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Joined group", Toast.LENGTH_SHORT).show();
                    isJoined = true;
                    updateChatAccess();
                    loadMembers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to join group", Toast.LENGTH_SHORT).show()
                );
    }

    private void leaveGroup() {
        db.collection("modules")
                .document(moduleCode)
                .collection("groups")
                .document(groupId)
                .collection("members")
                .document(currentUserId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Left group", Toast.LENGTH_SHORT).show();
                    isJoined = false;
                    updateChatAccess();
                    loadMembers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave group", Toast.LENGTH_SHORT).show()
                );
    }

    private void sendMessage() {
        if (!isJoined) {
            Toast.makeText(this, "Join the group to send messages", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etMessage.getText().toString().trim();

        if (text.isEmpty()) return;

        Message message = new Message(text, currentUserName, currentUserId, System.currentTimeMillis());

        db.collection("modules")
                .document(moduleCode)
                .collection("groups")
                .document(groupId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(unused -> etMessage.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Message failed", Toast.LENGTH_SHORT).show()
                );
    }

    private void startMessagesListener() {
        stopMessagesListener();

        messagesListener = db.collection("modules")
                .document(moduleCode)
                .collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    messageList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Message message = doc.toObject(Message.class);
                        messageList.add(message);
                    }

                    messageAdapter.notifyDataSetChanged();

                    if (!messageList.isEmpty()) {
                        recyclerMessages.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void stopMessagesListener() {
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
    }

    private void loadMembers() {
        db.collection("modules")
                .document(moduleCode)
                .collection("groups")
                .document(groupId)
                .collection("members")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    memberList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String name = userDoc.getString("fullName");

                                    if (name != null) {
                                        memberList.add(name);
                                    } else {
                                        memberList.add(userId);
                                    }

                                    memberAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMessagesListener();
    }
}