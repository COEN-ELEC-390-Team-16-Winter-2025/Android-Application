package com.drinkwise.app.ui.home.bachistory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BacHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private BACEntryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bac_history, container, false);

        recyclerView = view.findViewById(R.id.bacHistoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BACEntryAdapter();
        recyclerView.setAdapter(adapter);

        loadBacHistory();

        return view;
    }

    private void loadBacHistory() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.e("Firestore", "No user is signed in!");
            return;
        }

        Log.d("Firestore", "Looking for userId: " + userId);

        db.collection("users")
                .document(userId)
                .collection("BacEntry")
//                .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<BACEntry> bacHistory = new ArrayList<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Double bacValue = doc.getDouble("bacValue");
                            String status = doc.getString("Status");
                            String dateStr = doc.getString("Date");
                            String timeStr = doc.getString("Time");

                            if (bacValue != null && dateStr != null && timeStr != null) {
                                String documentId = doc.getId();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                                try {
                                    Date date = sdf.parse(documentId);
                                    Timestamp timestamp = new Timestamp(date);

                                    Log.d("Firestore", "BAC Entry - Timestamp: " + date
                                            + ", bacValue: " + bacValue
                                            + ", Status: " + status);

                                    BACEntry entry = new BACEntry(bacValue, timestamp);
                                    bacHistory.add(entry);
                                } catch (ParseException e) {
                                    Log.e("Firestore", "Invalid document ID (not a parsable date): " + documentId, e);
                                }
                            } else {
                                Log.w("Firestore", "Missing data in document: " + doc.getId());
                            }
                        }

                        adapter.setBacEntries(bacHistory);
                    } else {
                        Log.d("Firestore", "No BAC history found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting BAC history", e);
                });
    }




    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
}
