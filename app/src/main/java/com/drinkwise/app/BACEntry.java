package com.drinkwise.app;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// BACEntry represents a single BAC
public class BACEntry {
    private final double bac;
    private final Timestamp timestamp;
    private final String status;
    // example "2025-04-01"
    private final String date;
    // example "14:30:15"
    private final String time;

    // constructor
    public BACEntry(double bac, Timestamp timestamp) {
        this.bac = bac;
        this.timestamp = timestamp;

        // status based on the BAC
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

        Date temp = timestamp.toDate();

        SimpleDateFormat date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat time1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        //format
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