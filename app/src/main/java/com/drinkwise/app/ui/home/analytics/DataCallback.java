package com.drinkwise.app.ui.home.analytics;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public interface DataCallback {
    void onDataFetched(ArrayList<PieEntry> pieEntries);
}

