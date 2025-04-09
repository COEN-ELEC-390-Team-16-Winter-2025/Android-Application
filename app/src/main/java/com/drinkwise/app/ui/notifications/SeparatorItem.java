package com.drinkwise.app.ui.notifications;

import com.google.firebase.Timestamp;

public class SeparatorItem implements NotificationItem {
    private String label;
    private long timestampMillis;
    private Timestamp Timestamp;

    public SeparatorItem() {
        this.label = "New messages above";
        this.timestampMillis = System.currentTimeMillis();
    }

    @Override
    public long getTimestampMillis() {
        return timestampMillis;
    }

    @Override
    public int getType() {
        return 3; // TYPE_SEPARATOR
    }

    @Override
    public String getMessage() {
        return label;
    }

    @Override
    public Timestamp getTimestamp() {
        return Timestamp;
    }

    public String getLabel() {
        return label;
    }

}