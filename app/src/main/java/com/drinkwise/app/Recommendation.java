package com.drinkwise.app;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// This class represents a recommendation based on the number of drinks logged,
// the time interval between drinks, and the timestamp of the log.
public class Recommendation {
    // The recommendation message
    private String message;
    // Timestamp when the recommendation was generated
    private final Timestamp timestamp;
    // Total number of drinks logged
    private final int drinkCount;
    // Time interval between the last two drinks (in milliseconds)
    private final long interval;
    // Indicates whether the recommendation has been resolved
    private boolean resolved;
    // Formatted date string from the timestamp
    private final String date;
    // Formatted time string from the timestamp
    private final String time;

    // Constructor that calculates the recommendation based on drinkCount, interval, and timestamp.
    public Recommendation(int drinkCount, long interval, Timestamp timestamp) {
        this.timestamp = timestamp;
        this.drinkCount = drinkCount;
        this.interval = interval;
        this.resolved = false;

        // Set the recommendation message based on the number of drinks logged.
        if (drinkCount == 1) {
            this.message = "Enjoy your drink! Remember to pace yourself and stay hydrated";
        } else if (drinkCount == 2) {
            this.message = "This is your second drink! Consider having a glass of water next!";
        } else if (drinkCount == 3) {
            this.message = "Are you hungry? Keep your stomach full to prevent a hangover!";
        } else if (drinkCount == 4) {
            this.message = "Are you drinking enough water?";
        } else if (drinkCount == 5) {
            this.message = "Your judgment might be impaired. Take a break and try some breathing exercises!";
        } else if (drinkCount == 6) {
            this.message = "Remember to stay safe and avoid risky behaviors, your judgment might be impaired!";
        } else if (drinkCount == 7) {
            this.message = "Slurred speech and slower reaction time might affect you. Want to take a break and hydrate?";
        } else if (drinkCount == 8) {
            this.message = "Blurry vision and loss of coordination are setting in. Think about slowing down!";
        } else if (drinkCount == 9) {
            this.message = "You’re at a high risk for hangover and dehydration. Drink water and rest!";
        } else if (drinkCount == 10) {
            this.message = "Feeling confused? Dizzy? Want to vomit? Consider getting assistance and some rest";
        } else if (drinkCount > 10 ) {
            this.message = "You're still awake? Inconceivable.";
        } else {
            this.message = "Default";
        }

        // The following commented code is for binge-drinking checks (not active)
        // 2 drinks in 1 hour
        // if(drinkCount >= 2 && interval < 3600000) { ... }
        // 4 drinks in 2 hours
        // if(drinkCount >= 4 && interval < 7200000) { ... }

        // Convert the Firebase Timestamp to a Date object.
        Date temp = timestamp.toDate();
        // Format the date as "yyyy-MM-dd"
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // Format the time as "HH:mm:ss"
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        // Save the formatted date and time.
        this.date = dateFormat.format(temp);
        this.time = timeFormat.format(temp);
    }

    // Returns whether the recommendation is resolved.
    public boolean isResolved() {
        return resolved;
    }

    public int getDrinkCount() {
        return drinkCount;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public long getInterval() {
        return interval;
    }
}