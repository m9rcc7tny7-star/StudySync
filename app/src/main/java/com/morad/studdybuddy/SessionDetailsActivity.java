package com.morad.studdybuddy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SessionDetailsActivity extends AppCompatActivity {

    private TextView tvTopic, tvDetails, tvAttendees, tvMeetingLabel, tvMeetingLink;
    private Button btnAttend;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String moduleCode;
    private String sessionId;
    private String topic;
    private String date;
    private String time;
    private String mode;
    private String location;
    private String meetingLink;

    private String currentUserId;
    private boolean isAttending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        tvTopic = findViewById(R.id.tvTopic);
        tvDetails = findViewById(R.id.tvDetails);
        tvAttendees = findViewById(R.id.tvAttendees);
        tvMeetingLabel = findViewById(R.id.tvMeetingLabel);
        tvMeetingLink = findViewById(R.id.tvMeetingLink);
        btnAttend = findViewById(R.id.btnAttend);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");
        sessionId = getIntent().getStringExtra("sessionId");
        topic = getIntent().getStringExtra("topic");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        mode = getIntent().getStringExtra("mode");
        location = getIntent().getStringExtra("location");
        meetingLink = getIntent().getStringExtra("meetingLink");

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        tvTopic.setText(topic);

        if ("Online".equals(mode)) {
            tvDetails.setText(date + " • " + time + "\nMode: Online");

            if (meetingLink != null && !meetingLink.trim().isEmpty()) {
                tvMeetingLabel.setVisibility(View.VISIBLE);
                tvMeetingLink.setVisibility(View.VISIBLE);
                tvMeetingLink.setText(meetingLink);

                tvMeetingLink.setOnClickListener(v -> openMeetingLink(meetingLink));
            }
        } else {
            tvDetails.setText(date + " • " + time + "\nMode: In Person\nLocation: " + location);
            tvMeetingLabel.setVisibility(View.GONE);
            tvMeetingLink.setVisibility(View.GONE);
        }

        checkAttendance();
        loadAttendeeCount();

        btnAttend.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isAttending) {
                showLeaveSessionDialog();
            } else {
                attendSession();
            }
        });
    }

    private void showLeaveSessionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Session")
                .setMessage("Are you sure you want to leave this study session?")
                .setPositiveButton("Leave", (dialog, which) -> leaveSession())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openMeetingLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            Toast.makeText(this, "No meeting link available", Toast.LENGTH_SHORT).show();
            return;
        }

        link = link.trim();

        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            link = "https://" + link;
        }

        if (!Patterns.WEB_URL.matcher(link).matches()) {
            Toast.makeText(this, "Invalid meeting link", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app can open this link", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open link", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAttendance() {
        if (currentUserId == null) return;

        db.collection("modules")
                .document(moduleCode)
                .collection("sessions")
                .document(sessionId)
                .collection("attendees")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isAttending = documentSnapshot.exists();
                    if (isAttending) {
                        btnAttend.setText("Leave Session");
                    } else {
                        btnAttend.setText("Attend");
                    }
                });
    }

    private void loadAttendeeCount() {
        db.collection("modules")
                .document(moduleCode)
                .collection("sessions")
                .document(sessionId)
                .collection("attendees")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        tvAttendees.setText("Attendees: " + queryDocumentSnapshots.size()));
    }

    private void attendSession() {
        db.collection("modules")
                .document(moduleCode)
                .collection("sessions")
                .document(sessionId)
                .collection("attendees")
                .document(currentUserId)
                .set(new Member(currentUserId))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "You are attending", Toast.LENGTH_SHORT).show();
                    isAttending = true;
                    btnAttend.setText("Leave Session");
                    loadAttendeeCount();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to attend", Toast.LENGTH_SHORT).show()
                );
    }

    private void leaveSession() {
        db.collection("modules")
                .document(moduleCode)
                .collection("sessions")
                .document(sessionId)
                .collection("attendees")
                .document(currentUserId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "You left the session", Toast.LENGTH_SHORT).show();
                    isAttending = false;
                    btnAttend.setText("Attend");
                    loadAttendeeCount();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave session", Toast.LENGTH_SHORT).show()
                );
    }
}