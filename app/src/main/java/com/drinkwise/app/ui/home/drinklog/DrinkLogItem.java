package com.drinkwise.app.ui.home.drinklog;

// This class represents a single drink log entry
public class DrinkLogItem {
    // The type of drink (example : "Beer", "Wine")
    private String drinkType;
    // The number of calories in the drink (long so that it allows null values)
    private Long calories;
    // The time when the drink was consumed
    private String time;
    // The contribution of this drink to the overall BAC
    private Double bacContribution;

    // constructor
    public DrinkLogItem() {}

    public DrinkLogItem(String drinkType, Long calories, String time, Double bacContribution) {
        this.drinkType = drinkType;
        this.calories = calories;
        this.time = time;
        this.bacContribution = bacContribution;
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

    public Double getBacContribution() {
        return bacContribution;
    }

    public void setDrinkType(String drinkType) {
        this.drinkType = drinkType;
    }

    public void setCalories(Long calories) {
        this.calories = calories;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setBacContribution(Double bacContribution) {
        this.bacContribution = bacContribution;
    }

}