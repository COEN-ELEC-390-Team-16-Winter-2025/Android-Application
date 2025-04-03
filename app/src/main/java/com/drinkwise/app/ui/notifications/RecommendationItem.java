package com.drinkwise.app.ui.notifications;

import com.google.firebase.Timestamp;

public class RecommendationItem implements NotificationItem {
    private String message;
    private Timestamp timestamp;

    public RecommendationItem() {}
    public RecommendationItem(String message, Timestamp timestamp) {
        this.message = message;
        this.timestamp = (timestamp != null) ? timestamp : Timestamp.now();
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    @Override
    public long getTimestampMillis() {
        return timestamp != null? timestamp.toDate().getTime() : 0;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
