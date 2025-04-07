package com.drinkwise.app.ui.home.analytics;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.drinkwise.app.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsFragment extends Fragment {


    protected PieChart drinkTypePieChart;
    protected BarChart caloriesBarChart;
    protected LineChart BACLineChart;
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
        caloriesBarChart = view.findViewById(R.id.caloriesBarChart);
        BACLineChart = view.findViewById(R.id.BACLineChart);

        generateGraph = view.findViewById(R.id.generateGraph);

        generateGraph.setOnClickListener(v -> {
            showGenerateGraphDialog();
        });


        // Inflate your fragment layout
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        destroyGraph();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyGraph();
    }

    public void destroyGraph(){
        if(BACLineChart != null){
            BACLineChart.clear();
            BACLineChart.setData(null);
            BACLineChart.invalidate();
        }
    }

    public void BAC_line_init(ArrayList<Entry> lineChartEntries, ArrayList<String> dates, Timestamp start, Timestamp end){

        drinkTypePieChart.setVisibility(View.GONE);
        caloriesBarChart.setVisibility(View.GONE);
        BACLineChart.setVisibility(View.VISIBLE);

        if(lineChartEntries.isEmpty()){
            Toast.makeText(requireContext(), "No data available to display", Toast.LENGTH_SHORT).show();
            return;
        }


        //TODO: Remove this line of code and use get in fetching data instead of addsnapshotlistener
        int minimumSize = Math.min(lineChartEntries.size(), dates.size());
        List<Pair<Entry, String>> combinedList = new ArrayList<>();
        for (int i = 0; i < minimumSize; i++) {
            combinedList.add(new Pair<>(lineChartEntries.get(i), dates.get(i)));
        }

        Map<String, Pair<Entry, String>> highestBACValues = new HashMap();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for(Pair<Entry, String> entry : combinedList){
            float BAC = entry.first.getY();
            String date = entry.second;

            Date dateFromEntry;
            try{
                dateFromEntry = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date);
                String formattedDate = formatter.format(dateFromEntry);
                if(!highestBACValues.containsKey(formattedDate) || BAC > highestBACValues.get(formattedDate).first.getY()){
                    highestBACValues.put(formattedDate, entry);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Log.d("DEBUG GRAPH", "Hashmap: "+highestBACValues);

        List<Map.Entry<String, Pair<Entry, String>>> entriesByDate = new ArrayList<>(highestBACValues.entrySet());
        Collections.sort(entriesByDate, (e1, e2) -> e1.getKey().compareTo(e2.getKey()));

        lineChartEntries.clear();
        dates.clear();

        int i = 0;
        for (Map.Entry<String, Pair<Entry, String>> entry : entriesByDate) {
            lineChartEntries.add(new Entry(i, entry.getValue().first.getY()));
            dates.add(entry.getKey());
            i++;
        }


        LineDataSet set = new LineDataSet(lineChartEntries, "BAC Levels");
        set.setColor(Color.BLUE);
        set.setValueTextSize(14f);
        set.setLineWidth(3f);

        LimitLine upperLimit = new LimitLine(0.08f, "Legally Impaired");
        upperLimit.setLineWidth(3f);
        upperLimit.enableDashedLine(10f, 10f, 0f);
        upperLimit.setTextSize(15f);


        // TODO: Find a way to position the label outside the graph area
        upperLimit.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        upperLimit.setXOffset(10f);
        // the above was an attempt. it put it on the left inside the graph

        YAxis leftAxis = BACLineChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upperLimit);

        leftAxis.setAxisMinimum(0f);

        LineData lineData = new LineData(set);

        BACLineChart.getXAxis().setValueFormatter(new DateFormatter(dates));
        BACLineChart.getXAxis().setGranularity(1f);
        BACLineChart.setData(lineData);

        BACLineChart.getAxisRight().setEnabled(false);
        BACLineChart.animateY(500);
        BACLineChart.invalidate();

        BACLineChart.getDescription().setEnabled(false);


        Log.d("BACChart", "Entries size: " + lineChartEntries.size());
        for (Entry entry : lineChartEntries) {
            Log.d("BACChart", "Entry x: " + entry.getX() + " y: " + entry.getY());
        }

    }
    public void calories_bar_init(ArrayList<BarEntry> barEntries, ArrayList<String> dates, Timestamp start, Timestamp end){

        BACLineChart.setVisibility(View.GONE);
        drinkTypePieChart.setVisibility(View.GONE);
        caloriesBarChart.setVisibility(View.VISIBLE);

        if (barEntries == null || barEntries.isEmpty()) {
            Toast.makeText(requireContext(), "No data for this period", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Pair<BarEntry, String>> combinedList = new ArrayList<>();
        for (int i = 0; i < barEntries.size(); i++) {
            combinedList.add(new Pair<>(barEntries.get(i), dates.get(i)));
        }

        Collections.sort(combinedList, (pair1, pair2) -> pair1.second.compareTo(pair2.second));


        barEntries.clear();
        dates.clear();

        for (int i = 0; i < combinedList.size(); i++) {
            barEntries.add(new BarEntry(i, combinedList.get(i).first.getY())); // Sequential x-values
            dates.add(combinedList.get(i).second); // Sorted dates
        }


        caloriesBarChart.setDrawValueAboveBar(true);
        caloriesBarChart.setPinchZoom(true);
        caloriesBarChart.setDrawValueAboveBar(true);
        caloriesBarChart.getAxisRight().setEnabled(false);

        BarDataSet barDataSet = new BarDataSet(barEntries, "Calories");
        barDataSet.setColor(Color.RED);
        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(14f);
        caloriesBarChart.setData(barData);

        XAxis xAxis = caloriesBarChart.getXAxis();
        xAxis.setValueFormatter(new DateFormatter(dates));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = caloriesBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        caloriesBarChart.invalidate();
        caloriesBarChart.animateY(500);
        caloriesBarChart.getDescription().setEnabled(false);


    }

    public void drink_type_chart_init(ArrayList<PieEntry> pieEntries, Timestamp start, Timestamp end){

        BACLineChart.setVisibility(View.GONE);
        caloriesBarChart.setVisibility(View.GONE);
        drinkTypePieChart.setVisibility(View.VISIBLE);

        if (pieEntries == null || pieEntries.isEmpty()) {
            Log.e("PieChart", "No data available to display.");
            Toast.makeText(requireContext(), "No data for this period", Toast.LENGTH_SHORT).show();
            return; // Prevents drawing an empty chart
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date starDate = start.toDate();
        Date endDate = end.toDate();



        int total_drink_count = 0;
        ArrayList<Integer> colors = new ArrayList<>();
        for(PieEntry entry: pieEntries){
            switch(entry.getLabel()){
                case "Beer": colors.add(ContextCompat.getColor(requireContext(), R.color.beer));
                break;
                case "Wine": colors.add((ContextCompat.getColor(requireContext(), R.color.wine)));
                break;
                case "Champagne": colors.add((ContextCompat.getColor(requireContext(), R.color.champagne)));
                break;
                case "Cocktail": colors.add((ContextCompat.getColor(requireContext(), R.color.cocktail)));
                break;
                case "Shot": colors.add((ContextCompat.getColor(requireContext(), R.color.shot)));
                break;
                case "Sake": colors.add((ContextCompat.getColor(requireContext(), R.color.sake)));
                break;
            }
            total_drink_count += entry.getValue();
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Drinks Consumed");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);

        pieData.setValueFormatter(new IntegerValueFormatter());
        drinkTypePieChart.setCenterText("Total Drinks: "+total_drink_count);
        drinkTypePieChart.setCenterTextSize(20f);
        drinkTypePieChart.setCenterTextColor(Color.BLACK);
        drinkTypePieChart.setData(pieData);
        drinkTypePieChart.setUsePercentValues(false);

        drinkTypePieChart.getDescription().setEnabled(true);
        drinkTypePieChart.getDescription().setText(String.format("Drinks Consumed from " + simpleDateFormat.format(starDate) + " to " + simpleDateFormat.format(endDate)));
        drinkTypePieChart.getDescription().setTextSize(18f);

        drinkTypePieChart.getLegend().setEnabled(false);
        drinkTypePieChart.setEntryLabelColor(Color.BLACK);
        drinkTypePieChart.animateY(500);

        drinkTypePieChart.invalidate();

    }

    public void fetch_BAC_entries(Timestamp start, Timestamp end, DataCallbackLine callback){

        HashMap<String, Double> data = new HashMap<>();
        ArrayList<Entry> lineChartEntries = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to view drink logs.", Toast.LENGTH_SHORT).show();
        }

        String userId = user.getUid();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar startDateCalendar = Calendar.getInstance();
        startDateCalendar.setTime(start.toDate());
        startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startDateCalendar.set(Calendar.MINUTE, 0);
        startDateCalendar.set(Calendar.SECOND, 0);
        String startDate = formatter.format(startDateCalendar.getTime());

        // End Date at 23:59:59
        Calendar endDateCalendar = Calendar.getInstance();
        endDateCalendar.setTime(end.toDate());
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);
        String endDate = formatter.format(endDateCalendar.getTime());

        Calendar currentDate = Calendar.getInstance();
        Calendar endDate2 = Calendar.getInstance();

        currentDate.setTimeInMillis(start.getSeconds()*1000);
        endDate2.setTimeInMillis(end.getSeconds()*1000);

        while(!currentDate.after(endDate2)){
            String date = formatter.format(currentDate.getTime());
            data.put(date, 0D);
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
        }


        db.collection("users")
                .document(userId)
                .collection("BacEntry")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String timestamp = document.getId();
                        Log.d("Timestamp", timestamp);
                        if (timestamp.compareTo(startDate) >= 0 && timestamp.compareTo(endDate) <= 0) {
                            Double bac = document.getDouble("bacValue");

                            if (bac != null) {
                                Date parsedDate;
                                try {
                                    parsedDate = formatter.parse(timestamp);
                                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parsedDate);

                                    if (data.containsKey(date)) {
                                        data.put(date, data.get(date) + bac);
                                    } else {
                                        data.put(date, bac);
                                    }

                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }

                    int index = 0;
                    ArrayList<String> dates = new ArrayList<>();
                    for (Map.Entry<String, Double> entry : data.entrySet()) {
                        float xValue = index;
                        float yValue = entry.getValue().floatValue();
                        dates.add(entry.getKey());

                        Log.d("DEBUG", "Timestamp: " + entry.getKey());
                        Log.d("DEBUG", "Value: " + yValue);
                        lineChartEntries.add(new Entry(xValue, yValue));
                        index++;
                    }

                    callback.onDataFetchedLine(lineChartEntries, dates);
                })
                .addOnFailureListener(e -> Log.e("Error", "Error fetching data: " + e));


    }
    public void fetch_calories_consumed(Timestamp start, Timestamp end, DataCallbackBar callback){

        HashMap<String, Long> data = new HashMap<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to view bac entries logs.", Toast.LENGTH_SHORT).show();
        }

        String userId = user.getUid();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar currentDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        currentDate.setTimeInMillis(start.getSeconds()*1000);
        endDate.setTimeInMillis(end.getSeconds()*1000);

        while(!currentDate.after(endDate)){
            String date = formatter.format(currentDate.getTime());
            data.put(date, 0L);
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
        }



        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .whereGreaterThanOrEqualTo("timestamp", start)
                .whereLessThanOrEqualTo("timestamp", end)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String drinkType = document.getString("drinkType");
                        Long calories = document.getLong("calories");
                        Timestamp timestamp = document.getTimestamp("timestamp");

                        if (calories != null) {
                            String date = formatter.format(timestamp.toDate());

                            if (data.containsKey(date)) {
                                data.put(date, data.get(date) + calories);
                            } else {
                                data.put(date, calories);
                            }
                        }
                    }

                    int i = 0;
                    ArrayList<String> dates = new ArrayList<>();
                    for (Map.Entry<String, Long> entry : data.entrySet()) {
                        dates.add(entry.getKey());
                        barEntries.add(new BarEntry(i, entry.getValue()));
                        i++;
                    }
                    callback.onDataFetchedBar(barEntries, dates);
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "Error fetching data: " + e);
                });
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
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(requireContext(), "The current timestamp didn't return any results", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String drinkType = document.getString("drinkType");
                        Long calories = document.getLong("calories");
                        Timestamp timestamp = document.getTimestamp("timestamp");

                        // Update the count for the drink type
                        data.put(drinkType, data.getOrDefault(drinkType, 0) + 1);

                        Log.d("Fetching Results", "Drink: " + drinkType + ", Calories: " + calories + ", Date: " + timestamp.toDate());
                    }

                    for (Map.Entry<String, Integer> drinkEntry : data.entrySet()) {
                        pieEntries.add(new PieEntry(drinkEntry.getValue(), drinkEntry.getKey()));
                        Log.d("Pie Entries", pieEntries.toString());
                    }

                    // Trigger the callback with the fetched pie entries
                    callback.onDataFetched(pieEntries);
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "Error fetching data: ", e);
                });
    }

    public void showGenerateGraphDialog(){

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View generateGraphView = inflater.inflate(R.layout.generate_graph_dialog, null);

        graphTypeSpinner = generateGraphView.findViewById(R.id.graphType);
        selectStartDate = generateGraphView.findViewById(R.id.startDateBtn);
        selectEndDate = generateGraphView.findViewById(R.id.endDateBtn);

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        selectStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.DatePickerDialogTheme,
                    (view, year, month, dayOfMonth) -> {
                        startDate.set(year, month, dayOfMonth, 0, 0, 0);
                        String dateText = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate.getTime());
                        selectStartDate.setText(dateText);
                    },
                    startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH),
                    startDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        selectEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.DatePickerDialogTheme,
                    (view, year, month, dayOfMonth) -> {
                        endDate.set(year, month, dayOfMonth, 23, 59, 59);
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
                        case "BAC Graph": fetch_BAC_entries(new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()), (lineChartEntries, dates) -> {
                            if(!lineChartEntries.isEmpty()){
                                BAC_line_init(lineChartEntries, dates, new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()));
                                Toast.makeText(requireContext(), "Graph Generated!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.d("Line Entry", "No data for Line Entries");
                                Toast.makeText(requireContext(), "No data found for this period", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                        case "Calories Graph": fetch_calories_consumed(new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()), ((barEntries, dates) -> {
                            if(!barEntries.isEmpty()){
                                calories_bar_init(barEntries, dates, new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()));
                                Toast.makeText(requireContext(), "Graph Generated!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.d("Bar Entry", "No data for Bar Entries");
                                Toast.makeText(requireContext(), "No data found for this period", Toast.LENGTH_SHORT).show();
                            }
                        }));
                        break;
                        case "Drink Consumed Graph": fetch_drink_consumed(new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()), pieEntries -> {
                            if(!pieEntries.isEmpty()){
                                drink_type_chart_init(pieEntries, new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()));
                                Toast.makeText(requireContext(), "Graph Generated!", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Log.d("Pie Entry", "No data for Pie Entries");
                                Toast.makeText(requireContext(), "No data found for this period", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
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

    public class DateFormatter extends ValueFormatter{

        private ArrayList<String> dates;
        private SimpleDateFormat dateDisplay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private SimpleDateFormat dayDisplay = new SimpleDateFormat("MMM d", Locale.getDefault());
        public DateFormatter(ArrayList<String> dates) {
            this.dates = dates;
        }

        @Override
        public String getFormattedValue(float value) {
            int i = (int) value;

            if( i >= 0 && i < dates.size()){
                try {
                    Date date = dateDisplay.parse(dates.get(i));
                    return dayDisplay.format(date);
                } catch (ParseException e) {
                    Log.e("Parsing Error", "Error parsing date" + e);
                    return "";
                }
            }
            return "";
        }
    }

    public class DateFormatter2 extends ValueFormatter {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());


        @Override
        public String getFormattedValue(float value) {
            long timestamp = (long) value;
            return dateFormat.format(new Date(timestamp));
        }

    }

    public interface DataCallbackLine {
        void onDataFetchedLine(ArrayList<Entry> lineChartEntries, ArrayList<String> dates);
    }
}

