package com.drinkwise.app.ui.notifications;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class AlertItem implements NotificationItem {

    private String alertType;
    private String message;
    private Timestamp timestamp;
    private boolean resolved;
    private String safetyLevel;
    private String escalationLevel;
    private double bacValue;

    public AlertItem() {

    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    @Override
    public long getTimestampMillis() {
        return timestamp != null ? timestamp.toDate().getTime() : 0;
    }

    @Override
    public int getType() {
        return 2; // Alert type for adapter
    }
    @PropertyName("Message")
    public void setMessage(String message) {
        this.message = message;
    }
    @PropertyName("Message")
    public String getMessage() {
        return message;
    }



    @PropertyName("Timestamp")
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("Timestamp")
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @PropertyName("SafetyLevel")
    public void setSafetyLevel(String safetyLevel) {
        this.safetyLevel = safetyLevel;
    }

    @PropertyName("SafetyLevel")
    public String getSafetyLevel() {
        return safetyLevel != null ? safetyLevel : "Unknown";
    }



    public double getBacValue() {
        return bacValue;
    }

    public void setBacValue(double bacValue) {
        this.bacValue = bacValue;
    }
}
