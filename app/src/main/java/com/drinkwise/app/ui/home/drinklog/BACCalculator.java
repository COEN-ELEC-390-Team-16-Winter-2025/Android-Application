package com.drinkwise.app.ui.home.drinklog;

import java.util.List;

public class BACCalculator {

    /**
     * Calculates the estimated BAC by summing the bacContribution values
     * from each logged drink.
     *
     * @param drinkLogItems List of DrinkLogItem objects containing the BAC contributions.
     * @return The total estimated BAC.
     */
    public static double calculateBAC(List<DrinkLogItem> drinkLogItems) {
        double totalBAC = 0.0;
        for (DrinkLogItem item : drinkLogItems) {
            totalBAC += item.getBacContribution();
        }
        return totalBAC;
    }
}