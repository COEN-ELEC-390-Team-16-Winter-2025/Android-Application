package com.drinkwise.app;

import android.app.ComponentCaller;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.drinkwise.app.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // Retrieve SharedPreferences
//        SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//        boolean isFirstTime = preferences.getBoolean("isFirstTime", true); // Default: true (first time)
//
//        // If first time, show Landing Page
//        if (isFirstTime) {
//            startActivity(new Intent(this, LandingActivity.class));
//            finish(); // Close MainActivity so it doesn't stay in back stack
//            return;
//        }

        // Normal behavior if it's not the first time
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        String bac_entry = intent.getStringExtra("latest_bac_entry");
        if(bac_entry == null){
            Log.d("Main Activity", "Object is null");
        }
        // Navigate to DashboardFragment and pass data
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        Bundle bundle = new Bundle();
        bundle.putString("latest_bac_entry", intent.getStringExtra("latest_bac_entry"));
        navController.navigate(R.id.navigation_dashboard, bundle);
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



