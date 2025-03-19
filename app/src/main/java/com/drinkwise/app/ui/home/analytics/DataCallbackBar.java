package com.drinkwise.app.ui.home.analytics;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public interface DataCallbackBar {
    void onDataFetchedBar(ArrayList<BarEntry> barEntries, ArrayList<String> dates);
}
