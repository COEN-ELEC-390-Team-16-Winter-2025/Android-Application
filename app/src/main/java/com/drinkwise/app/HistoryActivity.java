package com.drinkwise.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class HistoryActivity extends AppCompatActivity {

    private Spinner filterSpinner;
    private BACEntryAdapter bacAdapter;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // initialize Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        // setup UI
        filterSpinner = findViewById(R.id.filterSpinner);
        Button applyFilterButton = findViewById(R.id.applyFilterButton);
        RecyclerView bacRecyclerView = findViewById(R.id.bacRecyclerView);

        bacRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bacAdapter = new BACEntryAdapter();
        bacRecyclerView.setAdapter(bacAdapter);

        // load weekly data by default
        fetchBACData("Weekly");

        // button that applies the selected filter
        applyFilterButton.setOnClickListener(view -> {
            String selectedFilter = filterSpinner.getSelectedItem().toString();
            fetchBACData(selectedFilter);
        });
    }

    private void fetchBACData(String filterType) {
        // calculate start date for the query
        Calendar startDate = Calendar.getInstance();

        switch (filterType) {
            case "Daily":
                // today
                startDate.set(Calendar.HOUR_OF_DAY, 0);
                startDate.set(Calendar.MINUTE, 0);
                startDate.set(Calendar.SECOND, 0);
                break;

            case "Weekly":
                // 7 days ago
                startDate.add(Calendar.DAY_OF_YEAR, -7);
                break;

                //Shouldn't we implement a datePicker here ? I put 30 days by default for now
            case "Custom":
                startDate.add(Calendar.DAY_OF_YEAR, -30);
                break;

            default:
                // do weekly by default
                startDate.add(Calendar.DAY_OF_YEAR, -7);
                break;
        }

        // query firestore for all entries newer than startDate
        db.collection("users")
                .document(userId)
                .collection("bacData")
                .whereGreaterThan("timestamp", new Timestamp(startDate.getTime()))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BACEntry> bacEntries = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (var doc : queryDocumentSnapshots) {
                            BACEntry entry = doc.toObject(BACEntry.class);
                            if (entry != null) {
                                bacEntries.add(entry);
                            }
                        }
                    }
                    // update the RecyclerView
                    bacAdapter.setBacEntries(bacEntries);
                })
                .addOnFailureListener(e -> {
                    // handle any errors
                });
    }
}