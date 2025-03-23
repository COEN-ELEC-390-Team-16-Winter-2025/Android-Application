package com.drinkwise.app.ui.home.drinklog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DrinkLogFragment extends Fragment {

    private static final String TAG = "DrinkLogFragment";

    private RecyclerView drinkLogRecyclerView;
    private EditText searchBar;
    private Button sortButton, filterButton;

    private FirebaseFirestore db;
    private DrinkLogAdapter adapter;
    private List<DrinkLogItem> drinkLogList = new ArrayList<>();

    private List<DrinkLogItem> filteredList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView started");
        View view = inflater.inflate(R.layout.fragment_drink_log, container, false);

        drinkLogRecyclerView = view.findViewById(R.id.drinkLogRecyclerView);
        searchBar = view.findViewById(R.id.searchBar);
        sortButton = view.findViewById(R.id.sortButton);
        filterButton = view.findViewById(R.id.filterButton);

        drinkLogRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DrinkLogAdapter(new ArrayList<>());
        drinkLogRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        setupListeners();
        fetchDrinkLogs();

        return view;
    }

    private void setupListeners() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchFilter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        sortButton.setOnClickListener(v -> showSortDialog());
        filterButton.setOnClickListener(v -> showFilterDialog());
    }

    private void fetchDrinkLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to view drink logs.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        db.collection("users").document(userId)
                .collection("manual_drink_logs")
                .orderBy("timestamp") // Sort by time (optional)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching drink logs", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        Log.d(TAG, "Fetched " + value.size() + " documents");

                        drinkLogList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            Log.d(TAG, "Document ID: " + document.getId() + " => " + document.getData());

                            String drinkType = document.getString("drinkType");
                            Long calories = document.getLong("calories");
                            com.google.firebase.Timestamp timestamp = document.getTimestamp("timestamp");
                            Double BACContribution = document.getDouble("BAC_Contribution");

                            String formattedTime = "Unknown Time";
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                                formattedTime = sdf.format(timestamp.toDate());
                            }

                            drinkLogList.add(new DrinkLogItem(drinkType, calories, formattedTime, BACContribution));
                        }

                        // Reset filtered list and update the adapter with the full list
                        filteredList.clear();
                        filteredList.addAll(drinkLogList);
                        applySearchFilter("");  // Reset the search filter
                        adapter.setDrinkLogEntries(filteredList);

                    } else {
                        Log.d(TAG, "No drink log documents found");
                        Toast.makeText(getContext(), "No drink logs found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applySearchFilter(String query) {
        List<DrinkLogItem> searchResults = new ArrayList<>();
        for (DrinkLogItem entry : filteredList) {
            if (entry.getDrinkType().toLowerCase().contains(query.toLowerCase()) ||
                    entry.getTime().toLowerCase().contains(query.toLowerCase()) ||
                    String.valueOf(entry.getCalories()).contains(query) ||
                    String.format(Locale.getDefault(), "%.2f%%", entry.getBacContribution()).contains(query)) {
                searchResults.add(entry);
            }
        }

        Log.d(TAG, "Search filter applied. Found " + searchResults.size() + " matching entries.");

        adapter.setDrinkLogEntries(searchResults);
    }

    private void showSortDialog() {
        String[] sortOptions = {
                "Newest First", "Oldest First",
                "Drink Type (A-Z)", "Drink Type (Z-A)",
                "Calories (High to Low)", "Calories (Low to High)",
                "BAC Contribution (High to Low)", "BAC Contribution (Low to High)"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Sort By")
                .setItems(sortOptions, (dialog, which) -> {
                    Log.d(TAG, "Sorting selected: " + sortOptions[which]);

                    // If no sorting is applied, reset to the original list
                    if (which == -1) {
                        filteredList.clear();
                        filteredList.addAll(drinkLogList);
                    }

                    switch (which) {
                        case 0:
                            Collections.sort(filteredList, (a, b) -> b.getTime().compareTo(a.getTime()));
                            break;
                        case 1:
                            Collections.sort(filteredList, (a, b) -> a.getTime().compareTo(b.getTime()));
                            break;
                        case 2:
                            Collections.sort(filteredList, Comparator.comparing(DrinkLogItem::getDrinkType));
                            break;
                        case 3:
                            Collections.sort(filteredList, (a, b) -> b.getDrinkType().compareTo(a.getDrinkType()));
                            break;
                        case 4:
                            Collections.sort(filteredList, (a, b) -> b.getCalories().compareTo(a.getCalories()));
                            break;
                        case 5:
                            Collections.sort(filteredList, (a, b) -> a.getCalories().compareTo(b.getCalories()));
                            break;
                        case 6:
                            Collections.sort(filteredList, (a, b) -> Double.compare(b.getBacContribution(), a.getBacContribution()));
                            break;
                        case 7:
                            Collections.sort(filteredList, (a, b) -> Double.compare(a.getBacContribution(), b.getBacContribution()));
                            break;
                    }

                    Log.d(TAG, "Sorting complete. Updating adapter.");
                    adapter.setDrinkLogEntries(filteredList);
                })
                .show();
    }

    private void showFilterDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View filterDialogView = inflater.inflate(R.layout.dialog_filter_drink_log, null);

        RadioGroup timePeriodGroup = filterDialogView.findViewById(R.id.timePeriodGroup);
        Spinner drinkTypeSpinner = filterDialogView.findViewById(R.id.drinkTypeSpinner);
        Spinner bacSpinner = filterDialogView.findViewById(R.id.bacSpinner);
        EditText minCaloriesEditText = filterDialogView.findViewById(R.id.minCaloriesEditText);
        EditText maxCaloriesEditText = filterDialogView.findViewById(R.id.maxCaloriesEditText);
        Button startDateButton = filterDialogView.findViewById(R.id.startDateButton);
        Button endDateButton = filterDialogView.findViewById(R.id.endDateButton);
        LinearLayout customDateLayout = filterDialogView.findViewById(R.id.customDateLayout);

        final Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        final Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR_OF_DAY, 11);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);

        timePeriodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.customDateRadio) {
                customDateLayout.setVisibility(View.VISIBLE);
            } else {
                customDateLayout.setVisibility(View.GONE);
            }
        });

        startDateButton.setOnClickListener(v -> showDatePicker(startDateButton, startDate));
        endDateButton.setOnClickListener(v -> showDatePicker(endDateButton, endDate));

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter By")
                .setView(filterDialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    Log.d(TAG, "Filter applied");

                    // Reset the filtered list
                    filteredList.clear();

                    // Get filter values
                    String selectedDrinkType = drinkTypeSpinner.getSelectedItem().toString();
                    String selectedBac = bacSpinner.getSelectedItem().toString();
                    String minCalories = minCaloriesEditText.getText().toString();
                    String maxCalories = maxCaloriesEditText.getText().toString();
                    boolean isCustomDateSelected = timePeriodGroup.getCheckedRadioButtonId() == R.id.customDateRadio;
                    boolean isTodayDateSelected = timePeriodGroup.getCheckedRadioButtonId() == R.id.todayRadio;
                    boolean isThisWeekSelected = timePeriodGroup.getCheckedRadioButtonId() == R.id.thisWeekRadio;
                    boolean isThisMonthSelected = timePeriodGroup.getCheckedRadioButtonId() == R.id.thisMonthRadio;
                    boolean isLastMonthSelected = timePeriodGroup.getCheckedRadioButtonId() == R.id.lastMonthRadio;

                    // Apply filters to each item
                    for (DrinkLogItem item : drinkLogList) {
                        boolean matches = true;

                        // Drink Type Filter
                        if (!selectedDrinkType.equals("All") ) {
                            if (!item.getDrinkType().equalsIgnoreCase(selectedDrinkType)) {
                                Log.d(TAG, "Drink type mismatch: " + item.getDrinkType() + " != " + selectedDrinkType);
                                matches = false;
                            }
                        }

                        // BAC Filter
                        if (!selectedBac.equals("All")) {
                            if (!String.format(Locale.getDefault(), "%.2f", item.getBacContribution()).equals(selectedBac)) {
                                Log.d(TAG, "BAC mismatch: " + item.getBacContribution() + " != " + selectedBac);
                                matches = false;
                            }
                        }

                        // Calorie Range Filter
                        try {
                            if (!minCalories.isEmpty() && item.getCalories() < Integer.parseInt(minCalories)) {
                                Log.d(TAG, "Calories below min: " + item.getCalories() + " < " + minCalories);
                                matches = false;
                            }
                            if (!maxCalories.isEmpty() && item.getCalories() > Integer.parseInt(maxCalories)) {
                                Log.d(TAG, "Calories above max: " + item.getCalories() + " > " + maxCalories);
                                matches = false;
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Invalid calorie input", e);
                        }

                        if(isTodayDateSelected){
                            if(!isToday(item.getTime())){
                                matches = false;
                            }
                        }

                        if(isThisWeekSelected){
                            if(!isThisWeek(item.getTime())){
                                matches = false;
                            }
                        }

                        if(isThisMonthSelected){
                            if(!isThisMonth(item.getTime())){
                                matches = false;
                            }
                        }

                        if(isLastMonthSelected){
                            if(!isLastMonth(item.getTime())){
                                matches = false;
                            }
                        }

                        // Date Range Filter (only if custom date is selected)
                        if (isCustomDateSelected) {
                            Date start = startDate.getTime();
                            Date end = endDate.getTime();

                            if (!isWithinDateRange(item.getTime(), start, end)) {
                                Log.d(TAG, "Date out of range: " + item.getTime() + " not between " + start + " and " + end);
                                matches = false;
                            }
                        }

                        // Add to filtered list if all filters match
                        if (matches) {
                            Log.d(TAG, "Item matches all filters: " + item);
                            filteredList.add(item);
                        }
                    }

                    // Log the filtered list size
                    Log.d(TAG, "Filtered List: " + filteredList.size() + " items matched.");

                    // Update the adapter with the filtered list
                    if (filteredList.isEmpty()) {
                        Log.d(TAG, "No items matched the filter.");
                    }
                    adapter.setDrinkLogEntries(filteredList);
                })
                .setNegativeButton("Clear", (dialog, which) -> {
                    // Reset to the original list
                    filteredList.clear();
                    filteredList.addAll(drinkLogList);
                    adapter.setDrinkLogEntries(filteredList);
                })
                .show();
    }




    private void showDatePicker(Button button, Calendar calendar) {
        new DatePickerDialog(requireContext(), R.style.DatePickerDialogTheme, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            button.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean isToday(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            cal.setTime(date);

            return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isThisWeek(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            Calendar now = Calendar.getInstance();

            cal.setTime(date);

            int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
            int year = cal.get(Calendar.YEAR);

            return weekOfYear == now.get(Calendar.WEEK_OF_YEAR) &&
                    year == now.get(Calendar.YEAR);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isThisMonth(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            Calendar now = Calendar.getInstance();

            cal.setTime(date);

            int monthOfYear = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            return monthOfYear == now.get(Calendar.MONTH) &&
                    year == now.get(Calendar.YEAR);

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLastMonth(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            Calendar lastMonth = Calendar.getInstance();

            cal.setTime(date);

            lastMonth.add(Calendar.MONTH, -1);
            int year = cal.get(Calendar.YEAR);

            return cal.get(Calendar.MONTH) == lastMonth.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR) == lastMonth.get(Calendar.YEAR);

        } catch (Exception e) {
            return false;
        }
    }

    // Helper methods to filter based on certain criteria
    private boolean isWithinDateRange(String dateStr, Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        try {
            Log.d(TAG, "Parsing date string: " + dateStr);
            Date date = sdf.parse(dateStr);

            if (date == null) {
                Log.e(TAG, "Failed to parse date: " + dateStr);
                return false;
            }

            // Log the parsed date and the start/end dates for debugging
            Log.d(TAG, "Parsed date: " + date);
            Log.d(TAG, "Start date: " + startDate);
            Log.d(TAG, "End date: " + endDate);

            // Check if the date is within the range (inclusive)
            return !date.before(startDate) && !date.after(endDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateStr, e);
            return false;
        }
    }


}
