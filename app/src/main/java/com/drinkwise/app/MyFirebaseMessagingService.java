package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import android.content.pm.ServiceInfo;

import android.content.Intent;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_DEBUG";
    private static final String CHANNEL_ID = "drinkwise_alerts_channel";
    private ListenerRegistration recommendationListener;
    private FirebaseFirestore db;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FCM Service created");
        // Initialize components but don't make it foreground
        db = FirebaseFirestore.getInstance();
        createNotificationChannel();
        setupRecommendationListener();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        // Add this to MainActivity.onCreate() after listener setup:
//        db.collection("users").document(user.getUid()).collection("Recommendations")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    Log.d(TAG, "📂 Existing recommendations count: " + querySnapshot.size());
//                    for (QueryDocumentSnapshot doc : querySnapshot) {
//                        Log.d(TAG, "📄 Doc: " + doc.getId() + " - " + doc.getData());
//                    }
//                });
    }

    // Add this new method
    public static void safeStart(Context context) {
        try {
            Intent intent = new Intent(context, MyFirebaseMessagingService.class);
            context.startService(intent); // Regular service start
            Log.d(TAG, "Service started safely");
        } catch (Exception e) {
            Log.e(TAG, "Service start failed", e);
        }
    }


    @Override
    public void onDestroy() {
        if (recommendationListener != null) {
            recommendationListener.remove();
        }
        super.onDestroy();
    }

    private void setupRecommendationListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "❌ No authenticated user - skipping listener setup");
            return;
        }
        Log.d(TAG, "✅ User authenticated: " + user.getUid());

        String collectionPath = "users/" + user.getUid() + "/Recommendations";
        Log.d(TAG, "🔍 Setting up listener on collection: " + collectionPath);

//        recommendationListener = db.collection("users")
//                .document(user.getUid())
//                .collection("Recommendations")
//                .whereEqualTo("Resolved", false)  // Only unresolved docs
//                .orderBy("Timestamp", Query.Direction.DESCENDING)
//                .limit(1)
//                .addSnapshotListener((snapshots, error) -> {
//                    Log.d(TAG, "📡 Snapshot received at " + System.currentTimeMillis());
//
//                    if (error != null) {
//                        Log.e(TAG, "❗ Listener error", error);
//                        return;
//                    }
//
//                    if (snapshots == null) {
//                        Log.d(TAG, "🔄 Snapshot is null (initial state)");
//                        return;
//                    }
//
//                    Log.d(TAG, "📊 Snapshot metadata: " +
//                            "hasPendingWrites=" + snapshots.getMetadata().hasPendingWrites() +
//                            ", fromCache=" + snapshots.getMetadata().isFromCache());
//
//                    Log.d(TAG, "📝 Document changes count: " + snapshots.getDocumentChanges().size());
//                    Log.d(TAG, "📋 Full documents count: " + snapshots.size());
//
//                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
//                        Log.d(TAG, "🔄 Document change type: " + dc.getType() +
//                                ", doc ID: " + dc.getDocument().getId());
//
//                        if (dc.getType() == DocumentChange.Type.ADDED) {
//                            boolean resolved = dc.getDocument().getBoolean("Resolved");
//                            Log.d(TAG, "📄 Document contents: " +
//                                    "Resolved=" + resolved +
//                                    ", Data=" + dc.getDocument().getData());
//
//                            if (!resolved) {
//                                Log.d(TAG, "🎯 New unresolved recommendation detected");
//                                handleNewRecommendation(dc.getDocument());
//                            } else {
//                                Log.d(TAG, "⏭ Recommendation already resolved, skipping");
//                            }
//                        }
//                    }
//                });

        // Add this test code temporarily
        db.collection("users")
                .document(user.getUid())
                .collection("Recommendations")
                .whereEqualTo("Resolved", false)  // Only unresolved recommendations
                .orderBy("Timestamp", Query.Direction.DESCENDING)  // Newest first
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Rules test passed - Found " + task.getResult().size() + " recommendations");

                        if (!task.getResult().isEmpty()) {
                            QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            Log.d(TAG, "📄 Recommendation: " + doc.getData());

                            // 1. Mark as resolved immediately
                            doc.getReference().update("Resolved", true)
                                    .addOnSuccessListener(aVoid -> {
                                        // 2. Only send notification after successful resolve
                                        String message = doc.getString("Message");
                                        Long drinkCount = doc.getLong("DrinkCount");

                                        String notificationMsg = message != null ? message :
                                                "You've had " + (drinkCount != null ? drinkCount : 0) + " drinks";

                                        sendNotification("Drink Recommendation", notificationMsg);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "❌ Failed to mark as resolved", e);
                                    });
                        }
                    } else {
                        Log.e(TAG, "❌ Rules test failed", task.getException());

                        // Specific error handling
                        if (task.getException() instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException e = (FirebaseFirestoreException) task.getException();
                            if (e.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                Log.e(TAG, "🔐 Permission denied - Check Firestore security rules");
                            } else if (e.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                                Log.e(TAG, "⚠️ Index is building - Please wait or create the index manually");
                                // You could add a retry mechanism here
                            }
                        }
                    }
                });

        Log.d(TAG, "👂 Listener successfully registered");
    }
    private void handleNewRecommendation(DocumentSnapshot doc) {
        try {
            Log.d(TAG, "🛠 Processing new recommendation");

            String message = doc.getString("Message");
            Long drinkCount = doc.getLong("DrinkCount");
            Timestamp timestamp = doc.getTimestamp("Timestamp");

            Log.d(TAG, "📦 Recommendation data: " +
                    "Message=" + message +
                    ", DrinkCount=" + drinkCount +
                    ", Timestamp=" + (timestamp != null ? timestamp.toDate() : "null"));

            String title = "Drink Recommendation";
            String body = message != null ? message :
                    String.format(Locale.getDefault(),
                            "You've had %d drinks. Consider your next steps.",
                            drinkCount != null ? drinkCount : 0);

            Log.d(TAG, "💬 Notification content prepared: " + title + " - " + body);
            sendNotification(title, body);

            // Uncomment to mark as resolved after sending
            // doc.getReference().update("Resolved", true)
            //     .addOnSuccessListener(__ -> Log.d(TAG, "✔ Marked as resolved"))
            //     .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to mark resolved", e));

        } catch (Exception e) {
            Log.e(TAG, "❗ Error processing recommendation", e);
        }
    }

    private void sendNotification(String title, String message) {
        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) {
                Log.e(TAG, "❗ NotificationManager is null");
                return;
            }

            // Create notification (ensure channel exists)
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            manager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "🔔 Notification sent: " + message);

        } catch (Exception e) {
            Log.e(TAG, "❗ Notification failed", e);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            if (remoteMessage.getData().containsKey("type") &&
                    "recommendation".equals(remoteMessage.getData().get("type"))) {
                handleRemoteRecommendation(remoteMessage.getData());
            }
        }

        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message body: " + remoteMessage.getNotification().getBody());
            sendNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    private void handleRemoteRecommendation(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String drinkCount = data.get("drinkCount");

        if (message == null && drinkCount != null) {
            message = String.format(Locale.getDefault(),
                    "You've had %s drinks. Consider your next steps.",
                    drinkCount);
        }

        sendNotification(
                title != null ? title : "Drink Recommendation",
                message != null ? message : "New drink recommendation available"
        );
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        sendTokenToServer(token);
    }

    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        if ("recommendation".equals(type)) {
            String title = data.get("title");
            String message = data.get("message");
            sendNotification(title != null ? title : "New Recommendation",
                    message != null ? message : "You have a new drink recommendation");
        }
    }

//    private void sendNotification(String title, String messageBody) {
//        createNotificationChannel(); // Ensure channel exists
//
//        try {
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Ensure this exists
//                    .setContentTitle(title != null ? title : "New Message")
//                    .setContentText(messageBody)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setAutoCancel(true);
//
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            if (manager != null) {
//                manager.notify((int) System.currentTimeMillis(), builder.build());
//                Log.d(TAG, "Notification displayed");
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Notification failed", e);
//        }
//    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "DrinkWise Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Important drink recommendations");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void sendTokenToServer(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .set(Collections.singletonMap("fcmToken", token), SetOptions.merge())
                    .addOnFailureListener(e -> Log.w(TAG, "Token update failed", e));
        }
    }
}