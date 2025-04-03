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

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null) {
            loadReminders(userId);
        } else {
            Log.e("RemindersFragment", "No user logged in");
        }

        return root;
    }

    private void loadReminders(String userId) {
        db.collection("users")
                .document(Objects.requireNonNull(userId))  // Point to the specific user document
                .collection("reminders")  // Fetch from the "reminders" subcollection of that user
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Convert the document to a ReminderItem object
                            ReminderItem reminderItem = document.toObject(ReminderItem.class);
                            reminderList.add(reminderItem);
                        }

                        // Log the fetched reminders
                        Log.d("NotificationsFragment", "Fetched " + reminderList.size() + " reminders");

                        // Set up the adapter with the reminder data
                        notifAdapter = new NotifAdapter(getContext(), reminderList);
                        recyclerView.setAdapter(notifAdapter);
                    } else {
                        Log.e("NotificationsFragment", "Error fetching reminders: ", task.getException());
                    }
                });
    }



}
