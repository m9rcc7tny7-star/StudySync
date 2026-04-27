package com.morad.studdybuddy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StudySessionsActivity extends AppCompatActivity {

    private TextView tvSessionTitle;
    private TextView tvEmptySessions;

    private EditText etSessionTopic, etSessionDate, etSessionTime, etSessionLocation, etMeetingLink;
    private RadioGroup radioGroupMode;
    private RadioButton rbInPerson, rbOnline;
    private MaterialButton btnCreateSession;
    private RecyclerView recyclerSessions;

    private FirebaseFirestore db;

    private List<StudySession> sessionList;
    private List<String> sessionIds;
    private StudySessionAdapter sessionAdapter;

    private String moduleCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_sessions);

        tvSessionTitle = findViewById(R.id.tvSessionTitle);
        tvEmptySessions = findViewById(R.id.tvEmptySessions);

        etSessionTopic = findViewById(R.id.etSessionTopic);
        etSessionDate = findViewById(R.id.etSessionDate);
        etSessionTime = findViewById(R.id.etSessionTime);
        etSessionLocation = findViewById(R.id.etSessionLocation);
        etMeetingLink = findViewById(R.id.etMeetingLink);
        radioGroupMode = findViewById(R.id.radioGroupMode);
        rbInPerson = findViewById(R.id.rbInPerson);
        rbOnline = findViewById(R.id.rbOnline);
        btnCreateSession = findViewById(R.id.btnCreateSession);
        recyclerSessions = findViewById(R.id.recyclerSessions);

        db = FirebaseFirestore.getInstance();

        moduleCode = getIntent().getStringExtra("moduleCode");
        tvSessionTitle.setText(moduleCode + " Study Sessions");

        sessionList = new ArrayList<>();
        sessionIds = new ArrayList<>();

        sessionAdapter = new StudySessionAdapter(sessionList, sessionIds, (sessionId, session) -> {
            Intent intent = new Intent(StudySessionsActivity.this, SessionDetailsActivity.class);
            intent.putExtra("moduleCode", moduleCode);
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("topic", session.getTopic());
            intent.putExtra("date", session.getDate());
            intent.putExtra("time", session.getTime());
            intent.putExtra("mode", session.getMode());
            intent.putExtra("location", session.getLocation());
            intent.putExtra("meetingLink", session.getMeetingLink());
            startActivity(intent);
        });

        recyclerSessions.setLayoutManager(new LinearLayoutManager(this));
        recyclerSessions.setAdapter(sessionAdapter);

        etSessionDate.setOnClickListener(v -> showDatePicker());
        etSessionTime.setOnClickListener(v -> showTimePicker());

        radioGroupMode.setOnCheckedChangeListener((group, checkedId) -> updateModeFields());
        updateModeFields();

        loadSessions();

        btnCreateSession.setOnClickListener(v -> {
            String topic = etSessionTopic.getText().toString().trim();
            String date = etSessionDate.getText().toString().trim();
            String time = etSessionTime.getText().toString().trim();
            String mode = rbOnline.isChecked() ? "Online" : "In Person";
            String location = etSessionLocation.getText().toString().trim();
            String meetingLink = etMeetingLink.getText().toString().trim();

            if (topic.isEmpty()) {
                Toast.makeText(this, "Enter session topic", Toast.LENGTH_SHORT).show();
                return;
            }

            if (date.isEmpty()) {
                Toast.makeText(this, "Select session date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (time.isEmpty()) {
                Toast.makeText(this, "Select session time", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode.equals("In Person") && location.isEmpty()) {
                Toast.makeText(this, "Enter session location", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode.equals("Online") && meetingLink.isEmpty()) {
                Toast.makeText(this, "Enter meeting link", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mode.equals("In Person")) {
                meetingLink = "";
            } else {
                location = "";
            }

            StudySession session = new StudySession(topic, date, time, mode, location, meetingLink);

            db.collection("modules")
                    .document(moduleCode)
                    .collection("sessions")
                    .add(session)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Session created", Toast.LENGTH_SHORT).show();

                        etSessionTopic.setText("");
                        etSessionDate.setText("");
                        etSessionTime.setText("");
                        etSessionLocation.setText("");
                        etMeetingLink.setText("");
                        rbInPerson.setChecked(true);
                        updateModeFields();

                        loadSessions();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error creating session", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void updateModeFields() {
        if (rbOnline.isChecked()) {
            etSessionLocation.setEnabled(false);
            etSessionLocation.setText("");
            etSessionLocation.setVisibility(EditText.GONE);

            etMeetingLink.setEnabled(true);
            etMeetingLink.setVisibility(EditText.VISIBLE);
        } else {
            etMeetingLink.setEnabled(false);
            etMeetingLink.setText("");
            etMeetingLink.setVisibility(EditText.GONE);

            etSessionLocation.setEnabled(true);
            etSessionLocation.setVisibility(EditText.VISIBLE);
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                    );
                    etSessionDate.setText(formattedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String formattedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            selectedHour,
                            selectedMinute
                    );
                    etSessionTime.setText(formattedTime);
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    private void loadSessions() {
        db.collection("modules")
                .document(moduleCode)
                .collection("sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sessionList.clear();
                    sessionIds.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        StudySession session = doc.toObject(StudySession.class);
                        sessionList.add(session);
                        sessionIds.add(doc.getId());
                    }

                    sessionAdapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load sessions", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (sessionList.isEmpty()) {
            recyclerSessions.setVisibility(View.GONE);
            tvEmptySessions.setVisibility(View.VISIBLE);
        } else {
            recyclerSessions.setVisibility(View.VISIBLE);
            tvEmptySessions.setVisibility(View.GONE);
        }
    }
}