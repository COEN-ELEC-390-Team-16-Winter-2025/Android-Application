package com.drinkwise.app.ui.home.drinklog;

import java.util.List;

// BACCalculator is a utility class that calculates the estimated Blood Alcohol Concentration (BAC)
// based on the contributions from each logged drink.
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
            Double contribution = item.getBacContribution();
            if (contribution != null) {
                totalBAC += contribution;
            } else {
                // Optional: log this issue
                System.out.println("Warning: bacContribution was null for a drink.");
            }
        }

        return totalBAC;
    }

}