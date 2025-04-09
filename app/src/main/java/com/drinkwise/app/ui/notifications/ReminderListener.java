package com.drinkwise.app.ui.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

// ReminderListener listens to changes in the "reminders" collection in Firestore and displays local notifications when new reminders are added.
public class ReminderListener {

    // Context to access resources and system services
    private final Context context;
    // Firestore instance to query the database
    private final FirebaseFirestore db;

    // Constructor: Initializes the context, Firestore instance, and creates a notification channel.
    public ReminderListener(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        createNotificationChannel();
    }

    // Starts listening for reminders for a given user ID.
    // When reminders are updated in Firestore, it triggers notifications.
    public void startListening(String userId) {
        db.collection("reminders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) -> {
                    // If an error occurs, simply return without doing anything.
                    if (e != null) {
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        // For each document, extract the message and reminder type, then show a notification.
                        snapshot.getDocuments().forEach(document -> {
                            String message = document.getString("message");
                            String reminderType = document.getString("reminderType");
                            showNotification(reminderType, message);
                        });
                    }
                });
    }

    // Builds and displays a notification with the provided title and message.
    private void showNotification(String title, String message) {
        //Check first if toggle is on or off before showing reminders
        //Check if recommendations toggle is off or on before showing the pop-up
       // if(!reminders) {
         //   Log.d("Reminder listener", "Reminder toggle is off, not showing the pop-up.");
           // return;
       // }


        // Create a notification builder with the specified channel.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Using built-in icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Dismiss notification on tap

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Check if the POST_NOTIFICATIONS permission is granted.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } else {
            Log.w("ReminderListener", "POST_NOTIFICATIONS permission not granted.");
        }
    }

    // Creates a notification channel for reminders (it is required for Android Oreo and above).
    private void createNotificationChannel() {
        CharSequence name = "Reminders"; // Name of the channel
        String description = "Channel for BAC and Drinking Status Reminders"; // Channel description
        int importance = NotificationManager.IMPORTANCE_HIGH; // Importance level of the channel
        NotificationChannel channel = new NotificationChannel("reminder_channel", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}