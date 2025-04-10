package com.drinkwise.app.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotifAdapter notifAdapter;
    private List<NotificationItem> alertList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AlertsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_alerts, container, false);
        recyclerView = root.findViewById(R.id.alertRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notifAdapter = new NotifAdapter(getContext(), alertList);
        recyclerView.setAdapter(notifAdapter);

        boolean showAlerts = true;
        if (getArguments() != null) {
            showAlerts = getArguments().getBoolean("showAlerts", true);
        }

        if (showAlerts) {
            loadAlerts();
        } else {
            alertList.clear();
            notifAdapter.updateData(alertList);
            Log.d(TAG, "Alerts are disabled");
        }

        return root;
    }

    private void loadAlerts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e(TAG, "No user logged in");
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Preferences")
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error fetching preferences: ", e);
                        return;
                    }

                    boolean alertsEnabled = false;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Boolean alertPref = documentSnapshot.getBoolean("Alerts");
                        alertsEnabled = (alertPref != null) ? alertPref : false;
                    }

                    if (!alertsEnabled) {
                        alertList.clear();
                        notifAdapter.updateData(alertList);
                        Log.d(TAG, "Alerts are turned off in preferences.");
                    } else {
                        db.collection("users")
                                .document(userId)
                                .collection("Alerts")
                                .orderBy("Timestamp", Query.Direction.DESCENDING)
                                .addSnapshotListener((querySnapshot, queryError) -> {
                                    if (queryError != null) {
                                        Log.e(TAG, "Error fetching alerts: ", queryError);
                                        return;
                                    }

                                    if (querySnapshot != null) {
                                        alertList.clear();
                                        for (QueryDocumentSnapshot doc : querySnapshot) {
                                            Log.d(TAG, "Document Data: " + doc.getData());
                                            AlertItem alert = doc.toObject(AlertItem.class);
                                            alertList.add(alert);
                                        }
                                        Log.d(TAG, "Fetched " + alertList.size() + " alerts");
                                        notifAdapter.updateData(alertList);
                                    }
                                });
                    }
                });
    }
}
