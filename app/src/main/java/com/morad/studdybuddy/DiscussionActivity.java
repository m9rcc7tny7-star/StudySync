package com.morad.studdybuddy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DiscussionActivity extends AppCompatActivity {

    private TextView tvDiscussionTitle;
    private RecyclerView recyclerDiscussionMessages;
    private EditText etDiscussionMessage;
    private Button btnSendDiscussion;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String moduleCode;
    private String currentUserId;
    private String currentUserName;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    private ListenerRegistration discussionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        tvDiscussionTitle = findViewById(R.id.tvDiscussionTitle);
        recyclerDiscussionMessages = findViewById(R.id.recyclerDiscussionMessages);
        etDiscussionMessage = findViewById(R.id.etDiscussionMessage);
        btnSendDiscussion = findViewById(R.id.btnSendDiscussion);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        tvDiscussionTitle.setText(moduleCode + " Discussion");

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerDiscussionMessages.setLayoutManager(layoutManager);
        recyclerDiscussionMessages.setAdapter(messageAdapter);

        loadCurrentUserName();
        startDiscussionListener();

        btnSendDiscussion.setOnClickListener(v -> sendDiscussionMessage());
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

    private void sendDiscussionMessage() {
        String text = etDiscussionMessage.getText().toString().trim();

        if (text.isEmpty()) return;

        if (currentUserId == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(text, currentUserName, currentUserId, System.currentTimeMillis());

        db.collection("modules")
                .document(moduleCode)
                .collection("discussion")
                .add(message)
                .addOnSuccessListener(unused -> etDiscussionMessage.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Message failed", Toast.LENGTH_SHORT).show()
                );
    }

    private void startDiscussionListener() {
        stopDiscussionListener();

        discussionListener = db.collection("modules")
                .document(moduleCode)
                .collection("discussion")
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
                        recyclerDiscussionMessages.smoothScrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void stopDiscussionListener() {
        if (discussionListener != null) {
            discussionListener.remove();
            discussionListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDiscussionListener();
    }
}
