package com.drinkwise.app;

public class EmergencyContact {
    private String id; // Firestore document id
    private String name;
    private String phone_no;
    private String email;
    private String relationship;

    // No-argument constructor required for Firestore
    public EmergencyContact() { }

    public EmergencyContact(String id, String name, String phone_no, String email, String relationship) {
        this.id = id;
        this.name = name;
        this.phone_no = phone_no;
        this.email = email;
        this.relationship = relationship;
    }

    // Constructor without id (for new contacts)
    public EmergencyContact(String name, String phone_no, String email, String relationship) {
        this.name = name;
        this.phone_no = phone_no;
        this.email = email;
        this.relationship = relationship;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public String getPhone_no() {
        return phone_no;
    }
    public String getEmail() {
        return email;
    }
    public String getRelationship() {
        return relationship;
    }
}