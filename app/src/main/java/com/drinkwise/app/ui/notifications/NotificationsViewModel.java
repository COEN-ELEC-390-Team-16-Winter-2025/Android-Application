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
    }

    // Returns the LiveData containing the reminders text.
    public LiveData<String> getText() {
        return mText;
    }

}