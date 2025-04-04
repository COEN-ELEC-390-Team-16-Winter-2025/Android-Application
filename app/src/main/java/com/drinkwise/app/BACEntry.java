package com.drinkwise.app;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// BACEntry represents a single Blood Alcohol Concentration (BAC) reading.
// It stores the BAC value, its timestamp, and additional formatted date/time and status information.
public class BACEntry {
    // The BAC reading
    private final double bac;
    // The timestamp when the BAC was recorded (as a Firebase Timestamp)
    private final Timestamp timestamp;
    // A status string derived from the BAC value ( "Safe", "Impaired")
    private final String status;
    // Formatted date string derived from the timestamp ("2025-04-01")
    private final String date;
    // Formatted time string derived from the timestamp ("14:30:15")
    private final String time;

    // Constructor: Initializes a BACEntry with a BAC value and a timestamp.
    public BACEntry(double bac, Timestamp timestamp) {
        this.bac = bac;
        this.timestamp = timestamp;

        // Determine the status based on the BAC value.
        // These conditions define the state categories:
        // "Safe", "Mild Impairment", "Impaired", "High Impairment", "Severe Impairment", or "Medical Emergency".
        if (bac <= 0.02) {
            this.status = "Safe";
        } else if (bac > 0.02 && bac <= 0.05) {
            this.status = "Mild Impairment";
        } else if (bac > 0.05 && bac <= 0.08) {
            this.status = "Impaired";
        } else if (bac > 0.08 && bac <= 0.15) {
            this.status = "High Impairment";
        } else if (bac > 0.15 && bac <= 0.30) {
            this.status = "Severe Impairment";
        } else {
            this.status = "Medical Emergency";
        }

        // Convert the Firebase Timestamp to a Date object.
        Date temp = timestamp.toDate();

        // Create SimpleDateFormat objects for date and time formatting.
        SimpleDateFormat date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat time1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        // Format the date and time from the Date object.
        date = date1.format(temp);
        time = time1.format(temp);
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public double getBac() {
        return bac;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setStatus(String s) {
        // later
    }
}