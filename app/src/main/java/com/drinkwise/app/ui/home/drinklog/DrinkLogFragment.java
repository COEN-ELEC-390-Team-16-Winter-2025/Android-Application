package com.drinkwise.app.ui.home.drinklog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrinkLogFragment extends Fragment {
    private static final String TAG = "DrinkLogFragment";
    private TextView drinkLogTextView;
    private FirebaseFirestore db;

    private RecyclerView drinkLogRecyclerView;
    private DrinkLogAdapter adapter;
    private List<DrinkLogItem> drinkLogList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drink_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        drinkLogTextView = view.findViewById(R.id.drinkLogTextView);

        drinkLogRecyclerView = view.findViewById(R.id.drinkLogRecyclerView);
        drinkLogRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        drinkLogList = new ArrayList<>();
        adapter = new DrinkLogAdapter(drinkLogList);
        drinkLogRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchDrinkLogs();
    }

    private void fetchDrinkLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not logged in. Cannot fetch drink logs.");
//            drinkLogTextView.setText("Please log in to view drink logs.");
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

                            String formattedTime = "Unknown Time";
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                                formattedTime = sdf.format(timestamp.toDate());
                            }

                            drinkLogList.add(new DrinkLogItem(drinkType, calories, formattedTime));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No drink log documents found");
                        Toast.makeText(getContext(), "No drink logs found.", Toast.LENGTH_SHORT).show();
                    }

                });

    }
}
