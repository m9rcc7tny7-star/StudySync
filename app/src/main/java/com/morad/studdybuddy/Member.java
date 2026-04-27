package com.morad.studdybuddy;

public class Member {

    private String userId;

    public Member() {
        // Required empty constructor for Firestore
    }

    public Member(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}