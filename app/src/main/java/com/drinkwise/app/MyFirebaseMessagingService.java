package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.androidbrowserhelper.locationdelegation.PermissionRequestActivity;
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
import android.Manifest;



/*
 * Notification Handling in This Class:
 *
 * 1. **Hardcoded (Local) Notifications**:
 *    - These notifications are triggered by internal app events or database changes.
 *    - Examples include reminders, alerts, and drink recommendations.
 *    - Notifications are built and shown locally using methods like `sendNotification()`, `sendAlertNotification()`, and `sendReminderNotification()`.
 *    - No external services are needed for these notifications to be triggered.
 *
 * 2. **Remote (FCM) Notifications**:
 *    - FCM (Firebase Cloud Messaging) is used to receive notifications sent from external servers (such as alerts or updates from a backend).
 *    - When a remote message is received (via `onMessageReceived()`), the notification is processed and displayed locally using similar notification methods.
 *    - This allows the app to react to external events and alert the user even when the app is in the background.
 *
 * 3. **Why Use Both?**:
 *    - **Local notifications** are used for in-app events and immediate responses based on user actions or database updates.
 *    - **Remote notifications** allow the app to receive updates from the server or backend and notify the user when external events occur (e.g., new alerts or server-triggered reminders).
 *
 * The combination of local and remote notification handling enables the app to manage notifications both internally and externally, ensuring that users stay informed in real-time.
 */



public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_DEBUG";
    private static final String CHANNEL_ID = "drinkwise_alerts_channel";
    private static final String ALERTS_CHANNEL_ID = "drinkwise_critical_alerts";
    private static final String REMINDERS_CHANNEL_ID = "drinkwise_reminders_channel";
    private ListenerRegistration recommendationListener;
    private ListenerRegistration alertListener;
    private ListenerRegistration reminderListener;

    private String lastNotificationMessage;

    private FirebaseFirestore db;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FCM Service created");
        db = FirebaseFirestore.getInstance();
        createNotificationChannels();
        setupRecommendationListener();
        setupAlertListener();
        setupReminderListener();
    }


    // for main. service had issue starting so had to force call
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
        super.onDestroy();
    }

     // define the channels and register themm with the system's notification manager
    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Regular notifications channel
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "DrinkWise Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("General drink recommendations");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            // alerts channel
            NotificationChannel criticalChannel = new NotificationChannel(
                    ALERTS_CHANNEL_ID,
                    "DrinkWise Safety Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            criticalChannel.setDescription("Critical safety alerts");
            criticalChannel.enableLights(true);
            criticalChannel.setLightColor(Color.RED);
            criticalChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            criticalChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            criticalChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);

            // Add reminders channel
            NotificationChannel remindersChannel = new NotificationChannel(
                    REMINDERS_CHANNEL_ID,
                    "DrinkWise Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            remindersChannel.setDescription("Scheduled reminders");
            remindersChannel.enableVibration(true);
            remindersChannel.setVibrationPattern(new long[]{100, 200, 100});
            remindersChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(criticalChannel);
            manager.createNotificationChannel(remindersChannel);

            Log.d(TAG, "Created channels: " + CHANNEL_ID + ", " +
                    ALERTS_CHANNEL_ID + ", and " + REMINDERS_CHANNEL_ID);
        }
    }

    // Set up a real-time listener to monitor
    // checks for unresolved recommendations (resolved when closed)
    private void setupRecommendationListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No authenticated user - skipping recommendation listener");
            return;
        }

        recommendationListener = db.collection("users")
                .document(user.getUid())
                .collection("Recommendations")
                .whereEqualTo("Resolved", false)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Recommendation listener error", error);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                handleNewRecommendation(dc.getDocument());
                            }
                        }
                    }
                });
    }

    // Handles a new recommendation, updates its status, and sends a notification to the user
    private void handleNewRecommendation(DocumentSnapshot doc) {

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);

        Log.d(TAG, "Channel exists: " + (channel != null));
        if (channel != null) {
            Log.d(TAG, String.format(
                    "Channel settings:\nImportance: %d\nVibration: %b\nSound: %s",
                    channel.getImportance(),
                    channel.shouldVibrate(),
                    channel.getSound()
            ));
        }

        doc.getReference().update("Resolved", true)
                .addOnSuccessListener(aVoid -> {
                    String message = doc.getString("Message");
                    Long drinkCount = doc.getLong("DrinkCount");

                    String notificationMsg = message != null ? message :
                            "You've had " + (drinkCount != null ? drinkCount : 0) + " drinks";

                    sendNotification("Drink Recommendation", notificationMsg);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark recommendation as resolved", e);
                });
    }

    // Set up a listener for new safety alerts, updates their status, and triggers notifications for unresolved alerts.
    private void setupAlertListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No authenticated user - skipping alert listener");
            return;
        }

        alertListener = db.collection("users")
                .document(user.getUid())
                .collection("Alerts")
                .whereEqualTo("Resolved", false)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Alert listener error", error);
                        return;
                    }

                    Log.d(TAG, "ALERT SNAPSHOT RECEIVED. Changes: " +
                            (snapshots != null ? snapshots.getDocumentChanges().size() : "null"));

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Log.d(TAG, "CHANGE TYPE: " + dc.getType() +
                                    " Doc: " + dc.getDocument().getData());

                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                handleNewAlert(dc.getDocument());
                            }
                        }
                    }
                });

        Log.d(TAG, "ALERT LISTENER INITIALIZED for user: " + user.getUid());

    }

    // Handle new alerts by marking it as resolved and triggering a notification based on alert details.
    private void handleNewAlert(DocumentSnapshot doc) {
        Log.d(TAG, "NEW ALERT DETECTED: " + doc.getData());


        doc.getReference().update("Resolved", true)
                .addOnSuccessListener(aVoid -> {
                    String message = doc.getString("Message");
                    String safetyLevel = doc.getString("SafetyLevel");
                    Double bacValue = doc.getDouble("bacValue");
                    String escalationLevel = doc.getString("EscalationLevel");

                    String notificationMsg = buildAlertMessage(message, safetyLevel, bacValue);
                    sendAlertNotification("Safety Alert", notificationMsg, escalationLevel);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark alert as resolved", e);
                });
    }

    // Set up listener for active reminders and handles new reminders when they are added.
    private void setupReminderListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No authenticated user - skipping reminder listener");
            return;
        }

        reminderListener = db.collection("users")
                .document(user.getUid())
                .collection("reminders")
                .whereEqualTo("status", "active")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Reminder listener error", error);
                        return;
                    }

                    Log.d(TAG, "REMINDER SNAPSHOT RECEIVED. Changes: " +
                            (snapshots != null ? snapshots.getDocumentChanges().size() : "null"));

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                handleNewReminder(dc.getDocument());
                            }
                        }
                    }
                });

        Log.d(TAG, "REMINDER LISTENER INITIALIZED for user: " + user.getUid());
    }

    // Handle new reminders by displaying a notification and marking it as completed in the database.
    private void handleNewReminder(DocumentSnapshot doc) {
        Log.d(TAG, "NEW REMINDER DETECTED: " + doc.getData());

        // Early exit if already marked as completed
        if ("completed".equals(doc.getString("status"))) {
            Log.d(TAG, "Reminder already completed, skipping.");
            return;
        }

        String message = doc.getString("message");
        String reminderType = doc.getString("reminderType");
        String escalation = doc.getString("escalation");

        // Show notification immediately
        sendReminderNotification(
                reminderType != null ? reminderType : "Reminder",
                message != null ? message : "New reminder",
                escalation
        );

        // Update status without delay (removed Handler.postDelayed)
        doc.getReference().update("status", "completed")
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Reminder marked as completed."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update reminder status", e));
    }

    // Sends a reminder notification with a title, message, and escalation level
    // The notification is displayed as in apps notifs but does not drop down when ap is open (only sound in this case)
    private void sendReminderNotification(String title, String message, String escalation) {
        if (TextUtils.isEmpty(message)) return;

        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) return;

            // Create intent to open app when notification is tapped
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build the notification with heads-up display
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, REMINDERS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{0, 250, 250, 250})
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setFullScreenIntent(pendingIntent, true); // NEEDED FOR POP-UPS

            // Show notification with unique ID
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());

            Log.d(TAG, "Reminder pop-up notification displayed with ID: " + notificationId);

        } catch (Exception e) {
            Log.e(TAG, "Reminder notification failed", e);
        }
    }

    // Builds a formatted alert message
    private String buildAlertMessage(String message, String safetyLevel, Double bacValue) {
        StringBuilder builder = new StringBuilder();
        if (message != null) {
            builder.append(message);
        }
        if (bacValue != null) {
            builder.append("\nBAC: ").append(String.format(Locale.getDefault(), "%.3f", bacValue));
        }
        if (safetyLevel != null) {
            builder.append("\nLevel: ").append(safetyLevel);
        }
        return builder.toString();
    }

    // Send a notification with the provided title and message
    // check for duplicate messages are not sent consecutively
    // The notification opens the MainActivity when tapped, and it automatically cancels once the user interacts with it.
    private void sendNotification(String title, String message) {
        if (TextUtils.isEmpty(message) || message.equals(lastNotificationMessage)) {
            return;
        }
        lastNotificationMessage = message;

        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) return;

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            manager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "Notification sent: " + message);

        } catch (Exception e) {
            Log.e(TAG, "Notification failed", e);
        }
    }



    // Send an alert notification
    // Depending on the escalation level, the notification may have different colors and vibration patterns
    // Critical alerts trigger a full-screen notification and direct the user to the MainActivity with a flag to display alerts

    private void sendAlertNotification(String title, String message, String escalationLevel) {
        if (TextUtils.isEmpty(message)) return;

        Log.d(TAG, "Attempting to send alert. Channel: " + ALERTS_CHANNEL_ID +
                " Title: " + title + " Message: " + message);

        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) return;

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("show_alerts", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            int color = Color.RED;
            if (escalationLevel != null && escalationLevel.equals("Low")) {
                color = Color.YELLOW;
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERTS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setColor(color)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{0, 500, 200, 500});

            if (escalationLevel != null && (escalationLevel.equals("Medium") || escalationLevel.equals("High Impairment") || escalationLevel.equals("Severe Impairment") || escalationLevel.equals("Medical Emergency"))) {
                builder.setFullScreenIntent(pendingIntent, true);
            }

            manager.notify("ALERT_" + System.currentTimeMillis(),
                    (int) System.currentTimeMillis(),
                    builder.build());

            Log.d(TAG, "Alert notification sent: " + message);

        } catch (Exception e) {
            Log.e(TAG, "Alert notification failed", e);
        }
    }


    // triggered when a message is received from Firebase Cloud Messaging.
    // checks if the message contains data and handles different types of messages (recommendation or alert) accordingly.
    // It also sends a notification if the message contains a notification payload.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MyFirebaseMessagingService.class));
        }

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            if (remoteMessage.getData().containsKey("type")) {
                String type = remoteMessage.getData().get("type");
                if ("recommendation".equals(type)) {
                    handleRemoteRecommendation(remoteMessage.getData());
                } else if ("alert".equals(type)) {
                    handleRemoteAlert(remoteMessage.getData());
                }
            }
        }

        if (remoteMessage.getNotification() != null) {
            sendNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    // Handle and send a recommendation notification based on remote data.
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

    // Handle and send a alert notification based on remote data.
    private void handleRemoteAlert(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String safetyLevel = data.get("safetyLevel");
        String bacValue = data.get("bacValue");
        String escalationLevel = data.get("escalationLevel");

        String escLevel = null;
        try {
            escLevel = escalationLevel;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid escalation level", e);
        }

        Double bac = null;
        try {
            bac = Double.parseDouble(bacValue);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid BAC value", e);
        }

        String notificationMsg = buildAlertMessage(message, safetyLevel, bac);
        sendAlertNotification(
                title != null ? title : "Safety Alert",
                notificationMsg,
                escLevel
        );
    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        sendTokenToServer(token);
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