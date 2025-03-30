package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
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

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TextView actionBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Get SharedPreferences
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean firstTime = preferences.getBoolean("firstTime", true);

        if (firstTime) {
            preferences.edit().putBoolean("firstTime", false).apply();
            startActivity(new Intent(this, LandingActivity.class));
            finish();
            return;
        }

        setupCustomActionBar(true, "DrinkWise");
        setupNavigation();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.profile_menu, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.profile_icon){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18")
    private void setupCustomActionBar(boolean showInfoButton, String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.custom_actionbar_title, null);

            // Set the title
            actionBarTitle = customView.findViewById(R.id.action_bar_title);
            actionBarTitle.setText(title);

            // Info button logic
            ImageButton infoButton = customView.findViewById(R.id.info_button);
            if (showInfoButton) {
                infoButton.setVisibility(View.VISIBLE);
                infoButton.setOnClickListener(v -> {
                    // Launch InfoActivity with reverse sliding animation
                    Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                });
            } else {
                infoButton.setVisibility(View.GONE);
            }

            // Profile button logic
            ImageButton profileButton = customView.findViewById(R.id.profile_icon);
            profileButton.setOnClickListener(v -> {
                // Launch SettingsActivity with the default sliding animation
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });

            // Apply the custom action bar layout
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL
            );
            actionBar.setCustomView(customView, layoutParams);

            // Update action bar title based on navigation destination
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
        Intent intent = getIntent();
        if (intent.getBooleanExtra("toDashboard", false)) {
            intent.removeExtra("toDashboard");
        }
        String latestBacEntry = intent.getStringExtra("latest_bac_entry");
        if (latestBacEntry != null) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putString("latest_bac_entry", latestBacEntry);
            navController.navigate(R.id.navigation_dashboard, bundle);
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("ReminderTesting", "User is logged in: " + user.getUid());

            // Start reminders if enabled
            ReminderManager reminderManager = ReminderManager.getInstance(this);
            reminderManager.startReminders();
            Log.d("ReminderTesting", "Reminders started successfully");

            ReminderListener reminderListener = new ReminderListener(this);
            reminderListener.startListening(user.getUid());
        } else {
            Log.d("ReminderTesting", "No user logged in. Reminders not started.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferences.edit().putBoolean("firstTime", true).apply();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}