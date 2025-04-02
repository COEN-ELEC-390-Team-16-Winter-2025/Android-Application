package com.drinkwise.app;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;

public class FCMNotificationSender {

    private static final String TAG = "FCMNotificationSender";

    // Initialize Firebase Admin SDK with the service account
    public static void initializeFirebase() {
        try {
            // Use the relative path to the service account JSON file
            FileInputStream serviceAccount = new FileInputStream("app/bac-tracker-app-firebase-adminsdk-fbsvc-3f3a842f28.json"); // Relative path to your JSON file

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setProjectId("bac-tracker-app") // Your Firebase project ID
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing Firebase SDK", e);
        }
    }

    // Send Notification
    public static void sendNotification(String deviceToken) {
        try {
            // Create a message with data payload
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(new Notification("New Recommendation", "You have a new drinking recommendation!"))
                    .putData("drinkCount", "5")
                    .putData("resolved", "false")
                    .build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            Log.d(TAG, "Successfully sent message: " + response);

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification", e);
        }
    }

    public static void main(String[] args) {
        initializeFirebase(); // Initialize Firebase with service account
        sendNotification("YOUR_DEVICE_FCM_TOKEN");  // Replace with actual device token
    }
}
