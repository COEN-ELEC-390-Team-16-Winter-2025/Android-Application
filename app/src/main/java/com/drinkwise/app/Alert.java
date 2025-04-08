package com.drinkwise.app;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Alert {

    private final double bac;
    private final Timestamp timestamp;
    private final String safetyLevel;
    private final String escalationLevel;
    private boolean resolved;

    private final String message;

    private final String date;
    private final String time;

    public Alert(double bac, Timestamp timestamp) {
        this.bac = bac;
        this.timestamp = timestamp;
        this.resolved = false;


        //Instantiates the status and message based on a bac reading
        if (bac <= 0.02) {
            this.safetyLevel = "Safe";
            this.message = "You're sober. Good job!";
            this.escalationLevel = "None";
        } else if (bac > 0.02 && bac <= 0.05) {
            this.safetyLevel = "Mild Impairment";
            this.message = "Your BAC is rising. Be aware. Judgment may be slightly affected.";
            this.escalationLevel = "Low";
        } else if (bac > 0.05 && bac <= 0.08) {
            this.safetyLevel = "Impaired";
            this.message = "Legally impaired. Reaction time and coordination are reduced. Do not drive.";
            this.escalationLevel = "Medium";
        } else if (bac > 0.08 && bac <= 0.15) {
            this.safetyLevel = "High Impairment";
            this.message = "Significant impairment. Poor coordination, judgment and reaction time. Avoid driving and tasks requiring focus.";
            this.escalationLevel = "High";
        } else if (bac > 0.15 && bac <= 0.30) {
            this.safetyLevel = "Severe Impairment";
            this.message = "Severe intoxication. Confusion, nausea, and risk of blacking out. You may need help from a friend or medical professional.";
            this.escalationLevel = "Urgent";
        } else {
            this.safetyLevel = "Medical Emergency";
            this.message = "Critical risk! Danger of unconsciousness, vomiting, or respiratory failure. Seek medical attention NOW.";
            this.escalationLevel = "Emergency";
        }

        Date temp = timestamp.toDate();

        SimpleDateFormat date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat time1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        date = date1.format(temp);
        time = time1.format(temp);
    }

    public double getBac() {
        return bac;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getSafetyLevel() {
        return safetyLevel;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getEscalationLevel() {
        return escalationLevel;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved){
        this.resolved = resolved;
    }

}