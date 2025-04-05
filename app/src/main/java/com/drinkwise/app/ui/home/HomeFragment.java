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

// HomeFragment is responsible for displaying the main home screen,
// which contains a ViewPager2 with multiple pages ( Drink Log, BAC History, Analytics)
// and a TabLayout to allow users to switch between these pages.
public class HomeFragment extends Fragment {

    // ViewBinding instance for accessing views in fragment_home.xml
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up ViewPager2 and TabLayout for the fragment's UI
        setupViewPager();

        return root;
    }

    // Sets up the ViewPager2 with an adapter and connects it to a TabLayout
    private void setupViewPager() {
        ViewPager2 viewPager = binding.viewPager;
        viewPager.setAdapter(new ViewPagerAdapter(requireActivity()));

        // Get the TabLayout from the binding
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
        }).attach(); // Attach to finalize the connection
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear the binding reference to avoid memory leaks
        binding = null;
    }
}