package com.drinkwise.app;

import android.Manifest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.drinkwise.app.databinding.ActivityMainBinding;
import com.drinkwise.app.ui.notifications.ReminderListener;
import com.drinkwise.app.ui.notifications.ReminderManager;
import com.drinkwise.app.MyFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import java.util.Collections;


// MainActivity is the central activity of the app. It sets up the action bar,
// navigation components, and initializes Firebase along with reminders.
public class MainActivity extends AppCompatActivity {

    // ViewBinding instance to access views in activity_main.xml.
    private ActivityMainBinding binding;
    // TextView for the custom action bar title.
    private TextView actionBarTitle;
    String TAG = "FCM_DEBUG";
    private static final int NOTIFICATION_POLICY_ACCESS_REQUEST_CODE = 1001;
    private static final String CHANNEL_ID = "drinkwise_alerts_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase.
        FirebaseApp.initializeApp(this);
        Log.d("FCM_DEBUG", "Firebase initialized in Main");
        // Start the service manually
        MyFirebaseMessagingService.safeStart(this);

        // Get SharedPreferences and check if this is the first time the app is launched.
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean firstTime = preferences.getBoolean("firstTime", true);

        Log.d("FCM_DEBUG", "Firebase Messaging Servoce initialized in Main");

        // Handle first-time launch
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (preferences.getBoolean("firstTime", true)) {
            preferences.edit().putBoolean("firstTime", false).apply();
            startActivity(new Intent(this, LandingActivity.class));
            finish();
            return;
        }

        // Set up the custom action bar.
        setupCustomActionBar();
        // Set up bottom navigation and nav controller.
        setupNavigation();

        // Request notification permission (Android 13+)
        requestNotificationPermission();

        // Initialize FCM and notifications
        initializeFCM();

        // Test notification channel
        testChannelNow();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1002
                );
            }
        }
    }

    private void initializeFCM() {
        // Get FCM token and start service
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "FCM Token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Store token in Firestore if needed
                    storeTokenInFirestore(token);
                });
    }

    private void storeTokenInFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .set(Collections.singletonMap("fcmToken", token), SetOptions.merge()) // This will create or update
                    .addOnFailureListener(e -> Log.e(TAG, "Token save failed", e));
        }
    }
    private void testChannelNow() {
        NotificationManager manager = getSystemService(NotificationManager.class);
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

        // Immediately send test notification
        sendTestNotification();
    }

    private void sendTestNotification() {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle("TEST NOTIFICATION")
                    .setContentText("Check if you see this at " + System.currentTimeMillis())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "Test notification sent at " + System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "Notification failed", e);
        }
    }


    private void setupNotificationPolicy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (manager.isNotificationPolicyAccessGranted()) {
                applyNotificationPolicy(manager);
            } else {
                showPolicyPermissionDialog();
            }
        }
    }

    private void applyNotificationPolicy(NotificationManager manager) {
        try {
            manager.setNotificationPolicy(
                    new NotificationManager.Policy(
                            NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS |
                                    NotificationManager.Policy.PRIORITY_CATEGORY_REMINDERS,
                            0, 0
                    )
            );
            Log.d(TAG, "Notification policy applied successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply notification policy", e);
        }
    }

    private void showPolicyPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Important Alert Settings")
                .setMessage("To ensure you receive critical drink alerts even in Do Not Disturb mode, please enable notification access:")
                .setPositiveButton("Enable", (dialog, which) -> {
                    openNotificationPolicySettings();
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    Log.d(TAG, "User skipped notification policy setup");
                    // Proceed with basic notification setup
                    createBasicNotificationChannel();
                })
                .setCancelable(false)
                .show();
    }

    private void openNotificationPolicySettings() {
        try {
            Intent intent = new Intent(
                    android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            );
            startActivityForResult(intent, NOTIFICATION_POLICY_ACCESS_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open notification settings", e);
            // Fallback to basic setup
            createBasicNotificationChannel();
        }
    }

    private void createBasicNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete old channel if exists (forces update)
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.deleteNotificationChannel(CHANNEL_ID);

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "DrinkWise Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            // MUST ADD THESE CONFIGURATIONS:
            channel.setDescription("Important drink alerts");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            channel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOTIFICATION_POLICY_ACCESS_REQUEST_CODE) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager.isNotificationPolicyAccessGranted()) {
                applyNotificationPolicy(manager);
            } else {
                // User didn't grant permission - proceed with basic setup
                createBasicNotificationChannel();
                Toast.makeText(this,
                        "Notifications will work, but may be silenced during Do Not Disturb",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Optionally override onOptionsItemSelected to handle menu item clicks.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If profile icon is clicked, launch SettingsActivity.
        if(item.getItemId() == R.id.profile_icon){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    // Sets up a custom action bar with a title and buttons for info and profile.
    @SuppressLint({"SetTextI18", "SetTextI18n"})
    private void setupCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Enable custom view and hide default title.
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.custom_actionbar_title, null);

            // Set the initial title to "DrinkWise".
            actionBarTitle = customView.findViewById(R.id.action_bar_title);
            actionBarTitle.setText("DrinkWise");

            // Set up the info button.
            ImageButton infoButton = customView.findViewById(R.id.info_button);
            infoButton.setVisibility(View.VISIBLE);
            infoButton.setOnClickListener(v -> {
                // Launch InfoActivity with a reverse sliding animation.
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });

            // Set up the profile button.
            ImageButton profileButton = customView.findViewById(R.id.profile_icon);
            profileButton.setOnClickListener(v -> {
                // Launch SettingsActivity with default sliding animation.
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });

            // Apply the custom view to the action bar with centered title.
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL
            );
            actionBar.setCustomView(customView, layoutParams);

            // Update the action bar title based on navigation destination changes.
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                if (destId == R.id.navigation_home) {
                    actionBarTitle.setText("History");
                } else if (destId == R.id.navigation_dashboard) {
                    actionBarTitle.setText("Dashboard");
                } else if (destId == R.id.navigation_notifications) {
                    actionBarTitle.setText("Notifications");
                } else {
                    actionBarTitle.setText("DrinkWise");
                }
            });
        }
    }

    // Sets up bottom navigation using NavController and AppBarConfiguration.
    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear the "toDashboard" flag if present.
        Intent intent = getIntent();
        if (intent.getBooleanExtra("toDashboard", false)) {
            intent.removeExtra("toDashboard");
        }
        // If a latest BAC entry is provided, navigate to the Dashboard with that entry.
        String latestBacEntry = intent.getStringExtra("latest_bac_entry");
        if (latestBacEntry != null) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putString("latest_bac_entry", latestBacEntry);
            navController.navigate(R.id.navigation_dashboard, bundle);
        }
        // If the user is logged in, start the reminder systems.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("ReminderTesting", "User is logged in: " + user.getUid());

            // Start the ReminderManager.
            ReminderManager reminderManager = ReminderManager.getInstance(this);
            reminderManager.startReminders();
            Log.d("ReminderTesting", "Reminders started successfully");

            // Start the ReminderListener.
            ReminderListener reminderListener = new ReminderListener(this);
            reminderListener.startListening(user.getUid());
        } else {
            Log.d("ReminderTesting", "No user logged in. Reminders not started.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Reset the firstTime flag in SharedPreferences.
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferences.edit().putBoolean("firstTime", true).apply();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        // Update the current intent.
        setIntent(intent);
    }
}