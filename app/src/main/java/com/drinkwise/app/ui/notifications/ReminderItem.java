package com.drinkwise.app.ui.notifications;

import com.google.firebase.Timestamp;

// ReminderItem represents a single reminder with its details
public class ReminderItem implements NotificationItem {

    // The type of the reminder like "BAC Recheck", "Late Night Check"
    private String reminderType;
    // the message to be displayed for this reminder
    private String message;
    // The timestamp when the reminder was created or scheduled
    private Timestamp timestamp;
    // The interval in minutes after which the reminder should be checked again
    private int intervalMinutes;
    // The current status of the reminder for example "active" or "resolved"
    private String status;
    // The escalation level of the reminder (4 levels : "Low", "Medium", "High", "Emergency")
    private String escalation;
    //If the reminder has been read by user or not
    private Boolean resolved;
    //Id of the reminderitem
    private String id;


    // deserialization
    public ReminderItem() {}

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    @Override
    public long getTimestampMillis() {
        return timestamp != null ? timestamp.toDate().getTime() : 0;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
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

    public int getInterval() {
        return intervalMinutes;
    }
}