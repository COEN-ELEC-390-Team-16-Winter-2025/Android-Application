package com.drinkwise.app.ui.home.drinklog;

// This class represents a single drink log entry, including details such as
// the type of drink, calories, time of consumption, and its contribution to the BAC.
public class DrinkLogItem {
    // The type of drink (example : "Beer", "Wine")
    private String drinkType;
    // The number of calories in the drink (as a Long, to allow for null values)
    private Long calories;
    // The time when the drink was consumed (stored as a String)
    private String time;
    // The contribution of this drink to the overall BAC (as a Double)
    private Double bacContribution;

    // Constructor: initializes a DrinkLogItem with the provided values.
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
}