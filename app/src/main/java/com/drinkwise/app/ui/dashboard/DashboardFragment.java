package com.drinkwise.app.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.drinkwise.app.R;
import com.drinkwise.app.ScanningActivity;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    // Top Section Views
    private TextView bacLevel;           // Shows BAC level as a percentage (e.g., 0.06%)
    private ProgressBar bacProgressBar;  // Horizontal progress bar for BAC level
    private TextView bacStatus;          // Text status like "Safe", "Caution", "Danger"

    // Alcohol counters
    private TextView beerCount;
    private TextView wineCount;
    private Button addBeerButton;
    private Button addWineButton;

    // Bottom Buttons
    private Button seeListButton;
    private Button refreshButton;
    private Button quickHelpButton;

    // Counters
    private int beerCounter = 3;
    private int wineCounter = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize BAC section views
        bacLevel = view.findViewById(R.id.bacLevel);
        bacProgressBar = view.findViewById(R.id.bacProgressBar);
        bacStatus = view.findViewById(R.id.bacStatus);

        // Initialize alcohol counter views
        beerCount = view.findViewById(R.id.beerCount);
        wineCount = view.findViewById(R.id.wineCount);
        addBeerButton = view.findViewById(R.id.addBeerButton);
        addWineButton = view.findViewById(R.id.addWineButton);

        // Initialize action buttons
        seeListButton = view.findViewById(R.id.seeListButton);
        refreshButton = view.findViewById(R.id.refreshButton);
        quickHelpButton = view.findViewById(R.id.quickHelpButton);

        setupButtonListeners();

        // Handle arguments passed to the fragment (latest BAC)
        if (getArguments() != null) {
            String latestBacEntry = getArguments().getString("latest_bac_entry");
            if (latestBacEntry != null) {
                try {
                    double bacValue = Double.parseDouble(latestBacEntry);
                    updateBacLevel(bacValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid BAC entry from arguments: " + latestBacEntry, e);
                    showDefaultBacValue();
                }
            } else {
                showDefaultBacValue();
            }
        } else {
            showDefaultBacValue();
        }

        // Initialize counters
        updateBeerCount();
        updateWineCount();
    }

    private void showDefaultBacValue() {
        bacLevel.setText("--");
        bacProgressBar.setProgress(0);
        bacStatus.setText("N/A");
        bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_default));
    }

    private void setupButtonListeners() {

        seeListButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            startActivity(intent);
        });

        refreshButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            intent.putExtra("mode", "refreshBAC");
            startActivity(intent);
        });

        quickHelpButton.setOnClickListener(v -> {
            // TODO: Implement quick help functionality
            Log.d(TAG, "Quick Help button clicked");
        });

        addBeerButton.setOnClickListener(v -> {
            beerCounter++;
            updateBeerCount();
        });

        addWineButton.setOnClickListener(v -> {
            wineCounter++;
            updateWineCount();
        });
    }

    private void updateBeerCount() {
        beerCount.setText(String.valueOf(beerCounter));
    }

    private void updateWineCount() {
        wineCount.setText(String.valueOf(wineCounter));
    }

    /**
     * Updates BAC level, progress bar, and status text/color
     *
     * @param bacValue BAC value as a decimal (e.g., 0.06 for 6%)
     */
    private void updateBacLevel(double bacValue) {
        if (getContext() == null) return;

        // Update BAC percentage text
        bacLevel.setText(String.format("%.2f%%", bacValue));

        // Convert to progress (assuming 100 is max BAC * 1.0)
        int progress = (int) (bacValue * 100);
        bacProgressBar.setProgress(progress);

        // Set status, text color, and progress drawable
        if (bacValue < 0.03) {
            bacStatus.setText("Safe");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_safe));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_safe));
        } else if (bacValue < 0.08) {
            bacStatus.setText("Caution");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_caution));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_caution));
        } else {
            bacStatus.setText("Danger");
            bacStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.bac_danger));
            bacProgressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bac_progress_bar_danger));
        }

        Log.d(TAG, "BAC updated: " + bacValue + ", progress: " + progress);
    }
}

