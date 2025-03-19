package com.drinkwise.app.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.drinkwise.app.ui.home.analytics.AnalyticsFragment;
import com.drinkwise.app.ui.home.bachistory.BacHistoryFragment;
import com.drinkwise.app.ui.home.drinklog.DrinkLogFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DrinkLogFragment();
            case 1:
                return new BacHistoryFragment();
            case 2:
                return new AnalyticsFragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}