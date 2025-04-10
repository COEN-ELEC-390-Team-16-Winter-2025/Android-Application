package com.drinkwise.app.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drinkwise.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //Adding a separation line to the recommendations
        DividerItemDecoration dividerItemDecoration =  new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        notifAdapter = new NotifAdapter(getContext(), reminderList);
        recyclerView.setAdapter(notifAdapter);

        boolean showReminders = true;
        if(getArguments() != null) {
            showReminders = getArguments().getBoolean("showRecommendations", true);
        }

        if(showReminders) {
            loadReminders();
        } else {
            reminderList.clear();
            notifAdapter.updateData(reminderList);
        }
        return root;
    }
    //The on pause method would cause the notification to stop being displayed when going back to it
//    @Override
//    public void onPause() {
//        super.onPause();
//        //readReminders();
//    }

    private void loadReminders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("RemindersFragment", "No user logged in");
            return;
        }

        Calendar calendar = Calendar.getInstance();

        Date endOfDay = calendar.getTime();

        // StartOfDay = 2 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        // Get end of 2 days ago

        //get preferences
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

                        //get the reminders
                        db.collection("users")
                                .document(Objects.requireNonNull(userId))  // Point to the specific user document
                                .collection("reminders")  // collect from the "reminders" collection of that user
                                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                                .whereLessThanOrEqualTo("timestamp", endOfDay)
                                .addSnapshotListener((querySnapshot, queryError) -> {

                                    if (queryError != null) {
                                        Log.e("RemindersFragment", "Error fetching reminders: ", queryError);
                                        return;
                                    }
                                    if (querySnapshot != null) {
                                        reminderList.clear();
                                        int unreadCount = 0;
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                            // ReminderItem object
                                            ReminderItem reminderItem = document.toObject(ReminderItem.class);
                                            reminderItem.setId(document.getId());

                                            reminderList.add(reminderItem);
                                            if(reminderItem.getResolved() == null || !reminderItem.getResolved()) {
                                                unreadCount++;
                                            }
                                        }
                                        if(unreadCount > 0 && unreadCount < reminderList.size()) {
                                            SeparatorItem separator = new SeparatorItem();
                                            reminderList.add(unreadCount, separator);
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
