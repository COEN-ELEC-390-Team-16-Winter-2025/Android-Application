package com.drinkwise.app.ui.notifications;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

public class ReminderManager {

    private static final String TAG = "ReminderManager";
    private static ReminderManager instance;

    private final FirebaseFirestore db;
    private String userId;
    private final Handler handler;
    private Runnable reminderRunnable;

    private ReminderManager(Context context) {
        Context context1 = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            this.userId = user.getUid();
        }
        this.handler = new Handler(Looper.getMainLooper());
    }

    public static ReminderManager getInstance(Context context) {
        if(instance == null) {
            instance = new ReminderManager(context);
        }
        return instance;
    }

    public void startReminders() {
        reminderRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndGenerateReminders();
                handler.postDelayed(this, 5 * 60 * 1000);
            }
        };
        handler.post(reminderRunnable);
    }

    public void stopReminders() {
        handler.removeCallbacks(reminderRunnable);
        Log.d(TAG, "Reminders stopped.");
    }

    private void checkAndGenerateReminders() {
        // 1) Check BAC for follow-up reminders
        retrieveCurrentBAC(bac -> {
            createBACFollowUpReminder(bac);
            // 2) Late Night Safe Check
            if (isLateNight() && bac >= 0.08) {
                createReminder(
                        "Late Night Safe Check",
                        "Recheck your BAC before bed to ensure safety.",
                        0,
                        "Low"
                );
            }
        });

        // 3) Drinking Confirmation
        checkNoDrinksLoggedFor(noDrinksLogged -> {
            if (noDrinksLogged) {
                createReminder(
                        "Drinking Confirmation",
                        "Haven't logged anything in a while. Are you still drinking?",
                        60,
                        "Info"
                );
            }
        });
    }

    /**
     * Determines which reminder to create based on the user's current BAC.
     * - Safe (< 0.02)
     * - Mild Impairment (< 0.05)
     * - Impaired (< 0.08)
     * - High Impairment (< 0.15)
     * - Severe Impairment (< 0.30)
     * - Medical Emergency (>= 0.30)
     */
    private void createBACFollowUpReminder(double bac) {
        if (bac < 0.02) {
            // Safe
            createReminder(
                    "Follow-up Reminder: Safe",
                    "Still sober? Come back and check!",
                    30, // 30 minutes
                    "Low"
            );
        } else if (bac < 0.05) {
            // Mild Impairment
            createReminder(
                    "Follow-up Reminder: Mild Impairment",
                    "You registered as impaired. Come recheck to see how you've changed.",
                    30,
                    "Low"
            );
        } else if (bac < 0.08) {
            // Impaired
            createReminder(
                    "Follow-up Reminder: Impaired",
                    "You're highly impaired. Recheck your BAC to stay aware of your level.",
                    20,
                    "Medium"
            );
        } else if (bac < 0.15) {
            // High Impairment
            createReminder(
                    "Follow-up Reminder: High Impairment",
                    "Significant impairment detected! Recheck your BAC to ensure your level.",
                    10,
                    "Medium"
            );
        } else if (bac < 0.30) {
            // Severe Impairment
            createReminder(
                    "Follow-up Reminder: Severe Impairment",
                    "Dangerous intoxication detected. Recheck your BAC to understand your current condition!",
                    10,
                    "Urgent"
            );
        } else {
            // Medical Emergency
            createReminder(
                    "Follow-up Reminder: Medical Emergency",
                    "Critical BAC level detected! Recheck your BAC!",
                    10,
                    "Emergency"
            );
        }
    }
    // Creates and writes a reminder to Firestore
    private void createReminder(String reminderType, String message, int intervalMinutes, String escalation) {
        if (userId == null) return;
        ReminderItem reminder = new ReminderItem();
        reminder.setReminderType(reminderType);
        reminder.setMessage(message);
        reminder.setTimestamp(new Timestamp(new Date()));
        reminder.setIntervalMinutes(intervalMinutes);
        reminder.setStatus("active");
        reminder.setEscalation(escalation);

        Log.d(TAG, "Creating reminder: " + reminderType + " (escalation: " + escalation + ")");

        db.collection("reminders").add(reminder)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Reminder created: " + reminderType))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating reminder", e));
    }

    // Checks if no drinks logged for X minutes
    private void checkNoDrinksLoggedFor(final NoDrinksCallback callback) {
        if(userId == null) {
            callback.onResult(false);
            return;
        }
        long thresholdMillis = System.currentTimeMillis() - ((long) 60 * 60 * 1000);
        Timestamp thresholdTimestamp = new Timestamp(new Date(thresholdMillis));
        db.collection("users")
                .document(userId)
                .collection("manual_drink_logs")
                .whereGreaterThan("timestamp", thresholdTimestamp)
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> callback.onResult(snapshot.isEmpty()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking drink logs", e);
                    callback.onResult(false);
                });
    }

    private boolean isLateNight() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour < 6;
    }

    // Async retrieval of current BAC from Firestore
    public interface BACCallback {
        void onBACRetrieved(double bac);
    }

    private void retrieveCurrentBAC(final BACCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onBACRetrieved(0.0);
            return;
        }
        db.collection("users")
                .document(user.getUid())
                .collection("profile")
                .document("stats")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double bac = documentSnapshot.getDouble("currentBAC");
                        if (bac == null) bac = 0.0;
                        callback.onBACRetrieved(bac);
                    } else {
                        callback.onBACRetrieved(0.0);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving BAC", e);
                    callback.onBACRetrieved(0.0);
                });
    }

    // Async callback for no drinks check
    public interface NoDrinksCallback {
        void onResult(boolean noDrinksLogged);
    }
}