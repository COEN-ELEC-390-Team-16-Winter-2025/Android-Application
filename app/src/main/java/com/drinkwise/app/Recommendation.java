package com.drinkwise.app;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Recommendation {
    private final String recommendationId;
    private String message;
    private final Timestamp timestamp;
    private final int drinkCount;
    private final long interval;
    private boolean resolved;
    private final String date;
    private final String time;

    public Recommendation(String recommendationId, int drinkCount, long interval, Timestamp timestamp) {
        this.recommendationId = recommendationId;
        this.timestamp = timestamp;
        this.drinkCount = drinkCount;
        this.interval = interval;
        this.resolved = false;


        if(drinkCount == 1) {
            this.message = "Enjoy your drink! Remember to pace yourself and stay hydrated";
        } else if(drinkCount == 2) {
            this.message = "This is your second drink! Consider having a glass of water next!";
        } else if(drinkCount == 3) {
            this.message = "Are you hungry? Keep your stomach full to prevent a hangover!";
        } else if(drinkCount == 4) {
            this.message = "Are you drinking enough water?";
        } else if(drinkCount == 5) {
            this.message = "Your judgment might be impaired. Take a break and try some breathing exercises!";
        } else if(drinkCount == 6) {
            this.message = "Remember to stay safe and avoid risky behaviors, your judgment might be impaired!";
        } else if(drinkCount == 7) {
            this.message = "Slurred speech and slower reaction time might affect you. Want to take a break and hydrate?";
        }else if(drinkCount == 8) {
            this.message = "Blurry vision and loss of coordination are setting in. Think about slowing down!";
        } else if(drinkCount == 9) {
            this.message = "You’re at a high risk for hangover and dehydration. Drink water and rest!";
        } else if(drinkCount == 10) {
            this.message = "Feeling confused? Dizzy? Want to vomit? Consider getting assistance and some rest";
        } else if(drinkCount > 10 ) {
            this.message = "";
        } else {
            this.message = "";
        }

        //Binge-drinking checks
        //2 drinks in 1 hour
        if(drinkCount >= 2 && interval < 3600000) {
            this.message = "You had two drinks in 1 hour, remember to pace yourself to make your drinking session more enjoyable";
        }
        //4 drinks in 2 hour
        if(drinkCount >= 4 && interval < 7200000) {
            this.message = "Feeling dizzy and nauseous? Hydrate yourself and try some breathing exercises to refocus!";
        }


        //
        Date temp = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        this.date = dateFormat.format(temp);
        this.time = timeFormat.format(temp);
    }

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

    public String getRecommendationId() {
        return recommendationId;
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


