package com.drinkwise.app.ui.home.analytics;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.drinkwise.app.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.Time;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AnalyticsFragment extends Fragment {


    protected PieChart drinkTypePieChart;
    protected Button generateGraph, selectStartDate, selectEndDate;
    protected Spinner graphTypeSpinner;

    public AnalyticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);


        drinkTypePieChart = view.findViewById(R.id.drinkTypePieChart);
        generateGraph = view.findViewById(R.id.generateGraph);

        generateGraph.setOnClickListener(v -> {
            showGenerateGraphDialog();
        });




        // Inflate your fragment layout
        return view;




    }

    public void drink_type_chart_init(ArrayList<PieEntry> pieEntries){

        Log.d("PieChart", "Initializing PieChart...");

        if (pieEntries == null || pieEntries.isEmpty()) {
            Log.e("PieChart", "No data available to display.");
            Toast.makeText(requireContext(), "No data for this period", Toast.LENGTH_SHORT).show();
            return; // Prevents drawing an empty chart
        }

        Log.d("PieChart", "Total Entries: " + pieEntries.size());

        PieDataSet dataSet = new PieDataSet(pieEntries, "Drinks Consumed");
        dataSet.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.BLACK);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        int total_drink_count = 0;

        for(PieEntry entry: pieEntries){
            total_drink_count += entry.getValue();
        }
        PieData pieData = new PieData(dataSet);


        pieData.setValueFormatter(new IntegerValueFormatter());
        drinkTypePieChart.setCenterText("Total Drinks: "+total_drink_count);
        drinkTypePieChart.setCenterTextSize(20f);
        drinkTypePieChart.setCenterTextColor(Color.BLACK);
        drinkTypePieChart.setData(pieData);
        drinkTypePieChart.setUsePercentValues(false);
        drinkTypePieChart.getDescription().setEnabled(false);
        drinkTypePieChart.setEntryLabelColor(Color.BLACK);
        drinkTypePieChart.animateY(500);

        drinkTypePieChart.invalidate();

    }

    public void fetch_drink_consumed(Timestamp start, Timestamp end, DataCallback callback){

        Map<String, Integer> data = new HashMap<>();
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to view drink logs.", Toast.LENGTH_SHORT).show();
        }

        String userId = user.getUid();

        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .whereGreaterThanOrEqualTo("timestamp", start)
                .whereLessThanOrEqualTo("timestamp", end)
                .addSnapshotListener((value, error) -> {
                    if(error != null){
                        Log.e("Error", "Error fetching data: ", error);
                    }

                    if(value == null || value.isEmpty()){
                        Toast.makeText(requireContext(), "The current timestamp didn't return any results", Toast.LENGTH_SHORT).show();
                    }

                    for (QueryDocumentSnapshot document : value) {
                        String drinkType = document.getString("drinkType");
                        Long calories = document.getLong("calories");
                        Timestamp timestamp = document.getTimestamp("timestamp");

                        data.put(drinkType, data.getOrDefault(drinkType, 0 )+1);

                        Log.d("Fetching Results", "Drink: " + drinkType + ", Calories: " + calories + ", Date: " + timestamp.toDate());
                    }

                    for(Map.Entry<String, Integer> drinkEntry: data.entrySet()){
                        pieEntries.add(new PieEntry(drinkEntry.getValue(), drinkEntry.getKey()));
                        Log.d("Pie Entries", pieEntries.toString());
                    }

                    callback.onDataFetched(pieEntries);
                });
    }

    public void showGenerateGraphDialog(){

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View generateGraphView = inflater.inflate(R.layout.generate_graph_dialog, null);

        String[] graph = {"BAC Graph", "Calories Graph", "Drink Consumed Graph"};

        graphTypeSpinner = generateGraphView.findViewById(R.id.graphType);
        selectStartDate = generateGraphView.findViewById(R.id.startDateBtn);
        selectEndDate = generateGraphView.findViewById(R.id.endDateBtn);

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        selectStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        startDate.set(year, month, dayOfMonth);
                        String dateText = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate.getTime());
                        selectEndDate.setText(dateText);
                    },
                    startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH),
                    startDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        selectEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        endDate.set(year, month, dayOfMonth);
                        String dateText = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate.getTime());
                        selectEndDate.setText(dateText);
                    },
                    endDate.get(Calendar.YEAR),
                    endDate.get(Calendar.MONTH),
                    endDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });


        new AlertDialog.Builder(requireContext())
                .setTitle("Generate Graph")       // Set dialog title
                .setView(generateGraphView)       // Attach custom layout
                .setPositiveButton("Generate", (dialog, which) -> {
                    // Action when 'Generate' is clicked
                    String selectedGraphType = graphTypeSpinner.getSelectedItem().toString();

                    switch(selectedGraphType){
                        case "BAC Graph":
                        case "Calories Graph":
                        case "Drink Consumed Graph": fetch_drink_consumed(new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()), pieEntries -> {
                            if(!pieEntries.isEmpty()){
                                drink_type_chart_init(pieEntries);
                            }
                            else{
                                Log.d("Pie Entry", "No dataset for Pie Entries");
                            }
                        });

                    }

                    Toast.makeText(requireContext(), "Graph Generated!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Action when 'Cancel' is clicked
                    dialog.dismiss();  // Close the dialog
                })
                .show();  // Show the dialog

    }

    public class IntegerValueFormatter extends ValueFormatter{
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }
}

