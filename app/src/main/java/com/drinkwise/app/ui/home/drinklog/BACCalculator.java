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
        // Initialize the total BAC to 0.0
        double totalBAC = 0.0;

        // Loop through each DrinkLogItem in the list
        for (DrinkLogItem item : drinkLogItems) {
            // Add the bacContribution of the current item to the total
            totalBAC += item.getBacContribution();
        }

        // Return the total estimated BAC
        return totalBAC;
    }
}