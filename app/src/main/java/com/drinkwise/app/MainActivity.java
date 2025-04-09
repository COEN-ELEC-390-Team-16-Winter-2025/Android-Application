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

// MainActivity is the central activity of the app. It sets up the action bar, the navigation, and it starts up firebase
public class MainActivity extends AppCompatActivity {

    // to access views in activity_main.xml.
    private ActivityMainBinding binding;
    private TextView actionBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean firstTime = preferences.getBoolean("firstTime", true);

        // if first time mark that the landing page has been shown and launch LandingActivity
        if (firstTime) {
            preferences.edit().putBoolean("firstTime", false).apply();
            startActivity(new Intent(this, LandingActivity.class));
            finish();
            return;
        }

        setupCustomActionBar();
        setupNavigation();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.profile_icon){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    // custom action bar with a title and buttons for info and profile
    @SuppressLint({"SetTextI18", "SetTextI18n"})
    private void setupCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            LayoutInflater inflater = LayoutInflater.from(this);
            @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.custom_actionbar_title, null);
            actionBarTitle = customView.findViewById(R.id.action_bar_title);
            actionBarTitle.setText("DrinkWise");

            // info button.
            ImageButton infoButton = customView.findViewById(R.id.info_button);
            infoButton.setVisibility(View.VISIBLE);
            infoButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });

            // profile button.
            ImageButton profileButton = customView.findViewById(R.id.profile_icon);
            profileButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });

            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL
            );
            actionBar.setCustomView(customView, layoutParams);

            // this updates the action bar title based on navigation destination changes
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

    // bottom navigation using NavController and AppBarConfiguration
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
        // reminder systems if logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("ReminderTesting", "User is logged in: " + user.getUid());

            // start ReminderManager
            ReminderManager reminderManager = ReminderManager.getInstance(this);
            reminderManager.startReminders();
            Log.d("ReminderTesting", "Reminders started successfully");

            // start ReminderListener
            ReminderListener reminderListener = new ReminderListener(this);
            reminderListener.startListening(user.getUid());
        } else {
            Log.d("ReminderTesting", "No user logged in. Reminders not started.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // reset the firstTime flag in SharedPreferences.
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferences.edit().putBoolean("firstTime", true).apply();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}