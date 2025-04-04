package com.drinkwise.app.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// ViewModel for the NotificationsFragment. This ViewModel is responsible for fetching reminders from Firestore and exposing them as LiveData.
public class NotificationsViewModel extends ViewModel {

    // MutableLiveData that holds a String representing the reminders' text.
    private final MutableLiveData<String> mText;
    // Firestore instance for database operations.
    private final FirebaseFirestore db;

    // Constructor: initializes the MutableLiveData and Firestore instance, then calls loadReminders() to fetch reminder data.
    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        loadReminders();
    }

    // Returns the LiveData containing the reminders text.
    public LiveData<String> getText() {
        return mText;
    }

    // loadReminders() fetches reminder documents from the "reminders" collection in Firestore,
    // orders them by timestamp in descending order, and updates mText with a formatted string.
    private void loadReminders() {
        db.collection("reminders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException e) -> {
                    // If there is an error, update mText with an error message.
                    if (e != null) {
                        mText.setValue("Error loading reminders.");
                        return;
                    }
                    // StringBuilder to build the display text for reminders.
                    StringBuilder sb = new StringBuilder();
                    // SimpleDateFormat for formatting timestamps.
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // Convert Firestore documents into a list of ReminderItem objects.
                        List<ReminderItem> reminderList = querySnapshot.toObjects(ReminderItem.class);
                        for (ReminderItem item : reminderList) {
                            sb.append(item.getReminderType())
                                    .append(": ")
                                    .append(item.getMessage())
                                    .append(" (")
                                    .append(sdf.format(item.getTimestamp().toDate()))
                                    .append(")\n\n");
                        }
                        mText.setValue(sb.toString());
                    } else {
                        // If no reminders are found, set a default message.
                        mText.setValue("No reminders available.");
                    }
                });
    }
}