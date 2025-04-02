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

        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");
            sendNotification(title, message);
        }
    }

    private void sendNotification(String title, String messageBody) {
        Log.d("FCMService", "Sending notification with title: " + title + " and message: " + messageBody);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel";
            CharSequence channelName = "Default Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

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

        // Get the current authenticated user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // If the user is logged in
        if (user != null) {
            // Save the token to Firestore under the user's document
            String userId = user.getUid();
            db.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FCMService", "FCM token saved successfully for user: " + userId);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("FCMService", "Error saving FCM token", e);
                    });
        }
    }

}
