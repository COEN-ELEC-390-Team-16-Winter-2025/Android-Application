package com.drinkwise.app;

import android.app.ComponentCaller;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.drinkwise.app.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TextView actionBarTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup custom ActionBar title
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
                    actionBarTitle.setText("Home");
                } else if (destId == R.id.navigation_dashboard) {
                    actionBarTitle.setText("Dashboard");
                } else if (destId == R.id.navigation_notifications) {
                    actionBarTitle.setText("Notifications");
                } else {
                    actionBarTitle.setText("DrinkWise");
                }
            });

        }

        FirebaseApp.initializeApp(this);

        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        boolean returningUser = preferences.getBoolean("returningUser", false);
        // If first time, show Landing Page
        if (!returningUser) {
            startActivity(new Intent(this, LandingActivity.class));
            finish(); // Close MainActivity so it doesn't stay in back stack
            return;
        }


        // Normal behavior if it's not the first time



        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }



    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if(intent.getBooleanExtra("toDashboard", false)){
            intent.removeExtra("toDashboard");
        }



        if(intent.getStringExtra("latest_bac_entry") != null) {
            // Navigate to DashboardFragment and pass data
            String bac_entry = intent.getStringExtra("latest_bac_entry");
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putString("latest_bac_entry", intent.getStringExtra("latest_bac_entry"));
            navController.navigate(R.id.navigation_dashboard, bundle);
        }
    }

    // delete later
    @Override
    protected void onStop() {
        super.onStop();

        // Reset on app background/close
        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        preferences.edit().putBoolean("isFirstTime", true).apply();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}


