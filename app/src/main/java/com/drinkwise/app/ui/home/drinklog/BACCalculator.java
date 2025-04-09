package com.drinkwise.app.ui.home.drinklog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BACCalculator {


    private static final double ELIMINATION_RATE = 0.015; // Elimination rate per hour. We should change it or maybe adjust it for weight and height ??

    public static double calculateBAC(List<DrinkLogItem> drinkLogItems) {
        double totalBAC = 0.0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long currentTimeMillis = System.currentTimeMillis();

        for (DrinkLogItem item : drinkLogItems) {
            Double contribution = item.getBacContribution();
            if (contribution != null) {
                double effectiveContribution = contribution;
                String timeString = item.getTime();
                try {
                    Date drinkTime = sdf.parse(timeString);
                    if (drinkTime != null) {
                        long elapsedMillis = currentTimeMillis - drinkTime.getTime();
                        double elapsedHours = elapsedMillis / 3600000.0;
                        effectiveContribution = contribution - (ELIMINATION_RATE * elapsedHours);
                        if (effectiveContribution < 0) {
                            effectiveContribution = 0;
                        }
                    }
                } catch (ParseException e) {
                    System.out.println("Warning: could not parse time for drink log item: " + timeString);
                }
                totalBAC += effectiveContribution;
            } else {
                System.out.println("Warning: bacContribution was null for a drink.");
            }
        }

        return totalBAC;
    }
}