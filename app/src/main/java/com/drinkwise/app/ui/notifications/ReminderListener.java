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

// ReminderListener listens to changes in the "reminders" collection in Firestore and it displays local notifications when new reminders are added
public class ReminderListener {

    private final Context context;
    private final FirebaseFirestore db;

    // constructor
    public ReminderListener(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        createNotificationChannel();
    }

    //listens for reminders for a user ID
    public void startListening(String userId) {
        db.collection("reminders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) -> {
                    // If error occurs return without doing anything
                    if (e != null) {
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        snapshot.getDocuments().forEach(document -> {
                            String message = document.getString("message");
                            String reminderType = document.getString("reminderType");
                            showNotification(reminderType, message);
                        });
                    }
                });
    }

    // builds/displays a notification with title and message.
    private void showNotification(String title, String message) {
        //Check first if toggle is on or off before showing reminders
        //Check if recommendations toggle is off or on before showing the pop-up
       // if(!reminders) {
         //   Log.d("Reminder listener", "Reminder toggle is off, not showing the pop-up.");
           // return;
       // }


        // This creates a notification builder with the channel
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "reminder_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } else {
            Log.w("ReminderListener", "POST_NOTIFICATIONS permission not granted.");
        }
    }

    // This creates a notification channel for reminders (I have learned that it is required for Android Oreo and above).
    private void createNotificationChannel() {
        CharSequence name = "Reminders";
        String description = "Channel for BAC and Drinking Status Reminders";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("reminder_channel", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}