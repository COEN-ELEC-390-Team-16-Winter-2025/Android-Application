package com.drinkwise.app.ui.notifications;

import com.google.firebase.Timestamp;

public class ReminderItem {
    private String reminderType;
    private String message;
    private Timestamp timestamp;
    private int intervalMinutes;
    private String status;
    private String escalation;

    public ReminderItem() {}

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getEscalation() {
        return escalation;
    }
    public void setEscalation(String escalation) {
        this.escalation = escalation;
    }
}
