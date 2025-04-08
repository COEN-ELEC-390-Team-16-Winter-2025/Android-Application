package com.drinkwise.app.ui.home.drinklog;

import java.util.List;
public class BACCalculator {

    public static double calculateBAC(List<DrinkLogItem> drinkLogItems) {
        double totalBAC = 0.0;

        for (DrinkLogItem item : drinkLogItems) {
            Double contribution = item.getBacContribution();
            if (contribution != null) {
                totalBAC += contribution;
            } else {

                System.out.println("Warning: bacContribution was null for a drink.");
            }
        }

        return totalBAC;
    }

}