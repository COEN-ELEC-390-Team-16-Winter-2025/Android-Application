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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up ViewPager2 and TabLayout
        setupViewPager();

        return root;
    }

    private void setupViewPager() {
        // Initialize ViewPager2
        ViewPager2 viewPager = binding.viewPager;
        viewPager.setAdapter(new ViewPagerAdapter(requireActivity()));

        // Connect TabLayout with ViewPager2
        TabLayout tabLayout = binding.tabLayout;
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Drink Log");
                    break;
                case 1:
                    tab.setText("BAC History");
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