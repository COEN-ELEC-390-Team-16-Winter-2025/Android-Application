package com.drinkwise.app.ui.home.drinklog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.drinkwise.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DrinkLogFragment extends Fragment {
    private static final String TAG = "DrinkLogFragment";
    private TextView drinkLogTextView;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drink_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        drinkLogTextView = view.findViewById(R.id.drinkLogTextView);
        db = FirebaseFirestore.getInstance();
        fetchDrinkLogs();
    }

    private void fetchDrinkLogs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not logged in. Cannot fetch drink logs.");
            drinkLogTextView.setText("Please log in to view drink logs.");
            return;
        }

        String userId = user.getUid();

        db.collection("users").document(userId)
                .collection("manual_drink_logs")
                .orderBy("timestamp") // Sort by time (optional)
                .addSnapshotListener((value, error) -> {

                    if(error != null){
                        Log.e(TAG, "Error fetching drink logs", error);
                        drinkLogTextView.setText("Failed to load drink logs.");
                    }
                    StringBuilder logText = new StringBuilder();
                    for (QueryDocumentSnapshot document : value) {
                        String drinkType = document.getString("drinkType");
                        Long calories = document.getLong("calories");

                        // Retrieve the timestamp
                        com.google.firebase.Timestamp timestamp = document.getTimestamp("timestamp");
                        String formattedTime = "Unknown Time";  // Default value if timestamp is missing

                        // Format the timestamp if it exists
                        if (timestamp != null) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault());
                            formattedTime = sdf.format(timestamp.toDate());
                        }

                        logText.append("Drink: ").append(drinkType)
                                .append(", Calories: ").append(calories)
                                .append(", Time: ").append(formattedTime)
                                .append("\n");
                    }

                    // Set the formatted log text in the TextView
                    drinkLogTextView.setText(logText.toString());
                });
    }
}
