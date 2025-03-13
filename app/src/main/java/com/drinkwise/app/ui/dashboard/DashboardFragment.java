package com.drinkwise.app.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.drinkwise.app.R;
import com.drinkwise.app.ScanningActivity;

public class DashboardFragment extends Fragment {

    // BAC related views
    private TextView bacLevel;
    private ProgressBar bacProgressBar;
    private TextView bacStatus;

    // Alcohol counters
    private TextView beerCount;
    private TextView wineCount;
    private Button addBeerButton;
    private Button addWineButton;

    // Bottom buttons
    private Button seeListButton;
    private Button refreshButton;
    private Button viewHistoryButton;
    private Button quickHelpButton;

    // Local counters for drinks
    private int beerCounter = 3;
    private int wineCounter = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Top BAC Section
        bacLevel = root.findViewById(R.id.bacLevel);
        bacProgressBar = root.findViewById(R.id.bacProgressBar);
        bacStatus = root.findViewById(R.id.bacStatus);

        // Alcohol counters
        beerCount = root.findViewById(R.id.beerCount);
        wineCount = root.findViewById(R.id.wineCount);
        addBeerButton = root.findViewById(R.id.addBeerButton);
        addWineButton = root.findViewById(R.id.addWineButton);

        // Bottom Buttons
        seeListButton = root.findViewById(R.id.seeListButton);
        refreshButton = root.findViewById(R.id.refreshButton);
        quickHelpButton = root.findViewById(R.id.quickHelpButton);

        setupButtonListeners();

        return root;
    }

    private void setupButtonListeners() {

        // See List Button
        seeListButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            intent.putExtra("mode", "fullList");
            startActivity(intent);
        });

        // Refresh Button
        refreshButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanningActivity.class);
            intent.putExtra("mode", "refreshBac");
            startActivity(intent);
        });


        // Quick Help Button
        quickHelpButton.setOnClickListener(v -> {
            // Sprint 3
            // option to call a friend or info for emergency services nearby..etc.
        });

        // Add Beer Button
        addBeerButton.setOnClickListener(v -> {
            beerCounter++;
            updateBeerCount();
        });

        // Add Wine Button
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

    // You can optionally use this to set BAC values dynamically
    private void updateBacLevel(double bacValue) {
        bacLevel.setText(String.format("%.2f%%", bacValue));

        // Example logic for progress bar and status
        int progress = (int) (bacValue * 100);
        bacProgressBar.setProgress(progress);

        if (bacValue < 0.03) {
            bacStatus.setText("Safe");
            bacStatus.setTextColor(getResources().getColor(R.color.bac_safe));
        } else if (bacValue < 0.08) {
            bacStatus.setText("Caution");
            bacStatus.setTextColor(getResources().getColor(R.color.bac_caution));
        } else {
            bacStatus.setText("Danger");
            bacStatus.setTextColor(getResources().getColor(R.color.bac_danger));
        }
    }

}
