package com.drinkwise.app.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.drinkwise.app.ui.home.analytics.AnalyticsFragment;
import com.drinkwise.app.ui.home.bachistory.BacHistoryFragment;
import com.drinkwise.app.ui.home.drinklog.DrinkLogFragment;

// ViewPagerAdapter extends FragmentStateAdapter to provide the correct Fragment for each tab/page.
// This adapter is used with ViewPager2 in the HomeFragment.
public class ViewPagerAdapter extends FragmentStateAdapter {

    // Constructor: Accepts a FragmentActivity which is passed to the super constructor.
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Called by the ViewPager2 to create a fragment for the given position.
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Return the fragment for the Drink Log tab.
                return new DrinkLogFragment();
            case 1:
                // Return the fragment for the BAC History tab.
                return new BacHistoryFragment();
            case 2:
                // Return the fragment for the Analytics tab.
                return new AnalyticsFragment();
            default:
                // If the position is invalid, throw an exception.
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    // Returns the total number of tabs to be displayed.
    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}