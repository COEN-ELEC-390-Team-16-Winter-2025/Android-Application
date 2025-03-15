package com.drinkwise.app.ui.home.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.drinkwise.app.R;

public class AnalyticsFragment extends Fragment {

    public AnalyticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate your fragment layout
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }
}

