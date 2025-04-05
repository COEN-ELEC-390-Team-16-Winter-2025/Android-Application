package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.drinkwise.app.databinding.ActivityMainBinding;
import com.drinkwise.app.ui.notifications.ReminderListener;
import com.drinkwise.app.ui.notifications.ReminderManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

// MainActivity is the central activity of the app. It sets up the action bar,
// navigation components, and initializes Firebase along with reminders.
public class MainActivity extends AppCompatActivity {

    // ViewBinding instance to access views in activity_main.xml.
    private ActivityMainBinding binding;
    // TextView for the custom action bar title.
    private TextView actionBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase.
        FirebaseApp.initializeApp(this);

        // Get SharedPreferences and check if this is the first time the app is launched.
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean firstTime = preferences.getBoolean("firstTime", true);

        if (firstTime) {
            // Mark that the landing page has been shown and launch LandingActivity.
            preferences.edit().putBoolean("firstTime", false).apply();
            startActivity(new Intent(this, LandingActivity.class));
            finish();
            return;
        }

        // Set up the custom action bar.
        setupCustomActionBar();
        // Set up bottom navigation and nav controller.
        setupNavigation();
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