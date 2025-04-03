package com.drinkwise.app.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.drinkwise.app.R;
import com.google.android.material.tabs.TabLayout;

public class NotificationsContainerFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        tabLayout = root.findViewById(R.id.tabLayout);
        viewPager = root.findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        NotificationsPagerAdapter adapter = new NotificationsPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new RemindersFragment(), "Reminders");
        adapter.addFragment(new RecommendationsFragment(), "Recommendations");
        viewPager.setAdapter(adapter);
    }


}
