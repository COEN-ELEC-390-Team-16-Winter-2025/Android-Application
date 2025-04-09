package com.drinkwise.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.drinkwise.app.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

// HomeFragment is responsible for displaying the main home screen
public class HomeFragment extends Fragment {

    // accesses views in fragment_home.xml
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setupViewPager();

        return root;
    }

    // This sets up the ViewPager2 with an adapter and connects it to a TabLayout
    private void setupViewPager() {
        ViewPager2 viewPager = binding.viewPager;
        viewPager.setAdapter(new ViewPagerAdapter(requireActivity()));

        TabLayout tabLayout = binding.tabLayout;
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Drink Log");
                    break;
                case 1:
                    tab.setText("BAC History");
                    break;
                case 2:
                    tab.setText("Analytics");
                    break;
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}