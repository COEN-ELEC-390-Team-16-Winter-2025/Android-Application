package com.drinkwise.app.ui.notifications;

import android.os.Bundle;
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
    private final List< AlertItem> alertList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);
        recyclerView = view.findViewById(R.id.alertRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notifAdapter = new NotifAdapter(getContext(), alertList);


        recyclerView.setAdapter(notifAdapter);

        boolean showAlerts = true;
        if (getArguments() != null) {
            showAlerts = getArguments().getBoolean("showAlerts", true);
        }

        if (showAlerts) {
            fetchAlerts();
        } else {
            alertList.clear();
            notifAdapter.updateData(alertList); // clear content if toggle is off
        }

        return view;
    }


    private void fetchAlerts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            return;
        }

        // Fetch the preferences
        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Preferences")
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    boolean alertsEnabled = false;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Boolean alertsPref = documentSnapshot.getBoolean("Alerts");
                        alertsEnabled = (alertsPref != null) ? alertsPref : false;
                    }

                    if (!alertsEnabled) {
                        alertList.clear();
                        notifAdapter.updateData(alertList); // Make sure this exists in your adapter
                    } else {
                        // Fetch the alerts
                        db.collection("users")
                                .document(userId)
                                .collection("Alerts")
                                .orderBy("Timestamp", Query.Direction.DESCENDING)
                                .addSnapshotListener((querySnapshot, queryError) -> {
                                    if (queryError != null) {
                                        return;
                                    }

                                    if (querySnapshot != null) {
                                        alertList.clear();
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            AlertItem alert = document.toObject(AlertItem.class);
                                            alertList.add(alert);
                                        }
                                        notifAdapter.updateData(alertList);
                                    }
                                });
                    }
                });
    }

}


