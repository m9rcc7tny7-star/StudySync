package com.morad.studdybuddy;

public class Message {

    private String text;
    private String senderName;
    private String senderId;
    private long timestamp;

    public Message() {
        // Required empty constructor for Firestore
    }

    public Message(String text, String senderName, String senderId, long timestamp) {
        this.text = text;
        this.senderName = senderName;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderId() {
        return senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
