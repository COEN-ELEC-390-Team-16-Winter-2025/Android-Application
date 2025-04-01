package com.drinkwise.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
     public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
         Log.d("FCMService", "onMessageReceived triggered");


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("FCMService", "User is not authenticated");
            return;
        }
        String userId = user.getUid();  // Use the current user's UID

        // Fetch latest recommendation for the user from Firestore
        db.collection("users")
                .document(userId)
                .collection("Recommendations")
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .limit(45)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("FCMService", "Query success, documents: " + queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        int drinkCount = document.getLong("DrinkCount").intValue();
                        String message = document.getString("Message");
                        boolean resolved = document.getBoolean("Resolved");

                        Log.d("FCMService", "Fetched recommendation: " + message);



                        // Send notification only if the recommendation is not resolved
                        if (!resolved) {
                            sendNotification("New Recommendation", message);
                            // Optionally, you can update the 'Resolved' field to mark the recommendation as shown
                            document.getReference().update("Resolved", true);
                        }
                    } else {
                        Log.d("FCMService", "No recommendations found.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FCMService", "Error fetching recommendations", e));
    }

    private void sendNotification(String title, String messageBody) {
        Log.d("FCMService", "Sending notification with title: " + title + " and message: " + messageBody);

        // Create the notification channel if necessary (for devices >= Android Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel";
            CharSequence channelName = "Default Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Create the notification with the specified icon
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)  // Set your notification icon here
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Log the notification details before sending it
        Log.d("FCMService", "Notification built, now sending.");

        // Notify the user
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }



    @Override
    public void onNewToken(String token) {
        // Handle the new token
        super.onNewToken(token);
        Log.d("FCMService", "New token: " + token);

    }
}
