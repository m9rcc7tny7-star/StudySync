package com.morad.studdybuddy;

public class StudySession {

    private String topic;
    private String date;
    private String time;
    private String mode;
    private String location;
    private String meetingLink;

    public StudySession() {
        // Required empty constructor for Firestore
    }

    public StudySession(String topic, String date, String time, String mode, String location, String meetingLink) {
        this.topic = topic;
        this.date = date;
        this.time = time;
        this.mode = mode;
        this.location = location;
        this.meetingLink = meetingLink;
    }

    public String getTopic() {
        return topic;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getMode() {
        return mode;
    }

    public String getLocation() {
        return location;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }
}
