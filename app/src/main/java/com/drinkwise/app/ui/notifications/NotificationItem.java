package com.drinkwise.app.ui.notifications;

import com.google.firebase.Timestamp;

public interface NotificationItem {
    long getTimestampMillis();    //used for sorting
    int getType();
    String getMessage();

    Timestamp getTimestamp();
}
