package com.drinkwise.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.drinkwise.app.databinding.ActivityMainBinding;

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
            // Set firstTime to false so next launch skips this block
            preferences.edit().putBoolean("firstTime", false).apply();

            // Launch LandingActivity on first app open
            startActivity(new Intent(this, LandingActivity.class));
            finish(); // Kill MainActivity so it's not in back stack
            return;
        }

        // Set up custom ActionBar title
        setupCustomActionBar();

        // Setup BottomNavigation and NavController
        setupNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.profile_icon){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.custom_actionbar_title, null);
            actionBarTitle = customView.findViewById(R.id.action_bar_title);

            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL
            );

            actionBar.setCustomView(customView, layoutParams);

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

        // Clear the toDashboard flag to prevent looping
        if (intent.getBooleanExtra("toDashboard", false)) {
            intent.removeExtra("toDashboard");
        }

        // Handle latest_bac_entry navigation if available
        String latestBacEntry = intent.getStringExtra("latest_bac_entry");
        if (latestBacEntry != null) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putString("latest_bac_entry", latestBacEntry);
            navController.navigate(R.id.navigation_dashboard, bundle);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // delete later
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferences.edit().putBoolean("firstTime", true).apply();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
