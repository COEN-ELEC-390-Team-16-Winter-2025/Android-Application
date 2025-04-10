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

// ViewModel for the NotificationsFragment. This ViewModel is responsible for getting reminders from Firestore and showing them as LiveData.
public class NotificationsViewModel extends ViewModel {

    // holds a string that represents the reminders' text.
    private final MutableLiveData<String> mText;
    private final FirebaseFirestore db;

    // constructor for the viewmodel
    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        loadReminders();
    }

    // returns the LiveData containing the reminders text
    public LiveData<String> getText() {
        return mText;
    }

    // loadReminders() gets reminder documents from the "reminders" collection in Firestore orders them by timestamp in descending order and updates mText with a formatted string.
    private void loadReminders() {
        db.collection("reminders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException e) -> {
                    // If error update mText with an error message
                    if (e != null) {
                        mText.setValue("Error loading reminders.");
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
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
                        mText.setValue("No reminders available.");
                    }
                });
    }
}