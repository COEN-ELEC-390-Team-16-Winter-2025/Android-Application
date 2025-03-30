package com.drinkwise.app;

import com.google.firebase.firestore.SnapshotMetadata;

public class EmergencyContact {

    private String name;
    private String phone_no;
    private String email;
    private String relationship;

    public EmergencyContact(String name, String phone_no, String email, String relationship) {
        this.name = " " + name;
        this.phone_no = " " + phone_no;
        this.email = " " + email;
        this.relationship = " " + relationship;
    }

    public String getName() {
        return name;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getEmail() {
        return email;
    }
}
