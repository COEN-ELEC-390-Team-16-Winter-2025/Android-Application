package com.drinkwise.app.ui.home.bachistory;

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

import com.drinkwise.app.BACEntry;
import com.drinkwise.app.BACEntryAdapter;
import com.drinkwise.app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.RadioButton;


public class BacHistoryFragment extends Fragment {

    private static final String TAG = "BacHistoryFragment"; // Logging tag
    private BACEntryAdapter adapter;
    private List<BACEntry> bacEntries = new ArrayList<>(); // Initialize list to avoid null pointers
    private EditText searchBar;

    // Define sorting options
    public enum SortOption {
        TIME_NEWEST_FIRST,
        TIME_OLDEST_FIRST,
        BAC_HIGHEST_FIRST,
        BAC_LOWEST_FIRST
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView started");
        View view = inflater.inflate(R.layout.fragment_bac_history, container, false);

        try {
            RecyclerView recyclerView = view.findViewById(R.id.bacHistoryRecyclerView);
            searchBar = view.findViewById(R.id.searchBar);
            Button sortButton = view.findViewById(R.id.sortButton);
            Button filterButton = view.findViewById(R.id.filterButton);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new BACEntryAdapter();
            recyclerView.setAdapter(adapter);


            sortButton.setOnClickListener(v -> {
                Log.d(TAG, "Sort button clicked");
                showSortDialog();
            });

            filterButton.setOnClickListener(v -> {
                Log.d(TAG, "Filter button clicked");
                showFilterDialog();
            });

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "Search query changed: " + s);
                    applyFiltersAndSearch();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            loadBacHistory();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
        }

        return view;
    }

    // Sort the BAC entries based on the selected sort option
    private List<BACEntry> sortBACEntries(List<BACEntry> entries, SortOption sortOption) {
        Log.d(TAG, "Sorting entries by: " + sortOption.name());
        switch (sortOption) {
            case TIME_NEWEST_FIRST:
                entries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));
                break;
            case TIME_OLDEST_FIRST:
                entries.sort(Comparator.comparing(BACEntry::getTimestamp));
                break;
            case BAC_HIGHEST_FIRST:
                entries.sort((e1, e2) -> Double.compare(e2.getBac(), e1.getBac()));
                break;
            case BAC_LOWEST_FIRST:
                entries.sort(Comparator.comparingDouble(BACEntry::getBac));
                break;
        }
        return entries;
    }

    // Class for filtering BAC entries based on various criteria
    public static class BACFilter {
        private String timePeriod;
        private Date startDate;
        private Date endDate;
        private String status;

        public String getTimePeriod() { return timePeriod; }
        public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // Filter the BAC entries based on the specified filter criteria
    private List<BACEntry> filterBACEntries(List<BACEntry> entries, BACFilter filter) {
        Log.d(TAG, "Filtering entries");
        List<BACEntry> filteredEntries = new ArrayList<>();

        // Loop through each BAC entry and check if it matches the filter criteria
        for (BACEntry entry : entries) {
            boolean matches = true;

            if (filter.getTimePeriod() != null) {
                switch (filter.getTimePeriod()) {
                    case "Today":
                        if (!isToday(entry.getTimestamp())) matches = false;
                        break;
                    case "This Week":
                        if (!isThisWeek(entry.getTimestamp())) matches = false;
                        break;
                    case "This Month":
                        if (!isThisMonth(entry.getTimestamp())) matches = false;
                        break;
                   case "Custom":
                        if (filter.getStartDate() != null && filter.getEndDate() != null) {
                            if (!isWithinDateRange(entry.getTimestamp(), filter.getStartDate(), filter.getEndDate()))
                                matches = false;
                        }
                        break;
                }
            }

            if (filter.getStatus() != null && !entry.getStatus().equals(filter.getStatus())) {
                matches = false;
            }

            if (matches) {
                filteredEntries.add(entry);
            }
        }
        Log.d(TAG, "Filtered entries count: " + filteredEntries.size());
        return filteredEntries;
    }

    // Load BAC entries from the Firestore database
    private void loadBacHistory() {
        Log.d(TAG, "Loading BAC history");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.e(TAG, "No user is signed in");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("BacEntry")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "BAC entries loaded successfully");

                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<BACEntry> bacHistory = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            try {
                                Double bacValue = doc.getDouble("bacValue");
                                String status = doc.getString("Status");
                                String dateStr = doc.getString("Date");
                                String timeStr = doc.getString("Time");

                                Log.d(TAG, "Doc ID: " + doc.getId());

                                if (bacValue != null && dateStr != null && timeStr != null) {
                                    String documentId = doc.getId();
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                                    Date date = sdf.parse(documentId);
                                    if (date != null) {
                                        Timestamp timestamp = new Timestamp(date);

                                        Log.d(TAG, "Parsed date: " + date);

                                        BACEntry entry = new BACEntry(bacValue, timestamp);
                                        entry.setStatus(status != null ? status : "Unknown");

                                        bacHistory.add(entry);
                                    } else {
                                        Log.w(TAG, "Parsed date is null for document: " + documentId);
                                    }

                                } else {
                                    Log.w(TAG, "Missing data in document: " + doc.getId());
                                }
                            } catch (ParseException e) {
                                Log.e(TAG, "ParseException for documentId: " + doc.getId(), e);
                            } catch (Exception e) {
                                Log.e(TAG, "Unexpected exception while processing document: " + doc.getId(), e);
                            }
                        }

                        bacEntries = bacHistory;
                        adapter.setBacEntries(bacHistory);
                    } else {
                        Log.d(TAG, "No BAC history found");
                        bacEntries = new ArrayList<>();
                        adapter.setBacEntries(bacEntries);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting BAC history", e));
    }

    // Retrieves the current user's ID from Firebase Authentication
    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;
        Log.d(TAG, "Current user ID: " + userId);
        return userId;
    }

    // Displays a dialog to allow the user to select a sorting option for BAC entries
    private void showSortDialog() {
        Log.d(TAG, "Showing sort dialog");

        String[] sortOptions = {"Newest First", "Oldest First", "Highest BAC First", "Lowest BAC First"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Sort By")
                .setItems(sortOptions, (dialog, which) -> {
                    SortOption selectedOption = SortOption.values()[which];
                    Log.d(TAG, "Selected sort option: " + selectedOption);

                    List<BACEntry> sortedEntries = sortBACEntries(new ArrayList<>(bacEntries), selectedOption);
                    adapter.setBacEntries(sortedEntries);
                })
                .show();
    }

    // Displays a dialog to allow the user to filter BAC entries by time period, status, and custom date range
    private void showFilterDialog() {
        Log.d(TAG, "Showing filter dialog");

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View filterDialogView = inflater.inflate(R.layout.dialog_filter, null);

        RadioGroup timePeriodGroup = filterDialogView.findViewById(R.id.timePeriodGroup);
        Spinner statusSpinner = filterDialogView.findViewById(R.id.statusSpinner);

        RadioButton customRadioButton = filterDialogView.findViewById(R.id.customRadio);
        LinearLayout customDateLayout = filterDialogView.findViewById(R.id.customDateLayout);
        Button startDateButton = filterDialogView.findViewById(R.id.startDateButton);
        Button endDateButton = filterDialogView.findViewById(R.id.endDateButton);

        final Calendar startDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();

        timePeriodGroup.setOnCheckedChangeListener((group, checkedId) -> customDateLayout.setVisibility(checkedId == R.id.customRadio ? View.VISIBLE : View.GONE));

        // Start Date picker
        startDateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.DatePickerDialogTheme,
                    (view, year, month, dayOfMonth) -> {
                        startDate.set(year, month, dayOfMonth);
                        String dateText = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate.getTime());
                        startDateButton.setText(dateText);
                    },
                    startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH),
                    startDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // End Date picker
        endDateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.DatePickerDialogTheme,
                    (view, year, month, dayOfMonth) -> {
                        endDate.set(year, month, dayOfMonth);
                        String dateText = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate.getTime());
                        endDateButton.setText(dateText);
                    },
                    endDate.get(Calendar.YEAR),
                    endDate.get(Calendar.MONTH),
                    endDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Single AlertDialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Filter By")
                .setView(filterDialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    BACFilter filter = new BACFilter();

                    String selectedTimePeriod = getSelectedTimePeriod(timePeriodGroup);
                    filter.setTimePeriod(selectedTimePeriod);

                    if ("Custom".equals(selectedTimePeriod)) {
                        if (startDateButton.getText().toString().equals("Start Date") ||
                                endDateButton.getText().toString().equals("End Date")) {
                            Toast.makeText(requireContext(), "Please select both start and end dates", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        filter.setStartDate(startDate.getTime());
                        filter.setEndDate(endDate.getTime());

                        Log.d(TAG, "Custom range selected: Start=" + startDate.getTime() + ", End=" + endDate.getTime());
                    }

                    filter.setStatus(statusSpinner.getSelectedItem().toString());

                    Log.d(TAG, "Applying filters: TimePeriod=" + filter.getTimePeriod() +
                            ", Status=" + filter.getStatus());

                    List<BACEntry> filteredEntries = filterBACEntries(new ArrayList<>(bacEntries), filter);
                    adapter.setBacEntries(filteredEntries);
                })
                .setNegativeButton("Clear", (dialog, which) -> {
                    Log.d(TAG, "Clearing filters");
                    adapter.setBacEntries(bacEntries);
                })
                .show();
    }


    // Returns the selected time period from the radio group (Today, This Week, This Month, Custom)
    private String getSelectedTimePeriod(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        Log.d(TAG, "Selected RadioButton ID: " + selectedId);

        if (selectedId == R.id.todayRadio) {
            return "Today";
        } else if (selectedId == R.id.thisWeekRadio) {
            return "This Week";
        } else if (selectedId == R.id.thisMonthRadio) {
            return "This Month";
        } else if (selectedId == R.id.customRadio) {
            return "Custom";
        }

        return null;
    }

    // Checks if the given timestamp is from today
    private boolean isToday(Timestamp timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar ts = Calendar.getInstance();
        ts.setTime(timestamp.toDate());

        return today.get(Calendar.YEAR) == ts.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == ts.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isThisWeek(Timestamp timestamp) {
        Calendar week = Calendar.getInstance();
        Calendar ts = Calendar.getInstance();
        ts.setTime(timestamp.toDate());

        return week.get(Calendar.YEAR) == ts.get(Calendar.YEAR)
                && week.get(Calendar.WEEK_OF_YEAR) == ts.get(Calendar.WEEK_OF_YEAR);
    }

    public static boolean isThisMonth(Timestamp timestamp) {
        if (timestamp == null) return false;

        Calendar current = Calendar.getInstance();  // Get the current date/time
        Calendar ts = Calendar.getInstance();       // Create a calendar from the timestamp
        ts.setTime(timestamp.toDate());


        return current.get(Calendar.YEAR) == ts.get(Calendar.YEAR) &&
                current.get(Calendar.MONTH) == ts.get(Calendar.MONTH);
    }


    private boolean isWithinDateRange(Timestamp timestamp, Date startDate, Date endDate) {
        Date entryDate = timestamp.toDate();
        return !entryDate.before(startDate) && !entryDate.after(endDate);
    }

    // Applies the search query on BAC entries and updates the list
    // search only status right now
    private void applyFiltersAndSearch() {
        String query = searchBar.getText().toString().trim();
        Log.d(TAG, "Applying search: " + query);

        List<BACEntry> searchResults = new ArrayList<>();
        for (BACEntry entry : bacEntries) {
            String status = entry.getStatus() != null ? entry.getStatus() : "";
            if (status.toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(entry);
            }
        }

        Log.d(TAG, "Search result count: " + searchResults.size());
        adapter.setBacEntries(searchResults);
    }

}