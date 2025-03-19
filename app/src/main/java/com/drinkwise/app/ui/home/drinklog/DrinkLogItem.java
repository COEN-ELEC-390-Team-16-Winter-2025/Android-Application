package com.drinkwise.app.ui.home.drinklog;

public class DrinkLogItem {
    private String drinkType;
    private Long calories;
    private String time;
    private double bacContribution;

    public DrinkLogItem(String drinkType, Long calories, String time ) {
        this.drinkType = drinkType;
        this.calories = calories;
        this.time = time;
        //this.bacContribution = bacContribution;
    }

    public String getDrinkType() {
        return drinkType;
    }

    public Long getCalories() {
        return calories;
    }

    public String getTime() {
        return time;
    }

    public double getBacContribution() {
        return bacContribution;
    }
}