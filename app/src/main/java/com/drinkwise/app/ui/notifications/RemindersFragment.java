package com.drinkwise.app.ui.notifications;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.LandingActivity;
import com.drinkwise.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemindersFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotifAdapter notifAdapter;
    private List<NotificationItem> reminderList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reminders, container, false);
        recyclerView = root.findViewById(R.id.recyclerViewReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notifAdapter = new NotifAdapter(getContext(), reminderList);
        recyclerView.setAdapter(notifAdapter);

        loadReminders();
        return root;
    }

    private void loadReminders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("RemindersFragment", "No user logged in");
            return;
        }
        //Fetch the preferences
        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("Preferences")
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("RemindersFragment", "Error fetching preferences: ", e);
                        return;
                    }
                    boolean remindersEnabled = false;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Boolean reminderPref = documentSnapshot.getBoolean("Reminders");
                        remindersEnabled = (reminderPref != null) ? reminderPref : false;
                    }
                    if (!remindersEnabled) {
                        reminderList.clear();
                        notifAdapter.updateData(reminderList);
                        Log.d("RemindersFragment", "Reminders are disabled");
                    } else {

                        //Fetch the reminders
                        db.collection("users")
                                .document(Objects.requireNonNull(userId))  // Point to the specific user document
                                .collection("reminders")  // Fetch from the "reminders" subcollection of that user
                                .addSnapshotListener((querySnapshot, queryError) -> {

                                    if (queryError != null) {
                                        Log.e("RemindersFragment", "Error fetching reminders: ", queryError);
                                        return;
                                    }
                                    if (querySnapshot != null) {
                                        reminderList.clear();
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            // Convert the document to a ReminderItem object
                                            ReminderItem reminderItem = document.toObject(ReminderItem.class);
                                            reminderList.add(reminderItem);
                                        }
                                        // Log the fetched reminders
                                        Log.d("RemindersFragment", "Fetched " + reminderList.size() + " reminders");
                                        notifAdapter.updateData(reminderList);
                                    }

                                });
                    }
                });
    }
}
