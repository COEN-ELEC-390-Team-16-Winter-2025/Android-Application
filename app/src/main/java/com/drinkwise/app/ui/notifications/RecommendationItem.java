package com.drinkwise.app.ui.notifications;

import com.google.firebase.Timestamp;

public class RecommendationItem implements NotificationItem {
    private String Message;
    private Timestamp Timestamp;
    private Boolean Resolved;
    private String id;

    public RecommendationItem() {}
    public RecommendationItem(String Message, Timestamp Timestamp) {
        this.Message = Message;
        this.Timestamp = (Timestamp != null) ? Timestamp : Timestamp.now();
    }

    public Timestamp getTimestamp() {
        return Timestamp;
    }

    public Boolean getResolved() {
        return Resolved;
    }

    public void setResolved(Boolean resolved) {
        this.Resolved = resolved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getTimestampMillis() {
        return Timestamp != null? Timestamp.toDate().getTime() : 0;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String getMessage() {
        return Message;
    }


}
